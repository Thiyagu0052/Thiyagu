package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.text.DecimalFormat

enum class TransactionType(val label: String, val displayLabel: String, val isDeliveryType: Boolean, val isReturnType: Boolean) {
    DELIVERY("Delivery", "கொடுத்தல்", true, false),
    RETURN_KACHA("Return Kacha", "வரவு", false, true);

    companion object {
        fun fromLabel(label: String): TransactionType {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: DELIVERY
        }
    }
}

@Entity(tableName = "shops")
@JsonClass(generateAdapter = true)
data class Shop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val gstNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "products")
@JsonClass(generateAdapter = true)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val category: String,
    val defaultWeight: Double = 0.0,
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD format
    val time: String = "", // HH:mm format
    val shopId: Long,
    val shopName: String,
    val type: String, // Delivery, Return Kacha, Return Fine, Settlement
    val weight: Double,
    val touch: Double,
    val touchAdjustment: Double = 0.0,
    val pureWeight: Double,
    val remarks: String = "",
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@JsonClass(generateAdapter = true)
data class BackupData(
    val shops: List<Shop>,
    val products: List<Product>,
    val transactions: List<Transaction>,
    val backupDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0"
)

data class HoldSummary(
    val totalDelivery: Double = 0.0,
    val totalReturn: Double = 0.0,
    val currentHold: Double = 0.0,
    val totalShops: Int = 0,
    val totalTransactions: Int = 0
)

object PureWeightCalculator {
    private val df = DecimalFormat("#.#")

    /**
     * Pure Weight = Weight * (Touch + TouchAdjustment) / 100
     */
    fun calculate(weight: Double, touch: Double, touchAdjustment: Double = 0.0): Double {
        if ((weight <= 0.0) || (touch <= 0.0)) return 0.0
        val effectiveTouch = touch + touchAdjustment
        val pure = (weight * effectiveTouch) / 100.0
        // Round to 1 decimal place as per spec (e.g., 6369 * 76 / 100 = 4840.4)
        return try {
            df.format(pure).toDouble()
        } catch (_: Exception) {
            kotlin.math.round(pure * 10.0) / 10.0
        }
    }

    fun formatWeight(weight: Double): String {
        return df.format(weight) + " g"
    }
}
