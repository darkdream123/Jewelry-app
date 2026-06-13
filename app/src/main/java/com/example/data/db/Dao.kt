package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelDao {
    @Query("SELECT * FROM jewel_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<JewelItem>>

    @Query("SELECT * FROM jewel_items WHERE stockCount > 0 ORDER BY name ASC")
    fun getAvailableItems(): Flow<List<JewelItem>>

    @Query("SELECT * FROM jewel_items WHERE code = :code LIMIT 1")
    suspend fun getItemByCode(code: String): JewelItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JewelItem)

    @Update
    suspend fun updateItem(item: JewelItem)

    @Delete
    suspend fun deleteItem(item: JewelItem)

    @Query("DELETE FROM jewel_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("UPDATE jewel_items SET stockCount = :newCount WHERE id = :id")
    suspend fun updateStockCount(id: Int, newCount: Int)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Query("SELECT * FROM invoices WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getInvoicesByCustomer(phone: String): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int
}

@Dao
interface MetalRateDao {
    @Query("SELECT * FROM metal_rates WHERE id = 1 LIMIT 1")
    fun getLiveRateFlow(): Flow<MetalRate?>

    @Query("SELECT * FROM metal_rates WHERE id = 1 LIMIT 1")
    suspend fun getLiveRate(): MetalRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRates(rate: MetalRate)
}

@Dao
interface GalleryPhotoDao {
    @Query("SELECT * FROM gallery_photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<GalleryPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: GalleryPhoto)

    @Delete
    suspend fun deletePhoto(photo: GalleryPhoto)

    @Query("DELETE FROM gallery_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)
}
