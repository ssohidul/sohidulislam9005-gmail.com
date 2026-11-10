package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity named 'Product' for local SQLite storage
 * in the inventory management system.
 *
 * Stores essential inventory details including:
 * - name: Name/title of the product
 * - barcode: Barcode or SKU code for fast scanning/lookup
 * - price: Selling price to customers
 * - stockQuantity: Available stock in inventory
 * - Additional retail fields: buyPrice, category, unit, lowStockThreshold, and timestamps.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "barcode")
    val barcode: String = "",

    @ColumnInfo(name = "price")
    val price: Double = 0.0,

    @ColumnInfo(name = "buy_price")
    val buyPrice: Double = 0.0,

    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Double = 0.0,

    @ColumnInfo(name = "category")
    val category: String = "সাধারণ",

    @ColumnInfo(name = "unit")
    val unit: String = "পিস",

    @ColumnInfo(name = "low_stock_threshold")
    val lowStockThreshold: Double = 5.0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
