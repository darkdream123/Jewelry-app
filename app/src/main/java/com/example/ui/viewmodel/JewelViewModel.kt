package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // --- Text-to-Speech Voice Support ---
    private var tts: TextToSpeech? = null
    var isTtsReady by mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                // Attempt to support standard formats. Set default language
                tts?.language = Locale("bn", "IN")
            }
        }
    }

    fun speakOut(message: String) {
        if (isTtsReady) {
            // Check if Bengali to adjust pronunciation voice if device supports it, or speak directly
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "JewelTTS")
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    // --- Voice Assistant & Multi-Purpose AI states ---
    var voiceResponseText by mutableStateOf("")
    var isListeningVoice by mutableStateOf(false)
    var showAddJewelDialogVoice by mutableStateOf(false)

    // Floating AI Dashboard Business Chat Window states
    var isChatWindowOpen by mutableStateOf(false)
    val chatMessages = mutableStateListOf<Pair<String, String>>() // "user" to message, "ai" to message
    var chatInputValue by mutableStateOf("")
    var isAisearching by mutableStateOf(false)

    init {
        clearChatHistory()
    }
    
    // Image Upload State
    var uploadedImageBase64 by mutableStateOf<String?>(null)
    var isAnalyzingImage by mutableStateOf(false)
    var aiImageAnalysisResult by mutableStateOf("")

    val galleryPhotos: StateFlow<List<GalleryPhoto>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePhotoToGallery(base64: String, description: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPhoto(
                GalleryPhoto(
                    base64Data = base64,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deletePhotoFromGallery(photo: GalleryPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(photo)
        }
    }

    fun setUploadedImage(base64: String?) {
        uploadedImageBase64 = base64
        if (base64 == null) {
            aiImageAnalysisResult = ""
        } else {
            // Auto save to persistent Showroom Gallery
            val timestampLabel = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            savePhotoToGallery(base64, t("Scanned design $timestampLabel", "স্ক্যানকৃত গহনা $timestampLabel"))
        }
    }

    fun runVoiceCommand(command: String) {
        viewModelScope.launch {
            isListeningVoice = true
            voiceResponseText = t("Processing your voice command...", "আপনার ভয়েস কমান্ড প্রসেস করা হচ্ছে...")
            
            val query = command.lowercase(Locale.ROOT).trim()
            val resultText = when {
                query.contains("add") || query.contains("inventory") || query.contains("যোগ") || query.contains("নিবন্ধন") || query.contains("পণ্য") -> {
                    navigateTo(AppScreen.INVENTORY)
                    showAddJewelDialogVoice = true
                    t(
                        "Navigating to Inventory register and opening the Add Ornament form hands-free!",
                        "ইনভেন্টরি তালিকায় নিয়ে যাওয়া হচ্ছে এবং নতুন গহনা নিবন্ধনের ফর্ম ওপেন করা হয়েছে!"
                    )
                }
                query.contains("pos") || query.contains("ট্যাক্স") || query.contains("বিল") || query.contains("বিক্রয়") || query.contains("sell") -> {
                    navigateTo(AppScreen.POS)
                    t(
                        "Navigating to Point of Sale module to draft custom tax invoice.",
                        "নতুন বিক্রয় রশিদ এবং ট্যাক্স ইনভয়েস তৈরি করতে পস মডিউলে নিয়ে যাওয়া হচ্ছে।"
                    )
                }
                query.contains("customer") || query.contains("কাস্টমার") || query.contains("সিআরএম") || query.contains("গ্রাহক") -> {
                    navigateTo(AppScreen.CUSTOMERS)
                    t(
                        "Opening Live Loyalty Club CRM database.",
                        "লাইভ লয়্যালটি ক্লাব কাস্টমার সিআরএম ডাটাবেজ ওপেন করা হচ্ছে।"
                    )
                }
                query.contains("history") || query.contains("رصيد") || query.contains("রশিদ") || query.contains("মেমো") || query.contains("হিসাব") || query.contains("invoice") -> {
                    navigateTo(AppScreen.INVOICES)
                    t(
                        "Opening previous invoice and cash bill archives.",
                        "পূর্ববর্তী সোনার রশিদ এবং মেমো আরকাইভ ওপেন করা হচ্ছে।"
                    )
                }
                query.contains("rate") || query.contains("দাম") || query.contains("রেট") || query.contains("মূল্য") -> {
                    val rate = liveMetalRate.value
                    if (rate != null) {
                        t(
                            "Today's Gold Rate for 22 Karat is ${rate.currency}${rate.gold22k} per gram, and Silver is ${rate.currency}${rate.silver} per gram.",
                            "আজকের ২২ ক্যারেট সোনার দাম প্রতি গ্রামে ${rate.currency}${rate.gold22k} এবং রূপার দাম প্রতি গ্রামে ${rate.currency}${rate.silver}।"
                        )
                    } else {
                        t("Live gold rates are loading. Please try again soon.", "লাইভ রেট লোড হচ্ছে। অনুগ্রহ করে একটু পরে আবার চেষ্টা করুন।")
                    }
                }
                query.contains("stock") || query.contains("মজুদ") || query.contains("সরাসরি") -> {
                    val list = items.value
                    val lowStock = list.filter { it.stockCount in 1..2 }
                    val oos = list.filter { it.stockCount <= 0 }
                    t(
                        "You have ${list.size} active ornaments in stock. ${lowStock.size} items are running low on stock, and ${oos.size} items are out of stock.",
                        "আপনার স্টকে মোট ${list.size}টি গহনা আছে। ${lowStock.size}টি আইটেমের স্টক প্রায় শেষ, এবং ${oos.size}টি আইটেম সম্পূর্ণ আউট অফ স্টক।"
                    )
                }
                query.contains("sale") || query.contains("বিক্রি") || query.contains("revenue") || query.contains("টাকা") || query.contains("সংগ্রহ") -> {
                    val invs = invoices.value
                    val todayTotal = invs.sumOf { it.grandTotal }
                    t(
                        "We have registered ${invs.size} custom gold bills, with a gross total showroom collection of ${currencySymbol}${String.format("%.2f", todayTotal)}.",
                        "আজ সর্বমোট ${invs.size}টি গোল্ড বিল তৈরি হয়েছে, যার মোট শোরুম কালেকশন ${currencySymbol}${String.format("%.2f", todayTotal)}।"
                    )
                }
                query.contains("hi") || query.contains("hello") || query.contains("হ্যালো") || query.contains("স্বাগতম") -> {
                    t(
                        "Hello Proprietor, I am your smart jewelry voice companion. How can I assist you in Swarnali Shilpaloy showroom today?",
                        "হ্যালো প্রোপ্রাইটার, আমি আপনার রয়্যাল জুয়েলারি ভয়েস সহকারী। আজ স্বর্ণালি শিল্পালয়ে আপনাকে কীভাবে সাহায্য করতে পারি?"
                    )
                }
                else -> {
                    // General fallback queries analyze using Gemini intelligence!
                    val fallbackResponse = GeminiService.generateMarketingTemplate(
                        itemName = query,
                        itemSpecs = "Showroom Inquiry",
                        discount = 0.0,
                        shopName = shopName
                    )
                    fallbackResponse
                }
            }
            
            voiceResponseText = resultText
            isListeningVoice = false
            speakOut(resultText)
        }
    }

    fun analyzeJewelPhoto(base64: String) {
        viewModelScope.launch {
            isAnalyzingImage = true
            aiImageAnalysisResult = t("Gemini multimodality model is examining the ornament craft detail...", "জেমিনি এআই আপনার আপলোড করা গহনার নিখুঁত কারুকাজ বিশ্লেষণ করছে...")
            val feedback = GeminiService.analyzeJewelImage(base64)
            aiImageAnalysisResult = feedback
            isAnalyzingImage = false
            speakOut(t("Analysis complete!", "গহনা বিশ্লেষণ সফলভাবে সম্পন্ন হয়েছে!"))
        }
    }

    // --- Navigation & Auth ---
    var currentLanguage by mutableStateOf("bn") // Defaults to "bn" (Bengali) as requested beautifully!
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

    var shopName by mutableStateOf("স্বর্ণালি শিল্পালয়")
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

    var currencySymbol by mutableStateOf("টাকা")
        private set

    fun login(name: String, currency: String = "টাকা") {
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

    var segmentationCampaigns by mutableStateOf("No campaigns generated yet. Tap 'Refresh Campaigns' below to run Gemini behavior analytics.")
        private set
    var isGeneratingCampaigns by mutableStateOf(false)
        private set

    fun triggerCampaignsGeneration() {
        val customersList = customers.value
        val invoicesList = invoices.value

        val (customersStr, invoicesStr) = if (customersList.isEmpty()) {
            "Empty Customer Database" to "No Invoice Logs Available"
        } else {
            val clients = customersList.take(20).joinToString("\n") { c ->
                "ID: ${c.id}, Name: ${c.name}, Phone: ${c.phone}, Address: ${c.address ?: "N/A"}"
            }
            val sales = invoicesList.take(30).joinToString("\n") { inv ->
                "Customer: ${inv.customerName}, Amount: ${currencySymbol}${inv.grandTotal}, Gold Rate: ${inv.goldRateAtSale}/g"
            }
            clients to sales
        }

        isGeneratingCampaigns = true
        viewModelScope.launch {
            val feedback = GeminiService.generateSegmentationMarketingCampaigns(
                customersJsonStr = customersStr,
                recentInvoicesSummary = invoicesStr,
                shopName = shopName
            )
            withContext(Dispatchers.Main) {
                segmentationCampaigns = feedback
                isGeneratingCampaigns = false
            }
        }
    }

    // --- PDF Export and Gemini Interactive AI Assistant ---
    fun exportReportToPdf() {
        val context = getApplication<Application>().applicationContext
        val customersList = customers.value
        val itemsList = items.value
        val rates = liveMetalRate.value

        val pdfDocument = android.graphics.pdf.PdfDocument()
        val titlePaint = android.graphics.Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#855B1A") // Gold brown brand color
        }
        val headerPaint = android.graphics.Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }
        val textPaint = android.graphics.Paint().apply {
            textSize = 10f
            color = android.graphics.Color.BLACK
        }
        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }

        // --- PAGE 1: CLIENT CRM REGISTER ---
        val pageInfo1 = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        canvas1.drawText("SWARNALI SHILPALOY - BUSINESS INTELLIGENCE", 50f, 50f, titlePaint)
        canvas1.drawText("CLIENT CRM DATABASE REPORT", 50f, 75f, headerPaint)
        canvas1.drawLine(50f, 85f, 545f, 85f, linePaint)

        val timestampLabel = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas1.drawText("Report Generated: $timestampLabel", 50f, 105f, textPaint)
        canvas1.drawText("Total Loyalty Customers: ${customersList.size}", 50f, 120f, textPaint)
        if (rates != null) {
            canvas1.drawText("Live Rate gold 22K reference: ${rates.gold22k} ${rates.currency}/g", 50f, 135f, textPaint)
        }

        canvas1.drawLine(50f, 155f, 545f, 155f, linePaint)
        canvas1.drawText("CUSTOMER NAME", 60f, 170f, headerPaint)
        canvas1.drawText("PHONE NUMBER", 230f, 170f, headerPaint)
        canvas1.drawText("ADDRESS DETAILS", 380f, 170f, headerPaint)
        canvas1.drawLine(50f, 180f, 545f, 180f, linePaint)

        var currentY = 200f
        for (customer in customersList.take(25)) {
            if (currentY > 780f) break
            canvas1.drawText(customer.name, 60f, currentY, textPaint)
            canvas1.drawText(customer.phone, 230f, currentY, textPaint)
            canvas1.drawText(customer.address ?: "N/A", 380f, currentY, textPaint)
            canvas1.drawLine(50f, currentY + 8f, 545f, currentY + 8f, linePaint)
            currentY += 22f
        }
        
        pdfDocument.finishPage(page1)

        // --- PAGE 2: STOCK CATALOGUE ---
        val pageInfo2 = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        canvas2.drawText("SWARNALI SHILPALOY - INVENTORY REGISTER", 50f, 50f, titlePaint)
        canvas2.drawText("SHOWROOM COMPREHENSIVE STOCK CATALOGUE", 50f, 75f, headerPaint)
        canvas2.drawLine(50f, 85f, 545f, 85f, linePaint)

        canvas2.drawText("Total Registered Categories: ${itemsList.size}", 50f, 105f, textPaint)
        val totalWeight = itemsList.sumOf { it.weight * it.stockCount }
        canvas2.drawText(String.format("Calculated Total Weight: %.3fg", totalWeight), 50f, 120f, textPaint)

        canvas2.drawLine(50f, 140f, 545f, 140f, linePaint)
        canvas2.drawText("CODE", 60f, 155f, headerPaint)
        canvas2.drawText("ORNAMENT NAME", 130f, 155f, headerPaint)
        canvas2.drawText("PURITY", 320f, 155f, headerPaint)
        canvas2.drawText("WEIGHT", 390f, 155f, headerPaint)
        canvas2.drawText("STOCK", 470f, 155f, headerPaint)
        canvas2.drawLine(50f, 165f, 545f, 165f, linePaint)

        currentY = 185f
        for (item in itemsList.take(25)) {
            if (currentY > 780f) break
            canvas2.drawText(item.code, 60f, currentY, textPaint)
            canvas2.drawText(item.name, 130f, currentY, textPaint)
            canvas2.drawText(item.purity, 320f, currentY, textPaint)
            canvas2.drawText(String.format("%.2f g", item.weight), 390f, currentY, textPaint)
            canvas2.drawText("${item.stockCount} pcs", 470f, currentY, textPaint)
            canvas2.drawLine(50f, currentY + 8f, 545f, currentY + 8f, linePaint)
            currentY += 22f
        }

        pdfDocument.finishPage(page2)

        try {
            val cacheDir = context.cacheDir
            val filename = "Swarnali_Shilpaloy_Report.pdf"
            val file = java.io.File(cacheDir, filename)
            val outputStream = java.io.FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)

            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_TITLE, "Swarnali Shilpaloy Business Intelligence PDF")
                type = "application/pdf"
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = android.content.Intent.createChooser(sendIntent, "Share Professional Business PDF").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
            speakOut(t("PDF generated successfully!", "পিডিএফ রিপোর্ট সফলভাবে তৈরি হয়েছে এবং শেয়ার অপশন ওপেন হয়েছে!"))
        } catch (e: Exception) {
            e.printStackTrace()
            speakOut(t("PDF Generation failed: ${e.localizedMessage}", "পিডিএফ তৈরি ব্যার্থ হয়েছে।"))
        }
    }

    fun sendChatQuery(query: String) {
        if (query.isBlank()) return
        chatMessages.add("user" to query)
        val userQuery = query
        chatInputValue = ""
        isAisearching = true

        val invoicesList = invoices.value
        val itemsList = items.value

        val currentHistorySummary = if (invoicesList.isEmpty()) {
            "No sales invoices exist yet."
        } else {
            invoicesList.take(20).joinToString("\n") { inv ->
                "Invoice ${inv.invoiceNumber} -> Customer: ${inv.customerName}, Total Paid: ${currencySymbol}${inv.grandTotal}, Gold Rate: ${inv.goldRateAtSale}/g"
            }
        }

        val currentInventorySummary = if (itemsList.isEmpty()) {
            "No inventory registered."
        } else {
            itemsList.take(20).joinToString("\n") { item ->
                "Code: ${item.code}, Name: ${item.name}, Metal: ${item.metalType}, Purity: ${item.purity}, Stock: ${item.stockCount} pcs, Weight: ${item.weight}g"
            }
        }

        viewModelScope.launch {
            try {
                val response = GeminiService.askBusinessAssistant(
                    currentHistorySummary = currentHistorySummary,
                    currentInventorySummary = currentInventorySummary,
                    query = userQuery,
                    shopName = shopName
                )
                withContext(Dispatchers.Main) {
                    chatMessages.add("ai" to response)
                    isAisearching = false
                    speakOut(t("I have analyzed your query.", "আমি আপনার প্রশ্নের তথ্যটি জেনেছি।"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatMessages.add("ai" to "Error generating analysis: ${e.localizedMessage}")
                    isAisearching = false
                }
            }
        }
    }

    fun clearChatHistory() {
        chatMessages.clear()
        chatMessages.add("ai" to t(
            "Hello Shop Owner! I am your interactive AI Retail Consultant. Ask me details about highest selling categories, current shop stock levels, or make me draft festive promotional greetings like Diwali messages!",
            "স্বাগতম ম্যানেজার! আমি আপনার সরাসরি জেমিনি এআই সহকারী। আজকের সোনা-রূপার স্টক বিশ্লেষণ বা দীপাবলি, ঈদ ইত্যাদি উৎসবের আকর্ষণীয় প্রমোশনাল মেসেজ লিখে দিতে আমাকে বলুন!"
        ))
    }

    // --- Offline Backups Exports to CSV & JSON ---
    fun shareTextData(title: String, filename: String, content: String) {
        val context = getApplication<Application>().applicationContext
        try {
            val cacheDir = context.cacheDir
            val file = java.io.File(cacheDir, filename)
            file.writeText(content)

            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TITLE, title)
                putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                putExtra(android.content.Intent.EXTRA_TEXT, content)
                type = "text/plain"
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, title).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
            speakOut(t("Exporting backup completed successfully!", "ব্যাকআপ ডাটা শেয়ারিং শুরু হয়েছে!"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportInventoryToCsv() {
        val itemList = items.value
        val sb = StringBuilder()
        sb.append("ID,Code,Name,MetalType,Purity,Weight(g),MakingCharges,ChargeType,StockCount\n")
        for (item in itemList) {
            sb.append("${item.id},\"${item.code}\",\"${item.name}\",\"${item.metalType}\",\"${item.purity}\",${item.weight},${item.makingCharges},\"${item.chargeType}\",${item.stockCount}\n")
        }
        shareTextData("Inventory Backup CSV", "inventory_backup.csv", sb.toString())
    }

    fun exportInventoryToJson() {
        val itemList = items.value
        val sb = StringBuilder()
        sb.append("[\n")
        itemList.forEachIndexed { i, item ->
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"code\": \"${item.code}\",\n")
            sb.append("    \"name\": \"${item.name}\",\n")
            sb.append("    \"metalType\": \"${item.metalType}\",\n")
            sb.append("    \"purity\": \"${item.purity}\",\n")
            sb.append("    \"weight\": ${item.weight},\n")
            sb.append("    \"makingCharges\": ${item.makingCharges},\n")
            sb.append("    \"chargeType\": \"${item.chargeType}\",\n")
            sb.append("    \"stockCount\": ${item.stockCount}\n")
            sb.append("  }${if (i < itemList.size - 1) "," else ""}\n")
        }
        sb.append("]")
        shareTextData("Inventory Backup JSON", "inventory_backup.json", sb.toString())
    }

    fun exportCustomersToCsv() {
        val customerList = customers.value
        val sb = StringBuilder()
        sb.append("ID,Name,Phone,Email,Address\n")
        for (c in customerList) {
            sb.append("${c.id},\"${c.name}\",\"${c.phone}\",\"${c.email ?: ""}\",\"${c.address ?: ""}\"\n")
        }
        shareTextData("Customers Backup CSV", "customers_backup.csv", sb.toString())
    }

    fun exportCustomersToJson() {
        val customerList = customers.value
        val sb = StringBuilder()
        sb.append("[\n")
        customerList.forEachIndexed { i, c ->
            sb.append("  {\n")
            sb.append("    \"id\": ${c.id},\n")
            sb.append("    \"name\": \"${c.name}\",\n")
            sb.append("    \"phone\": \"${c.phone}\",\n")
            sb.append("    \"email\": \"${c.email ?: ""}\",\n")
            sb.append("    \"address\": \"${c.address ?: ""}\"\n")
            sb.append("  }${if (i < customerList.size - 1) "," else ""}\n")
        }
        sb.append("]")
        shareTextData("Customers Backup JSON", "customers_backup.json", sb.toString())
    }

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
        stockCount: Int,
        imageUrl: String? = null
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
                stockCount = stockCount,
                imageUrl = imageUrl
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
    fun addCustomer(name: String, phone: String, email: String? = null, address: String? = null, photoUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val exist = repository.getCustomerByPhone(phone.trim())
            if (exist == null) {
                repository.insertCustomer(
                    Customer(
                        name = name.trim(),
                        phone = phone.trim(),
                        email = email?.trim(),
                        address = address?.trim(),
                        photoUrl = photoUrl
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
    fun updateRates(gold24k: Double, gold22k: Double, gold21k: Double, gold18k: Double, silver: Double, currency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = MetalRate(
                id = 1,
                gold24k = gold24k,
                gold22k = gold22k,
                gold21k = gold21k,
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
                gold21kRate = rates.gold21k,
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
        val rates = liveMetalRate.value ?: MetalRate(1, 11050.0, 10130.0, 9670.0, 8290.0, 180.0, "টাকা")
        val finalPrice = item.calculatePrice(
            gold24kRate = rates.gold24k,
            gold22kRate = rates.gold22k,
            gold21kRate = rates.gold21k,
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
        val rates = liveMetalRate.value ?: MetalRate(1, 11050.0, 10130.0, 9670.0, 8290.0, 180.0, "টাকা")
        val ratesPriceStr = "Gold 24K: ${currencySymbol}${rates.gold24k}/g, 22K: ${currencySymbol}${rates.gold22k}/g, 21K: ${currencySymbol}${rates.gold21k}/g, Silver: ${currencySymbol}${rates.silver}/g"

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
