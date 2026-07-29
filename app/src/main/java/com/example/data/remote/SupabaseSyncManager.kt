package com.example.data.remote

import android.util.Log
import com.example.data.model.Product
import com.example.data.model.Shop
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncStatus {
    object Idle : SupabaseSyncStatus()
    object Syncing : SupabaseSyncStatus()
    data class Success(val message: String, val lastSyncedAt: String) : SupabaseSyncStatus()
    data class Error(val errorMessage: String) : SupabaseSyncStatus()
}

class SupabaseSyncManager {

    companion object {
        const val SUPABASE_URL = "https://lgnvysxvjhukafabsboc.supabase.co"
        const val SUPABASE_KEY = "sb_publishable_aPaE0Vo0V9kkuaCPV2rQtQ_asiFwguQ"
        const val TABLE_NAME = "silver_erp_records"
        const val STORAGE_BUCKET = "SilverERP"
        private const val TAG = "SupabaseSyncManager"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _syncStatus = MutableStateFlow<SupabaseSyncStatus>(SupabaseSyncStatus.Idle)
    val syncStatus: StateFlow<SupabaseSyncStatus> = _syncStatus.asStateFlow()

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun generateDeterministicUuid(prefix: String, id: Long): String {
        val raw = "silver_erp_${prefix}_${id}"
        return UUID.nameUUIDFromBytes(raw.toByteArray()).toString()
    }

    /**
     * Upserts all shops, products, and transactions to Supabase table silver_erp_records
     */
    suspend fun syncAllData(
        shops: List<Shop>,
        products: List<Product>,
        transactions: List<Transaction>
    ): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.value = SupabaseSyncStatus.Syncing
        try {
            val recordsArray = JSONArray()
            val now = getCurrentIsoTimestamp()

            // Map Shops
            shops.forEach { shop ->
                val uuid = generateDeterministicUuid("shop", shop.id)
                val payload = JSONObject().apply {
                    put("entity_type", "shop")
                    put("id", shop.id)
                    put("shopName", shop.shopName)
                    put("ownerName", shop.ownerName)
                    put("phone", shop.phone)
                    put("address", shop.address)
                    put("gstNumber", shop.gstNumber)
                    put("notes", shop.notes)
                    put("createdAt", shop.createdAt)
                }

                val row = JSONObject().apply {
                    put("record_type", "shops")
                    put("record_id", uuid)
                    put("payload", payload)
                    put("updated_at", now)
                }
                recordsArray.put(row)
            }

            // Map Products
            products.forEach { product ->
                val uuid = generateDeterministicUuid("product", product.id)
                val payload = JSONObject().apply {
                    put("entity_type", "product")
                    put("id", product.id)
                    put("productName", product.productName)
                    put("category", product.category)
                    put("defaultWeight", product.defaultWeight)
                    put("imageUri", product.imageUri)
                    put("createdAt", product.createdAt)
                }

                val row = JSONObject().apply {
                    put("record_type", "products")
                    put("record_id", uuid)
                    put("payload", payload)
                    put("updated_at", now)
                }
                recordsArray.put(row)
            }

            // Map Transactions
            transactions.forEach { tx ->
                val uuid = generateDeterministicUuid("transaction", tx.id)
                val payload = JSONObject().apply {
                    put("entity_type", "transaction")
                    put("id", tx.id)
                    put("date", tx.date)
                    put("time", tx.time)
                    put("shopId", tx.shopId)
                    put("shopName", tx.shopName)
                    put("type", tx.type)
                    put("weight", tx.weight)
                    put("touch", tx.touch)
                    put("touchAdjustment", tx.touchAdjustment)
                    put("pureWeight", tx.pureWeight)
                    put("remarks", tx.remarks)
                    put("imageUri", tx.imageUri)
                    put("createdAt", tx.createdAt)
                }

                val row = JSONObject().apply {
                    put("record_type", "transactions")
                    put("record_id", uuid)
                    put("payload", payload)
                    put("updated_at", now)
                }
                recordsArray.put(row)
            }

            if (recordsArray.length() == 0) {
                _syncStatus.value = SupabaseSyncStatus.Success("0 records to sync", getFormattedTime())
                return@withContext true
            }

            val requestUrl = "$SUPABASE_URL/rest/v1/$TABLE_NAME"
            val body = recordsArray.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 201 || response.code == 204) {
                    val count = recordsArray.length()
                    _syncStatus.value = SupabaseSyncStatus.Success("Synced $count items", getFormattedTime())
                    Log.d(TAG, "Successfully synced $count items to Supabase")
                    true
                } else {
                    val errBody = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "Sync failed: Code ${response.code} -> $errBody")
                    _syncStatus.value = SupabaseSyncStatus.Error("Http ${response.code}: $errBody")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sync", e)
            _syncStatus.value = SupabaseSyncStatus.Error(e.localizedMessage ?: "Sync error")
            false
        }
    }

    /**
     * Fetches remote records from Supabase table silver_erp_records
     */
    suspend fun fetchRemoteRecords(): List<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val requestUrl = "$SUPABASE_URL/rest/v1/$TABLE_NAME?select=*"
            val request = Request.Builder()
                .url(requestUrl)
                .get()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(bodyString)
                    val resultList = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        resultList.add(jsonArray.getJSONObject(i))
                    }
                    resultList
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch remote records", e)
            emptyList()
        }
    }
}
