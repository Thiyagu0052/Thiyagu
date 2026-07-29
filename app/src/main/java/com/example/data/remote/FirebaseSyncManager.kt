package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.Product
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val message: String, val lastSyncedAt: String) : SyncStatus()
    data class Error(val errorMessage: String) : SyncStatus()
}

class FirebaseSyncManager {

    companion object {
        private const val TAG = "FirebaseSyncManager"
        private const val COLLECTION_SHOPS = "shops"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val STORAGE_BUCKET_URL = "gs://kkysilversalem.firebasestorage.app"
    }

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy {
        try {
            FirebaseStorage.getInstance(STORAGE_BUCKET_URL)
        } catch (e: Exception) {
            FirebaseStorage.getInstance()
        }
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var shopListener: ListenerRegistration? = null
    private var productListener: ListenerRegistration? = null
    private var transactionListener: ListenerRegistration? = null

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Listens for real-time updates from Firebase Firestore across all devices
     */
    fun startRealtimeSync(
        onShopsReceived: (List<Shop>) -> Unit,
        onProductsReceived: (List<Product>) -> Unit,
        onTransactionsReceived: (List<Transaction>) -> Unit
    ) {
        try {
            // Listen for Shops
            shopListener?.remove()
            shopListener = firestore.collection(COLLECTION_SHOPS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Shops listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val shopList = snapshot.documents.mapNotNull { doc ->
                            try {
                                Shop(
                                    id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null,
                                    shopName = doc.getString("shopName") ?: "",
                                    ownerName = doc.getString("ownerName") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    address = doc.getString("address") ?: "",
                                    gstNumber = doc.getString("gstNumber") ?: "",
                                    notes = doc.getString("notes") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing shop doc ${doc.id}", e)
                                null
                            }
                        }
                        onShopsReceived(shopList)
                        _syncStatus.value = SyncStatus.Success("Real-time sync active", getFormattedTime())
                    }
                }

            // Listen for Products
            productListener?.remove()
            productListener = firestore.collection(COLLECTION_PRODUCTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Products listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val productList = snapshot.documents.mapNotNull { doc ->
                            try {
                                Product(
                                    id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null,
                                    productName = doc.getString("productName") ?: "",
                                    category = doc.getString("category") ?: "",
                                    defaultWeight = doc.getDouble("defaultWeight") ?: 0.0,
                                    imageUri = doc.getString("imageUri") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing product doc ${doc.id}", e)
                                null
                            }
                        }
                        onProductsReceived(productList)
                    }
                }

            // Listen for Transactions
            transactionListener?.remove()
            transactionListener = firestore.collection(COLLECTION_TRANSACTIONS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Transactions listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val txList = snapshot.documents.mapNotNull { doc ->
                            try {
                                Transaction(
                                    id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null,
                                    date = doc.getString("date") ?: "",
                                    shopId = doc.getLong("shopId") ?: 0L,
                                    shopName = doc.getString("shopName") ?: "",
                                    type = doc.getString("type") ?: "",
                                    weight = doc.getDouble("weight") ?: 0.0,
                                    touch = doc.getDouble("touch") ?: 0.0,
                                    touchAdjustment = doc.getDouble("touchAdjustment") ?: 0.0,
                                    pureWeight = doc.getDouble("pureWeight") ?: 0.0,
                                    remarks = doc.getString("remarks") ?: "",
                                    imageUri = doc.getString("imageUri") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing transaction doc ${doc.id}", e)
                                null
                            }
                        }
                        onTransactionsReceived(txList)
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize realtime sync", e)
            _syncStatus.value = SyncStatus.Error("Firebase sync error: ${e.localizedMessage}")
        }
    }

    fun stopRealtimeSync() {
        shopListener?.remove()
        productListener?.remove()
        transactionListener?.remove()
    }

    /**
     * Upserts a Shop to Firebase Firestore
     */
    suspend fun syncShop(shop: Shop) = withContext(Dispatchers.IO) {
        try {
            val map = hashMapOf<String, Any>(
                "id" to shop.id,
                "shopName" to shop.shopName,
                "ownerName" to shop.ownerName,
                "phone" to shop.phone,
                "address" to shop.address,
                "gstNumber" to shop.gstNumber,
                "notes" to shop.notes,
                "createdAt" to shop.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_SHOPS).document(shop.id.toString()).set(map).await()
            _syncStatus.value = SyncStatus.Success("Synced to Firebase", getFormattedTime())
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing shop ${shop.id}", e)
            _syncStatus.value = SyncStatus.Error(e.localizedMessage ?: "Sync error")
        }
    }

    /**
     * Deletes a Shop from Firebase Firestore
     */
    suspend fun deleteShop(shopId: Long) = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_SHOPS).document(shopId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting shop $shopId", e)
        }
    }

    /**
     * Upserts a Product to Firebase Firestore
     */
    suspend fun syncProduct(product: Product) = withContext(Dispatchers.IO) {
        try {
            val map = hashMapOf<String, Any>(
                "id" to product.id,
                "productName" to product.productName,
                "category" to product.category,
                "defaultWeight" to product.defaultWeight,
                "imageUri" to product.imageUri,
                "createdAt" to product.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_PRODUCTS).document(product.id.toString()).set(map).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing product ${product.id}", e)
        }
    }

    /**
     * Deletes a Product from Firebase Firestore
     */
    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_PRODUCTS).document(productId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product $productId", e)
        }
    }

    /**
     * Upserts a Transaction to Firebase Firestore
     */
    suspend fun syncTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            val map = hashMapOf<String, Any>(
                "id" to transaction.id,
                "date" to transaction.date,
                "shopId" to transaction.shopId,
                "shopName" to transaction.shopName,
                "type" to transaction.type,
                "weight" to transaction.weight,
                "touch" to transaction.touch,
                "touchAdjustment" to transaction.touchAdjustment,
                "pureWeight" to transaction.pureWeight,
                "remarks" to transaction.remarks,
                "imageUri" to transaction.imageUri,
                "createdAt" to transaction.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_TRANSACTIONS).document(transaction.id.toString()).set(map).await()
            _syncStatus.value = SyncStatus.Success("Synced to Firebase", getFormattedTime())
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing transaction ${transaction.id}", e)
            _syncStatus.value = SyncStatus.Error(e.localizedMessage ?: "Sync error")
        }
    }

    /**
     * Deletes a Transaction from Firebase Firestore
     */
    suspend fun deleteTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_TRANSACTIONS).document(transactionId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction $transactionId", e)
        }
    }

    /**
     * Bulk Sync all local data to Firebase
     */
    suspend fun syncAllData(
        shops: List<Shop>,
        products: List<Product>,
        transactions: List<Transaction>
    ) = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.Syncing
        shops.forEach { syncShop(it) }
        products.forEach { syncProduct(it) }
        transactions.forEach { syncTransaction(it) }
        _syncStatus.value = SyncStatus.Success("All data synced live", getFormattedTime())
    }

    /**
     * Uploads local image URI (content:// or file://) to Firebase Storage.
     * If Firebase Storage upload succeeds, returns the public download URL.
     * If Firebase Storage fails or is restricted, compresses and returns a Base64 data URI
     * (data:image/jpeg;base64,...) so the photo reflects instantly on all synced devices & web!
     */
    suspend fun uploadImageToStorage(context: Context, localUriString: String): String = withContext(Dispatchers.IO) {
        if (localUriString.isBlank() || localUriString.startsWith("http://") || localUriString.startsWith("https://") || localUriString.startsWith("data:image/")) {
            return@withContext localUriString
        }

        val uri = Uri.parse(localUriString)
        val bytes = try {
            if (localUriString.startsWith("content://")) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else if (localUriString.startsWith("file://")) {
                File(uri.path ?: "").inputStream().use { it.readBytes() }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error reading image bytes for $localUriString", e)
            null
        }

        if (bytes == null || bytes.isEmpty()) {
            return@withContext localUriString
        }

        // Try uploading to Supabase Storage first
        try {
            val filename = "proofs/${UUID.randomUUID()}.jpg"
            val uploadUrl = "${SupabaseSyncManager.SUPABASE_URL}/storage/v1/object/${SupabaseSyncManager.STORAGE_BUCKET}/$filename"
            val mediaType = "image/jpeg".toMediaType()
            val requestBody = bytes.toRequestBody(mediaType)

            val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .addHeader("apikey", SupabaseSyncManager.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseSyncManager.SUPABASE_KEY}")
                .addHeader("Content-Type", "image/jpeg")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 200 || response.code == 201) {
                    val publicUrl = "${SupabaseSyncManager.SUPABASE_URL}/storage/v1/object/public/${SupabaseSyncManager.STORAGE_BUCKET}/$filename"
                    Log.d(TAG, "Successfully uploaded image to Supabase Storage: $publicUrl")
                    return@withContext publicUrl
                } else {
                    Log.d(TAG, "Supabase Storage upload returned code ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Storage upload fallback check", e)
        }

        // Try uploading to Firebase Storage second
        try {
            val filename = "proofs/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(filename)
            val uploadTask = ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Successfully uploaded image to Firebase Storage: $downloadUrl")
            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage upload failed/unconfigured, converting to compressed Base64 data URI", e)
        }

        // Fallback: Compress bitmap & convert to Base64 Data URI so all synced devices can view it
        return@withContext try {
            val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (originalBitmap != null) {
                val maxDimension = 800
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scale = if (width > maxDimension || height > maxDimension) {
                    maxDimension.toFloat() / Math.max(width, height)
                } else 1.0f

                val scaledBitmap = if (scale < 1.0f) {
                    android.graphics.Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
                } else {
                    originalBitmap
                }

                val outputStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val compressedArray = outputStream.toByteArray()
                val base64 = android.util.Base64.encodeToString(compressedArray, android.util.Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64"
            } else {
                localUriString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed Base64 conversion fallback", e)
            localUriString
        }
    }
}

