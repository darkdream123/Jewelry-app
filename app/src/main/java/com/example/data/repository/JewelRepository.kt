package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow

class JewelRepository(
    private val jewelDao: JewelDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val metalRateDao: MetalRateDao,
    private val galleryPhotoDao: GalleryPhotoDao
) {
    // Gallery Photos Flow
    val allPhotos: Flow<List<GalleryPhoto>> = galleryPhotoDao.getAllPhotos()

    suspend fun insertPhoto(photo: GalleryPhoto) {
        galleryPhotoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: GalleryPhoto) {
        galleryPhotoDao.deletePhoto(photo)
    }

    suspend fun deletePhotoById(id: Int) {
        galleryPhotoDao.deletePhotoById(id)
    }

    // Inventory Flow
    val allItems: Flow<List<JewelItem>> = jewelDao.getAllItems()
    val availableItems: Flow<List<JewelItem>> = jewelDao.getAvailableItems()

    suspend fun getItemByCode(code: String): JewelItem? {
        return jewelDao.getItemByCode(code)
    }

    suspend fun insertItem(item: JewelItem) {
        jewelDao.insertItem(item)
    }

    suspend fun updateItem(item: JewelItem) {
        jewelDao.updateItem(item)
    }

    suspend fun deleteItem(item: JewelItem) {
        jewelDao.deleteItem(item)
    }

    suspend fun deleteItemById(id: Int) {
        jewelDao.deleteItemById(id)
    }

    suspend fun updateStockCount(id: Int, newCount: Int) {
        jewelDao.updateStockCount(id, newCount)
    }

    // Customer FLow
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    suspend fun getCustomerByPhone(phone: String): Customer? {
        return customerDao.getCustomerByPhone(phone)
    }

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun deleteCustomerById(id: Int) {
        customerDao.deleteCustomerById(id)
    }

    // Invoices Flow
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    suspend fun getInvoiceById(id: Int): Invoice? {
        return invoiceDao.getInvoiceById(id)
    }

    fun getInvoicesByCustomer(phone: String): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByCustomer(phone)
    }

    suspend fun insertInvoice(invoice: Invoice): Invoice {
        val rawId = invoiceDao.insertInvoice(invoice)
        return invoice.copy(id = rawId.toInt())
    }

    suspend fun getInvoiceCount(): Int {
        return invoiceDao.getInvoiceCount()
    }

    // Metal Rates Flow
    val liveRateFlow: Flow<MetalRate?> = metalRateDao.getLiveRateFlow()

    suspend fun getLiveRate(): MetalRate? {
        return metalRateDao.getLiveRate()
    }

    suspend fun updateLiveRates(rates: MetalRate) {
        metalRateDao.updateRates(rates)
    }
}
