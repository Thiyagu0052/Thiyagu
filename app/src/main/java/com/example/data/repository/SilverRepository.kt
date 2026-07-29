package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.ShopDao
import com.example.data.local.TransactionDao
import com.example.data.model.HoldSummary
import com.example.data.model.Product
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine

class SilverRepository(
    private val shopDao: ShopDao,
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) {
    val allShops: Flow<List<Shop>> = shopDao.getAllShops()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val shopCount: Flow<Int> = shopDao.getShopCount()

    val holdSummary: Flow<HoldSummary> = combine(
        allTransactions,
        shopCount
    ) { txs, count ->
        var deliverySum = 0.0
        var returnSum = 0.0

        txs.forEach { tx ->
            val type = TransactionType.fromLabel(tx.type)
            if (type.isDeliveryType) {
                deliverySum += tx.pureWeight
            } else if (type.isReturnType) {
                returnSum += tx.pureWeight
            }
        }

        HoldSummary(
            totalDelivery = Math.round(deliverySum * 10.0) / 10.0,
            totalReturn = Math.round(returnSum * 10.0) / 10.0,
            currentHold = Math.round((deliverySum - returnSum) * 10.0) / 10.0,
            totalShops = count,
            totalTransactions = txs.size
        )
    }

    fun getTransactionsByShop(shopId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByShop(shopId)

    suspend fun insertShop(shop: Shop): Long = shopDao.insertShop(shop)
    suspend fun updateShop(shop: Shop) = shopDao.updateShop(shop)
    suspend fun deleteShop(shop: Shop) = shopDao.deleteShop(shop)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        shopDao.deleteAllShops()
        productDao.deleteAllProducts()
    }

    suspend fun saveShopsFromRemote(shops: List<Shop>) {
        val existingShops = shopDao.getAllShops().first()
        shops.forEach { remote ->
            val match = existingShops.find { it.id == remote.id || it.shopName.equals(remote.shopName, ignoreCase = true) }
            if (match != null) {
                shopDao.updateShop(remote.copy(id = match.id))
            } else {
                shopDao.insertShop(remote)
            }
        }
        // Remove duplicate shops with identical names
        val allCurrent = shopDao.getAllShops().first()
        val seenNames = mutableSetOf<String>()
        allCurrent.forEach { shop ->
            val key = shop.shopName.trim().lowercase()
            if (seenNames.contains(key)) {
                shopDao.deleteShop(shop)
            } else {
                seenNames.add(key)
            }
        }
    }

    suspend fun saveProductsFromRemote(products: List<Product>) {
        val existingProducts = productDao.getAllProducts().first()
        products.forEach { remote ->
            val match = existingProducts.find { it.id == remote.id || it.productName.equals(remote.productName, ignoreCase = true) }
            if (match != null) {
                productDao.updateProduct(remote.copy(id = match.id))
            } else {
                productDao.insertProduct(remote)
            }
        }
        val allCurrent = productDao.getAllProducts().first()
        val seenNames = mutableSetOf<String>()
        allCurrent.forEach { prod ->
            val key = prod.productName.trim().lowercase()
            if (seenNames.contains(key)) {
                productDao.deleteProduct(prod)
            } else {
                seenNames.add(key)
            }
        }
    }

    suspend fun saveTransactionsFromRemote(transactions: List<Transaction>) {
        val existingTxs = transactionDao.getAllTransactions().first()
        transactions.forEach { remote ->
            val match = existingTxs.find { local ->
                local.id == remote.id || (
                    local.date == remote.date &&
                    local.shopName.equals(remote.shopName, ignoreCase = true) &&
                    local.type == remote.type &&
                    local.weight == remote.weight &&
                    local.pureWeight == remote.pureWeight
                )
            }
            if (match != null) {
                val mergedImage = if (remote.imageUri.isNotBlank()) remote.imageUri else match.imageUri
                transactionDao.updateTransaction(remote.copy(id = match.id, imageUri = mergedImage))
            } else {
                transactionDao.insertTransaction(remote)
            }
        }
        // Remove exact duplicate transactions locally
        val allCurrent = transactionDao.getAllTransactions().first()
        val seenMap = mutableMapOf<String, Transaction>()
        allCurrent.forEach { tx ->
            val key = "${tx.date}_${tx.shopName.trim().lowercase()}_${tx.type.lowercase()}_${tx.weight}_${tx.pureWeight}"
            val existing = seenMap[key]
            if (existing != null) {
                if (tx.imageUri.isNotBlank() && existing.imageUri.isBlank()) {
                    val updated = existing.copy(imageUri = tx.imageUri)
                    transactionDao.updateTransaction(updated)
                    seenMap[key] = updated
                }
                transactionDao.deleteTransaction(tx)
            } else {
                seenMap[key] = tx
            }
        }
    }

    suspend fun mergeRemoteRecords(records: List<org.json.JSONObject>) {
        records.forEach { record ->
            val payload = record.optJSONObject("payload") ?: return@forEach
            val entityType = payload.optString("entity_type", "")
            when (entityType) {
                "shop" -> {
                    val id = payload.optLong("id")
                    val shopName = payload.optString("shopName")
                    if (id > 0 && shopName.isNotBlank()) {
                        val shop = Shop(
                            id = id,
                            shopName = shopName,
                            ownerName = payload.optString("ownerName"),
                            phone = payload.optString("phone"),
                            address = payload.optString("address"),
                            gstNumber = payload.optString("gstNumber"),
                            notes = payload.optString("notes"),
                            createdAt = payload.optLong("createdAt", System.currentTimeMillis())
                        )
                        shopDao.insertShop(shop)
                    }
                }
                "product" -> {
                    val id = payload.optLong("id")
                    val productName = payload.optString("productName")
                    if (id > 0 && productName.isNotBlank()) {
                        val product = Product(
                            id = id,
                            productName = productName,
                            category = payload.optString("category"),
                            defaultWeight = payload.optDouble("defaultWeight", 0.0),
                            imageUri = payload.optString("imageUri", ""),
                            createdAt = payload.optLong("createdAt", System.currentTimeMillis())
                        )
                        productDao.insertProduct(product)
                    }
                }
                "transaction" -> {
                    val id = payload.optLong("id")
                    val date = payload.optString("date")
                    if (id > 0 && date.isNotBlank()) {
                        val tx = Transaction(
                            id = id,
                            date = date,
                            shopId = payload.optLong("shopId"),
                            shopName = payload.optString("shopName"),
                            type = payload.optString("type"),
                            weight = payload.optDouble("weight", 0.0),
                            touch = payload.optDouble("touch", 0.0),
                            touchAdjustment = payload.optDouble("touchAdjustment", 0.0),
                            pureWeight = payload.optDouble("pureWeight", 0.0),
                            remarks = payload.optString("remarks"),
                            imageUri = payload.optString("imageUri"),
                            createdAt = payload.optLong("createdAt", System.currentTimeMillis())
                        )
                        transactionDao.insertTransaction(tx)
                    }
                }
            }
        }
    }
}
