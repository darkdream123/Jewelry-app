package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [JewelItem::class, Customer::class, Invoice::class, MetalRate::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jewelDao(): JewelDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun metalRateDao(): MetalRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_jewel_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        database.jewelDao(),
                        database.customerDao(),
                        database.metalRateDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(
            jewelDao: JewelDao,
            customerDao: CustomerDao,
            metalRateDao: MetalRateDao
        ) {
            // 1. Initialise Live Metal Rates (Realistic standard prices in USD per gram)
            metalRateDao.updateRates(
                MetalRate(
                    id = 1,
                    gold24k = 76.50,
                    gold22k = 71.20,
                    gold18k = 59.80,
                    silver = 0.95,
                    currency = "USD"
                )
            )

            // 2. Pre-populate elegant Inventory Items
            val initialItems = listOf(
                JewelItem(
                    code = "G-RN01",
                    name = "Royal Solitaire Gold Ring",
                    metalType = "Gold",
                    purity = "22K",
                    weight = 6.5,
                    makingCharges = 12.00,
                    chargeType = "Per Gram",
                    iconType = "Ring",
                    stockCount = 4
                ),
                JewelItem(
                    code = "G-NC02",
                    name = "Infinity Bridal Necklace",
                    metalType = "Gold",
                    purity = "22K",
                    weight = 24.8,
                    makingCharges = 150.00,
                    chargeType = "Flat Rate",
                    iconType = "Necklace",
                    stockCount = 2
                ),
                JewelItem(
                    code = "G-ER05",
                    name = "Filigree Gold Chandelier Earrings",
                    metalType = "Gold",
                    purity = "18K",
                    weight = 8.2,
                    makingCharges = 8.50,
                    chargeType = "Per Gram",
                    iconType = "Earrings",
                    stockCount = 6
                ),
                JewelItem(
                    code = "S-BR03",
                    name = "Sterling Premium Bracelet",
                    metalType = "Silver",
                    purity = "925 Silver",
                    weight = 15.4,
                    makingCharges = 20.00,
                    chargeType = "Flat Rate",
                    iconType = "Bracelet",
                    stockCount = 10
                ),
                JewelItem(
                    code = "P-RN08",
                    name = "Vows Platinum Eternity Ring",
                    metalType = "Platinum",
                    purity = "950 Pt",
                    weight = 5.2,
                    makingCharges = 18.00,
                    chargeType = "Per Gram",
                    iconType = "Ring",
                    stockCount = 3
                ),
                JewelItem(
                    code = "G-BG12",
                    name = "Antiquity Karat Gold Bangle",
                    metalType = "Gold",
                    purity = "22K",
                    weight = 18.0,
                    makingCharges = 9.50,
                    chargeType = "Per Gram",
                    iconType = "Bangle",
                    stockCount = 5
                )
            )
            for (item in initialItems) {
                jewelDao.insertItem(item)
            }

            // 3. Pre-populate starter Loyal Customers
            val initialCustomers = listOf(
                Customer(
                    name = "Liam Morrison",
                    phone = "555-0198",
                    email = "liam.m@example.com",
                    address = "124 Golden Ridge, Seattle"
                ),
                Customer(
                    name = "Sophia Chen",
                    phone = "555-0142",
                    email = "sophia.chen@example.com",
                    address = "78 Lotus Boulevard, San Francisco"
                ),
                Customer(
                    name = "Emma Thompson",
                    phone = "555-0122",
                    email = "emma.t@example.com",
                    address = "305 Emerald Dr, Boston"
                )
            )
            for (customer in initialCustomers) {
                customerDao.insertCustomer(customer)
            }
        }
    }
}
