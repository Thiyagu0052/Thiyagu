package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat

enum class TransactionType(val label: String, val isDeliveryType: Boolean, val isReturnType: Boolean) {
    DELIVERY("Delivery", true, false),
    RETURN_KACHA("Return Kacha", false, true);

    companion object {
        fun fromLabel(label: String): TransactionType {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: DELIVERY
        }
    }
}

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val gstNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val category: String,
    val defaultWeight: Double = 0.0,
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD or DD-MM-YYYY format
    val shopId: Long,
    val shopName: String,
    val type: String, // Delivery, Return Kacha, Return Fine, Settlement
    val weight: Double,
    val touch: Double,
    val touchAdjustment: Double = 0.0,
    val pureWeight: Double,
    val remarks: String = "",
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
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
        if (weight <= 0.0 || touch <= 0.0) return 0.0
        val effectiveTouch = touch + touchAdjustment
        val pure = (weight * effectiveTouch) / 100.0
        // Round to 1 decimal place as per spec (e.g., 6369 * 76 / 100 = 4840.4)
        return try {
            df.format(pure).toDouble()
        } catch (e: Exception) {
            Math.round(pure * 10.0) / 10.0
        }
    }

    fun formatWeight(weight: Double): String {
        return df.format(weight) + " g"
    }
}
