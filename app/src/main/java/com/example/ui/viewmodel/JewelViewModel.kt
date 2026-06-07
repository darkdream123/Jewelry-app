package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.gemini.GeminiService
import com.example.data.repository.JewelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CartItem(
    val item: JewelItem,
    val selectedWeight: Double,
    val finalPrice: Double
)

enum class AppScreen {
    LOGIN,
    DASHBOARD,
    INVENTORY,
    POS,
    CUSTOMERS,
    INVOICES,
    AI_MARKETING,
    RATE_MANAGER,
    INVOICE_DETAIL
}

class JewelViewModel(
    application: Application,
    private val repository: JewelRepository
) : AndroidViewModel(application) {

    // --- Navigation & Auth ---
    var currentLanguage by mutableStateOf("en") // "en" for English, "bn" for Bangla
        private set

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == "en") "bn" else "en"
    }

    fun t(en: String, bn: String): String {
        return if (currentLanguage == "bn") bn else en
    }

    var currentScreen by mutableStateOf(AppScreen.LOGIN)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var shopName by mutableStateOf("Swarnali Shilpaloy")
        private set

    fun getTranslatedShopName(): String {
        return if (shopName.equals("Swarnali Shilpaloy", ignoreCase = true) || 
                   shopName.equals("স্বর্ণালি শিল্পালয়", ignoreCase = true) || 
                   shopName.equals("Royal Gems Co.", ignoreCase = true) || 
                   shopName.equals("Smart Jewel Showroom", ignoreCase = true) ||
                   shopName.isBlank()) {
            t("Swarnali Shilpaloy", "স্বর্ণালি শিল্পালয়")
        } else {
            shopName
        }
    }

    var currencySymbol by mutableStateOf("$")
        private set

    fun login(name: String, currency: String = "$") {
        if (name.isNotBlank()) {
            shopName = name
            currencySymbol = currency
            isLoggedIn = true
            currentScreen = AppScreen.DASHBOARD
            // Load insights on login
            triggerInsightsGeneration()
        }
    }

    fun logout() {
        isLoggedIn = false
        currentScreen = AppScreen.LOGIN
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        if (screen == AppScreen.DASHBOARD) {
            triggerInsightsGeneration()
        }
    }

    // --- Observable Flows from Repository ---
    val items: StateFlow<List<JewelItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableItems: StateFlow<List<JewelItem>> = repository.availableItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveMetalRate: StateFlow<MetalRate?> = repository.liveRateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Live Pricing Calculations in Cart ---
    val cartItems = mutableStateListOf<CartItem>()
    var cartCustomer by mutableStateOf<Customer?>(null)
    var discountPercent by mutableStateOf(0.0)
    var cgstPercent by mutableStateOf(1.5) // Standard 1.5% CGST
    var sgstPercent by mutableStateOf(1.5) // Standard 1.5% SGST

    var selectedInvoice by mutableStateOf<Invoice?>(null)

    // Derived cart totals
    val subtotal: Double
        get() = cartItems.sumOf { it.finalPrice }

    val discountAmount: Double
        get() = subtotal * (discountPercent / 100.0)

    val taxableValue: Double
        get() = subtotal - discountAmount

    val cgstAmount: Double
        get() = taxableValue * (cgstPercent / 100.0)

    val sgstAmount: Double
        get() = taxableValue * (sgstPercent / 100.0)

    val grandTotal: Double
        get() = taxableValue + cgstAmount + sgstAmount

    val totalWeight: Double
        get() = cartItems.sumOf { it.selectedWeight }

    // --- Gemini state ---
    var marketingText by mutableStateOf("")
        private set
    var isGeneratingMarketing by mutableStateOf(false)
        private set

    var businessInsights by mutableStateOf("Ready to receive business performance signals...")
        private set
    var isGeneratingInsights by mutableStateOf(false)
        private set

    // --- Inventory methods ---
    fun addInventoryItem(
        code: String,
        name: String,
        metalType: String,
        purity: String,
        weight: Double,
        makingCharges: Double,
        chargeType: String,
        iconType: String,
        stockCount: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = JewelItem(
                code = code.trim(),
                name = name.trim(),
                metalType = metalType,
                purity = purity,
                weight = weight,
                makingCharges = makingCharges,
                chargeType = chargeType,
                iconType = iconType,
                stockCount = stockCount
            )
            repository.insertItem(item)
        }
    }

    fun updateInventoryItem(item: JewelItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item)
        }
    }

    fun deleteInventoryItem(item: JewelItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item)
        }
    }

    // --- Customer CRM methods ---
    fun addCustomer(name: String, phone: String, email: String? = null, address: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val exist = repository.getCustomerByPhone(phone.trim())
            if (exist == null) {
                repository.insertCustomer(
                    Customer(
                        name = name.trim(),
                        phone = phone.trim(),
                        email = email?.trim(),
                        address = address?.trim()
                    )
                )
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomer(customer)
        }
    }

    // --- Metal Rate settings ---
    fun updateRates(gold24k: Double, gold22k: Double, gold18k: Double, silver: Double, currency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = MetalRate(
                id = 1,
                gold24k = gold24k,
                gold22k = gold22k,
                gold18k = gold18k,
                silver = silver,
                currency = currency,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateLiveRates(updated)
            
            // Re-calculate cart pricing based on new rates
            withContext(Dispatchers.Main) {
                recalculateCartPrices(updated)
            }
        }
    }

    private fun recalculateCartPrices(rates: MetalRate) {
        val updatedCart = cartItems.map { cartItem ->
            val newPrice = cartItem.item.calculatePrice(
                gold24kRate = rates.gold24k,
                gold22kRate = rates.gold22k,
                gold18kRate = rates.gold18k,
                silverRate = rates.silver
            )
            cartItem.copy(finalPrice = newPrice)
        }
        cartItems.clear()
        cartItems.addAll(updatedCart)
    }

    // --- POS & Checkout Operations ---
    fun addJewelToCart(item: JewelItem) {
        val rates = liveMetalRate.value ?: MetalRate(1, 76.5, 71.2, 59.8, 0.95, "USD")
        val finalPrice = item.calculatePrice(
            gold24kRate = rates.gold24k,
            gold22kRate = rates.gold22k,
            gold18kRate = rates.gold18k,
            silverRate = rates.silver
        )
        // If already in cart, don't duplicate simple jewel unit
        if (cartItems.none { it.item.id == item.id }) {
            cartItems.add(CartItem(item, item.weight, finalPrice))
        }
    }

    fun removeCartItem(cartItem: CartItem) {
        cartItems.remove(cartItem)
    }

    fun clearCart() {
        cartItems.clear()
        cartCustomer = null
        discountPercent = 0.0
    }

    fun checkoutCart() {
        if (cartItems.isEmpty()) return

        val customerName = cartCustomer?.name ?: "Walk-in Customer"
        val customerPhone = cartCustomer?.phone ?: "N/A"

        viewModelScope.launch(Dispatchers.IO) {
            // Calculate sequential invoice code
            val count = repository.getInvoiceCount()
            val year = 2026 // Applet timezone or fixed
            val invoiceCode = "INV-$year-${(count + 1).toString().padStart(4, '0')}"

            // Build JSON list of items purchased
            val itemsDescriptor = cartItems.joinToString(", ") {
                "${it.item.name} (${it.item.purity} ${it.item.weight}g)"
            }

            val curRates = liveMetalRate.value
            val invoiceObj = Invoice(
                invoiceNumber = invoiceCode,
                customerName = customerName,
                customerPhone = customerPhone,
                totalWeight = totalWeight,
                goldRateAtSale = curRates?.gold22k ?: 71.2,
                subtotal = subtotal,
                discount = discountAmount,
                cgst = cgstAmount,
                sgst = sgstAmount,
                grandTotal = grandTotal,
                itemsJson = itemsDescriptor,
                shopName = shopName
            )

            // Save Invoice
            val savedInvoice = repository.insertInvoice(invoiceObj)

            // Decrement Stock Count of products sold
            for (cartUnit in cartItems) {
                val currentStock = cartUnit.item.stockCount
                repository.updateStockCount(cartUnit.item.id, (currentStock - 1).coerceAtLeast(0))
            }

            // Move to Success invoices screen
            withContext(Dispatchers.Main) {
                selectedInvoice = savedInvoice
                clearCart()
                currentScreen = AppScreen.INVOICE_DETAIL
            }
        }
    }

    // --- AI Integration Methods ---
    fun generateMarketingSms(item: JewelItem, discountPct: Double) {
        isGeneratingMarketing = true
        marketingText = "Consulting AI Copywriters..."
        
        viewModelScope.launch {
            val specs = "${item.metalType} (${item.purity}), Weight details: ${item.weight}g"
            val text = GeminiService.generateMarketingTemplate(
                itemName = item.name,
                itemSpecs = specs,
                discount = discountPct,
                shopName = shopName,
                customerName = cartCustomer?.name // Personalized if custom client selected
            )
            withContext(Dispatchers.Main) {
                marketingText = text
                isGeneratingMarketing = false
                currentScreen = AppScreen.AI_MARKETING
            }
        }
    }

    fun triggerInsightsGeneration() {
        val invoicesList = invoices.value
        val todayInvoiceCount = invoicesList.size
        val totalRevenue = invoicesList.sumOf { it.grandTotal }
        val sampleTopItem = items.value.firstOrNull()?.name ?: "Fine Diamond Bands"
        val rates = liveMetalRate.value ?: MetalRate(1, 76.5, 71.2, 59.8, 0.95, "USD")
        val ratesPriceStr = "Gold 24K: ${currencySymbol}${rates.gold24k}/g, 22K: ${currencySymbol}${rates.gold22k}/g, Silver: ${currencySymbol}${rates.silver}/g"

        isGeneratingInsights = true
        viewModelScope.launch {
            val feedback = GeminiService.getBusinessInsights(
                salesCount = todayInvoiceCount,
                totalRevenue = totalRevenue,
                topItem = sampleTopItem,
                shopName = shopName,
                currentRates = ratesPriceStr
            )
            withContext(Dispatchers.Main) {
                businessInsights = feedback
                isGeneratingInsights = false
            }
        }
    }
}

// --- ViewModel Factory to inject Repository ---

class JewelViewModelFactory(
    private val application: Application,
    private val repository: JewelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JewelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JewelViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
