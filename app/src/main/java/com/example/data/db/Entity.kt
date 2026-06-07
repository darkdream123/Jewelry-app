package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jewel_items")
data class JewelItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,          // e.g., "G-R01" for Gold Ring 01
    val name: String,          // e.g., "Classic Gold Band"
    val metalType: String,     // "Gold", "Silver", "Platinum"
    val purity: String,        // "18K", "22K", "24K", "950 Pt", "925 Silver"
    val weight: Double,        // weight in grams
    val makingCharges: Double, // making charges (can be per gram or flat)
    val chargeType: String,    // "Per Gram", "Flat Rate"
    val iconType: String,      // "Ring", "Necklace", "Earrings", "Bracelet", "Bangle", "Chain", "Pendant"
    val stockCount: Int,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Dynamic price calculation based on current metal rates
    fun calculatePrice(
        gold24kRate: Double,
        gold22kRate: Double,
        gold18kRate: Double,
        silverRate: Double
    ): Double {
        val baseRate = when (metalType.lowercase()) {
            "gold" -> {
                when (purity.uppercase()) {
                    "24K" -> gold24kRate
                    "22K" -> gold22kRate
                    "18K" -> gold18kRate
                    else -> gold22kRate
                }
            }
            "silver" -> silverRate
            "platinum" -> gold24kRate * 1.25 // Platinum is assumed 25% premium over gold 24k as an elegant approximation
            else -> 0.0
        }
        val metalValue = baseRate * weight
        val making = if (chargeType == "Per Gram") makingCharges * weight else makingCharges
        return metalValue + making
    }
}

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,         // can use standard lookups
    val email: String? = null,
    val address: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String, // e.g., "INV-2026-0001"
    val customerName: String,
    val customerPhone: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalWeight: Double,
    val goldRateAtSale: Double,  // 22k rate at time of sale
    val subtotal: Double,
    val discount: Double,
    val cgst: Double,            // tax amount
    val sgst: Double,            // tax amount
    val grandTotal: Double,
    val itemsJson: String,       // JSON list of sold items
    val shopName: String         // shop name for multi-shop print
)

@Entity(tableName = "metal_rates")
data class MetalRate(
    @PrimaryKey val id: Int = 1, // Always 1 for singular config
    val gold24k: Double,
    val gold22k: Double,
    val gold18k: Double,
    val silver: Double,
    val currency: String = "USD",
    val lastUpdated: Long = System.currentTimeMillis()
)
