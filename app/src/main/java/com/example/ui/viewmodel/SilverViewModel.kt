package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.HoldSummary
import com.example.data.model.Product
import com.example.data.model.PureWeightCalculator
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.repository.SilverRepository
import com.example.data.remote.FirebaseSyncManager
import com.example.data.remote.SyncStatus
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SilverViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SilverRepository(db.shopDao(), db.productDao(), db.transactionDao())
    val firebaseSyncManager = FirebaseSyncManager()

    val syncStatus: StateFlow<SyncStatus> = firebaseSyncManager.syncStatus

    init {
        // Start real-time Firebase Firestore synchronization listener
        firebaseSyncManager.startRealtimeSync(
            onShopsReceived = { remoteShops ->
                viewModelScope.launch {
                    if (remoteShops.isNotEmpty()) {
                        repository.saveShopsFromRemote(remoteShops)
                    }
                }
            },
            onProductsReceived = { remoteProducts ->
                viewModelScope.launch {
                    if (remoteProducts.isNotEmpty()) {
                        repository.saveProductsFromRemote(remoteProducts)
                    }
                }
            },
            onTransactionsReceived = { remoteTxs ->
                viewModelScope.launch {
                    if (remoteTxs.isNotEmpty()) {
                        repository.saveTransactionsFromRemote(remoteTxs)
                    }
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        firebaseSyncManager.stopRealtimeSync()
    }

    fun syncToFirebase() {
        viewModelScope.launch {
            val currentShops = repository.allShops.first()
            val currentProducts = repository.allProducts.first()
            val currentTxs = repository.allTransactions.first()
            firebaseSyncManager.syncAllData(currentShops, currentProducts, currentTxs)
        }
    }

    val shops: StateFlow<List<Shop>> = repository.allShops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holdSummary: StateFlow<HoldSummary> = repository.holdSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HoldSummary())

    // Search and Filters for Transactions
    val searchQuery = MutableStateFlow("")
    val selectedShopFilterId = MutableStateFlow<Long?>(null)
    val selectedTypeFilter = MutableStateFlow<String?>(null)

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        searchQuery,
        selectedShopFilterId,
        selectedTypeFilter
    ) { txs, query, shopId, type ->
        txs.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.shopName.contains(query, ignoreCase = true) ||
                    tx.remarks.contains(query, ignoreCase = true) ||
                    tx.type.contains(query, ignoreCase = true)
            val matchesShop = (shopId == null) || (tx.shopId == shopId)
            val matchesType = type.isNullOrBlank() || tx.type.equals(type, ignoreCase = true)
            matchesQuery && matchesShop && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun uploadTransactionImages(rawImageUri: String): String {
        if (rawImageUri.isBlank()) return ""
        val uris = ImageUtils.parseImageUris(rawImageUri)
        val uploadedList = uris.map { uriStr ->
            if (uriStr.startsWith("content://") || uriStr.startsWith("file://")) {
                firebaseSyncManager.uploadImageToStorage(getApplication(), uriStr)
            } else {
                uriStr
            }
        }
        return ImageUtils.joinImageUris(uploadedList)
    }

    // Shop Operations
    fun addShop(shopName: String, ownerName: String, phone: String, address: String, gstNumber: String, notes: String) {
        viewModelScope.launch {
            val shop = Shop(
                shopName = shopName.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                gstNumber = gstNumber.trim(),
                notes = notes.trim()
            )
            val newId = repository.insertShop(shop)
            val insertedShop = shop.copy(id = newId)
            firebaseSyncManager.syncShop(insertedShop)
        }
    }

    fun updateShop(shop: Shop) {
        viewModelScope.launch {
            repository.updateShop(shop)
            firebaseSyncManager.syncShop(shop)
            val currentTxs = transactions.value
            currentTxs.filter { it.shopId == shop.id }.forEach { tx ->
                if (tx.shopName != shop.shopName) {
                    val updatedTx = tx.copy(shopName = shop.shopName)
                    repository.updateTransaction(updatedTx)
                    firebaseSyncManager.syncTransaction(updatedTx)
                }
            }
        }
    }

    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            repository.deleteShop(shop)
            firebaseSyncManager.deleteShop(shop.id)
        }
    }

    // Product Operations
    fun addProduct(productName: String, category: String, defaultWeight: Double, imageUri: String = "") {
        viewModelScope.launch {
            val names = productName.split(",", "\n").asSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
            val uploadedImageUri = uploadTransactionImages(imageUri)
            for (name in names) {
                val product = Product(
                    productName = name,
                    category = category.trim(),
                    defaultWeight = defaultWeight,
                    imageUri = uploadedImageUri
                )
                val newId = repository.insertProduct(product)
                firebaseSyncManager.syncProduct(product.copy(id = newId))
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val uploadedImageUri = if (product.imageUri.isNotBlank()) uploadTransactionImages(product.imageUri) else ""
            val updated = product.copy(imageUri = uploadedImageUri)
            repository.updateProduct(updated)
            firebaseSyncManager.syncProduct(updated)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            firebaseSyncManager.deleteProduct(product.id)
        }
    }

    // Transaction Operations
    fun addTransaction(
        date: String,
        shopId: Long,
        shopName: String,
        type: String,
        weight: Double,
        touch: Double,
        touchAdjustment: Double,
        remarks: String,
        imageUri: String = ""
    ) {
        val pureWeight = PureWeightCalculator.calculate(weight, touch, touchAdjustment)
        viewModelScope.launch {
            val hostedImageUri = uploadTransactionImages(imageUri)
            val tx = Transaction(
                date = date,
                shopId = shopId,
                shopName = shopName,
                type = type,
                weight = weight,
                touch = touch,
                touchAdjustment = touchAdjustment,
                pureWeight = pureWeight,
                remarks = remarks.trim(),
                imageUri = hostedImageUri
            )
            val newId = repository.insertTransaction(tx)
            firebaseSyncManager.syncTransaction(tx.copy(id = newId))
        }
    }

    fun updateTransaction(
        id: Long,
        date: String,
        shopId: Long,
        shopName: String,
        type: String,
        weight: Double,
        touch: Double,
        touchAdjustment: Double,
        remarks: String,
        imageUri: String = ""
    ) {
        val pureWeight = PureWeightCalculator.calculate(weight, touch, touchAdjustment)
        viewModelScope.launch {
            val hostedImageUri = uploadTransactionImages(imageUri)
            val tx = Transaction(
                id = id,
                date = date,
                shopId = shopId,
                shopName = shopName,
                type = type,
                weight = weight,
                touch = touch,
                touchAdjustment = touchAdjustment,
                pureWeight = pureWeight,
                remarks = remarks.trim(),
                imageUri = hostedImageUri
            )
            repository.updateTransaction(tx)
            firebaseSyncManager.syncTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            firebaseSyncManager.deleteTransaction(transaction.id)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
            syncToFirebase()
        }
    }
}
