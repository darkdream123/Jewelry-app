package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.Customer
import com.example.data.db.Invoice
import com.example.data.db.JewelItem
import com.example.data.db.MetalRate
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.JewelViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- Custom Luxury Styling Constants ---
val LuxuryDarkBg = Color(0xFF0F0F11)       // Intense Charcoal / Near-black
val LuxurySurface = Color(0xFF17171C)      // Warm Obsidian
val LuxurySurfaceCard = Color(0xFF22222A)  // Premium Slate Card
val GoldColor = Color(0xFFD4AF37)          // Classic Gold Accent
val LightGold = Color(0xFFF3E5AB)          // Sparkly Gold Tint
val SilverAccent = Color(0xFFA7A7AD)       // Platinum Silver Hue
val CardOutline = Color(0xFF2E2E38)

// --- Helper Icon Selector mapping iconType string to Material Icon ---
@Composable
fun getJewelIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type.lowercase()) {
        "ring" -> Icons.Default.Diamond
        "necklace" -> Icons.Default.CardGiftcard
        "earrings" -> Icons.Default.FavoriteBorder
        "bracelet" -> Icons.Default.Category
        "bangle" -> Icons.Default.Circle
        "chain" -> Icons.Default.Link
        "pendant" -> Icons.Default.Star
        else -> Icons.Default.Diamond
    }
}

// ==========================================
// 1. LOGIN / INITIAL STORE SETUP SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: JewelViewModel) {
    var shopNameText by remember { mutableStateOf("") }
    var selectCurrency by remember { mutableStateOf("$") }
    var initialGoldRate by remember { mutableStateOf("70.0") }
    var initialSilverRate by remember { mutableStateOf("0.95") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Luxury dynamic glow in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldColor.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.2f),
                    radius = size.width * 0.8f
                )
            )
        }

        // Floaty language switcher for pre-auth screen
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .background(LuxurySurface, RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                .clickable { viewModel.toggleLanguage() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language Selector",
                tint = GoldColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (viewModel.currentLanguage == "en") "বাংলা" else "English",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Gem Logo",
                    tint = GoldColor,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "GemPOS Suite",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    color = GoldColor,
                    letterSpacing = 1.sp
                )

                Text(
                    text = viewModel.t("Smart Jewellery Management & Billing", "স্মার্ট জুয়েলারি ম্যানেজমেন্ট ও বিলিং"),
                    fontSize = 12.sp,
                    color = SilverAccent,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = viewModel.t("SETUP SHOWROOM OFFICE", "শোরুম ও অফিস সেটআপ করুন"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldColor,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = shopNameText,
                    onValueChange = { shopNameText = it },
                    label = { Text(viewModel.t("Jewellery Showroom Name", "জুয়েলারি শোরুমের নাম")) },
                    placeholder = { Text(viewModel.t("e.g. Swarnali Shilpaloy", "উদা: স্বর্ণালি শিল্পালয়")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldColor,
                        unfocusedBorderColor = CardOutline,
                        focusedLabelColor = GoldColor
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectCurrency,
                        onValueChange = { selectCurrency = it },
                        label = { Text(viewModel.t("Currency", "মুদ্রা")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldColor,
                            unfocusedBorderColor = CardOutline
                        )
                    )

                    OutlinedTextField(
                        value = initialGoldRate,
                        onValueChange = { initialGoldRate = it },
                        label = { Text(viewModel.t("22K Gold Rate /g", "২২কে স্বর্ণের দাম /গ্রাম")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldColor,
                            unfocusedBorderColor = CardOutline
                        )
                    )
                }

                Button(
                    onClick = {
                        val gr = initialGoldRate.toDoubleOrNull() ?: 71.2
                        val sr = initialSilverRate.toDoubleOrNull() ?: 0.95
                        viewModel.updateRates(
                            gold24k = gr * 1.08, // Approx 24k based on 22k entry
                            gold22k = gr,
                            gold18k = gr * 0.82,
                            silver = sr,
                            currency = selectCurrency
                        )
                        viewModel.login(shopNameText.ifBlank { "Swarnali Shilpaloy" }, selectCurrency)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = viewModel.t("INITIALISE SHOWROOM", "শোরুম শুরু করুন"),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: JewelViewModel) {
    val items by viewModel.items.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val rates by viewModel.liveMetalRate.collectAsState()

    val totalSpent = invoices.sumOf { it.grandTotal }
    val jewelInStocks = items.sumOf { it.stockCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Store context Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = viewModel.getTranslatedShopName().uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = viewModel.t("Dashboard Overview", "ড্যাশবোর্ড ওভারভিউ"),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.RATE_MANAGER) },
                    modifier = Modifier
                        .background(LuxurySurface, CircleShape)
                        .border(BorderStroke(1.dp, CardOutline), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Rates Status",
                        tint = GoldColor
                    )
                }
            }
        }

        // Live Metal Rates Banner
        item {
            rates?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rate Icon",
                                tint = GoldColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.t("LIVE SHOWROOM METAL RATE", "সরাসরি শোরুম মেটাল রেট"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = viewModel.t("Updated", "হালনাগাদ"),
                                fontSize = 10.sp,
                                color = SilverAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(viewModel.t("Gold 24K", "স্বর্ণ ২৪কে"), fontSize = 10.sp, color = SilverAccent)
                                Text("${viewModel.currencySymbol}${r.gold24k}/g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(viewModel.t("Gold 22K (Standard)", "স্বর্ণ ২২কে (মানক)"), fontSize = 10.sp, color = SilverAccent)
                                Text("${viewModel.currencySymbol}${r.gold22k}/g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(viewModel.t("Silver (Fine)", "রূপা (ফাইন)"), fontSize = 10.sp, color = SilverAccent)
                                Text("${viewModel.currencySymbol}${r.silver}/g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = LuxurySurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = viewModel.t("TOTAL SALES REVENUE", "মোট বিক্রয় রাজস্ব"), fontSize = 9.sp, color = SilverAccent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${viewModel.currencySymbol}${String.format("%.2f", totalSpent)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${invoices.size} " + viewModel.t("invoices produced", "টি চালান তৈরি করা হয়েছে"), fontSize = 10.sp, color = SilverAccent)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = LuxurySurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = viewModel.t("CURRENT STOCK", "বর্তমান স্টক"), fontSize = 9.sp, color = SilverAccent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$jewelInStocks " + viewModel.t("Units", "টি সামগ্রী"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${items.size} " + viewModel.t("unique styles", "টি অনন্য ডিজাইন"), fontSize = 10.sp, color = SilverAccent)
                    }
                }
            }
        }

        // Sales visualization chart (Drawn beautifully using Compose Canvas)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("WEEKLY REVENUE ANALYTICS", "সাপ্তাহিক রাজস্ব বিশ্লেষণ"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Helper details: draw elegant chart grid
                            val lineGap = size.height / 4
                            for (i in 0..4) {
                                val y = i * lineGap
                                drawLine(
                                    color = CardOutline,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                            }

                            // Render smooth mock graph representing previous jewelry sales
                            val points = listOf(0.2f, 0.4f, 0.35f, 0.7f, 0.85f, 0.6f, 0.95f)
                            val stepX = size.width / (points.size - 1)
                            val path = Path()

                            points.forEachIndexed { index, weight ->
                                val x = index * stepX
                                val y = size.height - (weight * size.height * 0.8f) // scale down so it doesn't clip
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            // Draw gradient under graph path
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo((points.size - 1) * stepX, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(GoldColor.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )

                            // Stroke line of graph
                            drawPath(
                                path = path,
                                color = GoldColor,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            // Render individual point nodes
                            points.forEachIndexed { index, weight ->
                                val x = index * stepX
                                val y = size.height - (weight * size.height * 0.8f)
                                drawCircle(
                                    color = Color.White,
                                    radius = 5f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = GoldColor,
                                    radius = 8f,
                                    center = Offset(x, y),
                                    style = Stroke(width = 2f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = if (viewModel.currentLanguage == "bn") {
                            listOf("সোম", "মঙ্গল", "বুধ", "বৃহ:", "শুক্র", "শনি", "রবি")
                        } else {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        }
                        for (lbl in labels) {
                            Text(text = lbl, fontSize = 10.sp, color = SilverAccent)
                        }
                    }
                }
            }
        }

        // Gemini AI Executive Coach Business Insights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "AI Expert",
                            tint = GoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.t("AI BUSINESS INTELLIGENCE OFFICE", "এআই ব্যবসায়িক বুদ্ধিমত্তা অফিস"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldColor,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (viewModel.isGeneratingInsights) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = GoldColor
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.triggerInsightsGeneration() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Insights",
                                    tint = GoldColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(LuxurySurfaceCard, RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.businessInsights,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. INVENTORY MANAGEMENT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: JewelViewModel) {
    val items by viewModel.items.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMetalTab by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = LuxuryDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = GoldColor,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = viewModel.t("STOCK CATALOGUE", "স্টক ক্যাটালগ"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldColor,
                letterSpacing = 1.sp
            )
            Text(
                text = viewModel.t("Showroom Inventory", "শোরুম ইনভেন্টরি"),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Low Stock Warning Alert Banner
            val lowStockItems = items.filter { it.stockCount in 1..2 }
            if (lowStockItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { selectedMetalTab = "Low Stock" },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF332010)),
                    border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFF9800).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Low Stock Warning",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.t("LOW STOCK ALERTS DETECTED", "কম স্টক অ্যালার্ট দেখা দিয়েছে"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D)
                            )
                            Text(
                                text = viewModel.t(
                                    "There are ${lowStockItems.size} jewellery articles running extremely low!",
                                    "শোরুমে মাত্র ${lowStockItems.size}টি আইটেমের স্টক প্রায় শেষ পর্যায়ে!"
                                ),
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go",
                            tint = Color(0xFFFF9800).copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(viewModel.t("Search by code or name...", "কোড বা নাম দিয়ে খুঁজুন...")) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldColor,
                    unfocusedBorderColor = CardOutline
                )
            )

            // Filter Tabs with Low Stock toggle integration
            val tabs = listOf("All", "Gold", "Silver", "Platinum", "Low Stock")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedMetalTab == tab
                    val translatedTab = when (tab) {
                        "All" -> viewModel.t("All", "সব")
                        "Gold" -> viewModel.t("Gold", "স্বর্ণ")
                        "Silver" -> viewModel.t("Silver", "রূপা")
                        "Platinum" -> viewModel.t("Platinum", "প্ল্যাটিনাম")
                        "Low Stock" -> if (lowStockItems.isNotEmpty()) {
                            viewModel.t("Low Stock (${lowStockItems.size})", "কম স্টক (${lowStockItems.size})")
                        } else {
                            viewModel.t("Low Stock", "কম স্টক")
                        }
                        else -> tab
                    }
                    val bg = when {
                        isSelected -> GoldColor
                        tab == "Low Stock" && lowStockItems.isNotEmpty() -> Color(0xFF422815)
                        else -> LuxurySurface
                    }
                    val textColor = when {
                        isSelected -> Color.Black
                        tab == "Low Stock" && lowStockItems.isNotEmpty() -> Color(0xFFFFB74D)
                        else -> Color.White
                    }
                    val hasBorder = tab == "Low Stock" && lowStockItems.isNotEmpty() && !isSelected
                    val modifierWithBorder = if (hasBorder) {
                        Modifier.border(BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f)), RoundedCornerShape(30.dp))
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(bg)
                            .then(modifierWithBorder)
                            .clickable { selectedMetalTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = translatedTab,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Filter logic
            val filteredItems = items.filter { item ->
                val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.code.contains(searchQuery, ignoreCase = true)
                val matchesTab = when (selectedMetalTab) {
                    "All" -> true
                    "Low Stock" -> item.stockCount in 1..2
                    else -> item.metalType.equals(selectedMetalTab, ignoreCase = true)
                }
                matchesSearch && matchesTab
            }

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.t("No showroom inventory matching search criteria.", "খোঁজা অনুযায়ী কোনো ইনভেন্টরি পাওয়া যায়নি।"), color = SilverAccent)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { item ->
                        JewelItemCard(item, viewModel)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditJewelDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onSave = { code, name, metal, purity, weight, charges, chargeType, icon, stock ->
                viewModel.addInventoryItem(code, name, metal, purity, weight, charges, chargeType, icon, stock)
                showDialog = false
            }
        )
    }
}

@Composable
fun JewelItemCard(item: JewelItem, viewModel: JewelViewModel) {
    val rates by viewModel.liveMetalRate.collectAsState()
    val calculatedPrice = rates?.let { r ->
        item.calculatePrice(r.gold24k, r.gold22k, r.gold18k, r.silver)
    } ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LuxurySurface),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getJewelIcon(item.iconType),
                        contentDescription = "Type logo",
                        tint = GoldColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                val isOOS = item.stockCount <= 0
                val isLowStock = item.stockCount in 1..2
                val badgeBg = when {
                    isOOS -> Color(0xFF3F1616)
                    isLowStock -> Color(0xFF4C3015)
                    else -> Color(0xFF163F19)
                }
                val badgeText = when {
                    isOOS -> viewModel.t("OOS", "স্টক নেই")
                    isLowStock -> viewModel.t("LOW STOCK", "কম স্টক")
                    else -> viewModel.t("In Stock", "স্টকে আছে")
                }
                val badgeColor = when {
                    isOOS -> Color(0xFFFF5252)
                    isLowStock -> Color(0xFFFFAB40)
                    else -> Color(0xFF69F0AE)
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.code.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GoldColor
            )

            Text(
                text = item.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            Text(
                text = "${item.metalType} • ${item.purity} • ${item.weight}g",
                fontSize = 11.sp,
                color = SilverAccent,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${viewModel.currencySymbol}${String.format("%.2f", calculatedPrice)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = GoldColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = viewModel.t("Qty: ", "পরিমাণ: ") + "${item.stockCount}",
                    fontSize = 11.sp,
                    color = SilverAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.addJewelToCart(item) },
                    enabled = item.stockCount > 0,
                    modifier = Modifier.weight(1f).height(30.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor, disabledContainerColor = CardOutline)
                ) {
                    Text(viewModel.t("ADD CART", "কার্টে যোগ"), fontSize = 9.sp, color = if (item.stockCount > 0) Color.Black else SilverAccent, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.generateMarketingSms(item, 10.0) },
                    modifier = Modifier.size(30.dp).background(LuxurySurfaceCard, RoundedCornerShape(4.dp))
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "AI promo info", tint = GoldColor, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = { viewModel.deleteInventoryItem(item) },
                    modifier = Modifier.size(30.dp).background(Color(0xFF421A1A), RoundedCornerShape(4.dp))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJewelDialog(
    viewModel: JewelViewModel,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, metal: String, purity: String, weight: Double, charges: Double, chargeType: String, icon: String, stock: Int) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var metalType by remember { mutableStateOf("Gold") }
    var purity by remember { mutableStateOf("22K") }
    var weight by remember { mutableStateOf("") }
    var charges by remember { mutableStateOf("") }
    var chargeType by remember { mutableStateOf("Per Gram") }
    var iconType by remember { mutableStateOf("Ring") }
    var stockCount by remember { mutableStateOf("5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = viewModel.t("REGISTER NEW FINE JEWELLERY", "নতুন জুয়েলারি সামগ্রী ইনপুট করুন"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoldColor
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(viewModel.t("Unique Item Code", "আইটেম কোড (অনন্য)")) },
                    placeholder = { Text("e.g. G-R102") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(viewModel.t("Jewel Article Name", "জুয়েলারি সামগ্রীর নাম")) },
                    placeholder = { Text("e.g. Vintage Princess Tiara Ring") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Metal Type tabs
                Text(viewModel.t("Precious Metal Category", "ধাতুর ধরণ / ক্যাটাগরি"), fontSize = 11.sp, color = SilverAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Gold", "Silver", "Platinum").forEach { type ->
                        val active = metalType == type
                        FilterChip(
                            selected = active,
                            onClick = {
                                metalType = type
                                purity = when (type) {
                                    "Gold" -> "22K"
                                    "Silver" -> "925 Silver"
                                    else -> "950 Pt"
                                }
                            },
                            label = { Text(viewModel.t(type, when(type){ "Gold" -> "স্বর্ণ" "Silver" -> "রূপা" else -> "প্ল্যাটিনাম" })) }
                        )
                    }
                }

                OutlinedTextField(
                    value = purity,
                    onValueChange = { purity = it },
                    label = { Text(viewModel.t("Purity Designation", "বিশুদ্ধতার মানদণ্ড (ক্যারেট)")) },
                    placeholder = { Text("e.g. 22K, 18K, 925 Sterling") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text(viewModel.t("Weight (g)", "ওজন (গ্রাম)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stockCount,
                        onValueChange = { stockCount = it },
                        label = { Text(viewModel.t("Stock Qty", "স্টক সংখ্যা")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = charges,
                        onValueChange = { charges = it },
                        label = { Text(viewModel.t("Making Charges", "তৈরি মজুরি চার্জ")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Charge Type Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(viewModel.t("Charge Mode", "চার্জ ধরণ"), fontSize = 10.sp, color = SilverAccent)
                        Row {
                            listOf("Per Gram", "Flat").forEach { mode ->
                                val sel = chargeType.startsWith(mode)
                                InputChip(
                                    selected = sel,
                                    onClick = { chargeType = if (mode == "Flat") "Flat Rate" else "Per Gram" },
                                    label = { Text(viewModel.t(mode, if(mode == "Flat") "ফিক্সড" else "গ্রাম প্রতি"), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                // Silhouette Type Selection Box
                Text(viewModel.t("Design Silhouette Image Mapping", "ডিজাইন আউটলাইন সিলেক্ট করুন"), fontSize = 11.sp, color = SilverAccent)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Ring", "Necklace", "Earrings", "Bracelet", "Bangle", "Chain", "Pendant").forEach { shape ->
                        val isSel = iconType == shape
                        val mappedShape = when(shape){
                            "Ring" -> "আংটি"
                            "Necklace" -> "নেকলেস"
                            "Earrings" -> "দুল"
                            "Bracelet" -> "ব্রেসলেট"
                            "Bangle" -> "বালা"
                            "Chain" -> "চেইন"
                            "Pendant" -> "লকেট"
                            else -> shape
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldColor else LuxurySurfaceCard)
                                .clickable { iconType = shape }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(viewModel.t(shape, mappedShape), color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(viewModel.t("Cancel", "বাতিল"))
                    }

                    Button(
                        onClick = {
                            val w = weight.toDoubleOrNull() ?: 0.0
                            val mc = charges.toDoubleOrNull() ?: 0.0
                            val s = stockCount.toIntOrNull() ?: 1
                            if (code.isNotBlank() && name.isNotBlank() && w > 0.0) {
                                onSave(code, name, metalType, purity, w, mc, chargeType, iconType, s)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(viewModel.t("Register", "নিবন্ধন করুন"), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. POINT OF SALE (POS) SYSTEM SCREEN
// ==========================================
@Composable
fun PosScreen(viewModel: JewelViewModel) {
    val cartItems = viewModel.cartItems
    val customers by viewModel.customers.collectAsState()
    val availableItems by viewModel.availableItems.collectAsState()

    var showCustomerSelect by remember { mutableStateOf(false) }
    var discountInput by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = viewModel.t("SHOWROOM COUNTER", "শোরুম কাউন্টার"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldColor
        )
        Text(
            text = viewModel.t("Point of Sale", "বিক্রয় কেন্দ্র (POS)"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Customer Selection Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(viewModel.t("Assigned Customer CRM", "গ্রাহকের প্রোফাইল (CRM)"), fontSize = 10.sp, color = SilverAccent)
                    Text(
                        text = if(viewModel.cartCustomer?.name == null) viewModel.t("Walk-in Showroom Guest", "শোরুমের সাধারণ ক্রেতা") else viewModel.cartCustomer!!.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (viewModel.cartCustomer != null) GoldColor else Color.White
                    )
                    viewModel.cartCustomer?.phone?.let {
                        Text(viewModel.t("Phone: ", "ফোন: ") + it, fontSize = 11.sp, color = SilverAccent)
                    }
                }

                Button(
                    onClick = { showCustomerSelect = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Text(if (viewModel.cartCustomer == null) viewModel.t("LINK CRM", "গ্রাহক লিঙ্ক করুন") else viewModel.t("CHANGE", "পরিবর্তন"), color = GoldColor, fontSize = 11.sp)
                }
            }
        }

        // Cart List Section
        Text(
            text = viewModel.t("Active Basket (", "সক্রিয় কার্ট (") + "${cartItems.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(LuxurySurface, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Empty", tint = CardOutline, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(viewModel.t("POS billing cart is currently empty.", "বিক্রয় কার্টটি বর্তমানে খালি রয়েছে।"), color = SilverAccent, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.INVENTORY) },
                        border = BorderStroke(1.dp, GoldColor)
                    ) {
                        Text(viewModel.t("BROWSE CATALOGUE", "ক্যাটালগ দেখুন"), color = GoldColor, fontSize = 11.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(LuxurySurface, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuxurySurfaceCard, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.item.code, fontSize = 9.sp, color = GoldColor, fontWeight = FontWeight.Bold)
                            Text(item.item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${item.item.purity} • ${item.item.weight}g", color = SilverAccent, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${viewModel.currencySymbol}${String.format("%.2f", item.finalPrice)}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { viewModel.removeCartItem(item) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calculations & Pricing Desk
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LuxurySurface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("Total Jewel Weight", "অলঙ্কারের মোট ওজন"), fontSize = 12.sp, color = SilverAccent)
                    Text("${viewModel.totalWeight} grams", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("Pre-Tax Subtotal", "কর-পূর্ব সাবটোটাল"), fontSize = 12.sp, color = SilverAccent)
                    Text("${viewModel.currencySymbol}${String.format("%.2f", viewModel.subtotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Editable Discount Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.t("Special Discount (%)", "বিশেষ ছাড় (%)"), fontSize = 12.sp, color = SilverAccent)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = {
                                discountInput = it
                                viewModel.discountPercent = it.toDoubleOrNull() ?: 0.0
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(60.dp).height(44.dp),
                            textStyle = TextStyle(fontSize = 11.sp, color = GoldColor, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldColor,
                                unfocusedBorderColor = CardOutline
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("GST Taxes (3.0% Jeweller Rate)", "জিএসটি ট্যাক্স (৩% স্পেশাল জুয়েলারি হার)"), fontSize = 12.sp, color = SilverAccent)
                    Text(
                        "${viewModel.currencySymbol}${String.format("%.2f", viewModel.cgstAmount + viewModel.sgstAmount)}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Divider(color = CardOutline, modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("GRAND TOTAL PAYABLE", "সর্বমোট প্রদেয়"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                    Text(
                        "${viewModel.currencySymbol}${String.format("%.2f", viewModel.grandTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldColor
                    )
                }

                Button(
                    onClick = { viewModel.checkoutCart() },
                    enabled = cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor, disabledContainerColor = CardOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(viewModel.t("SECURE CHECKOUT & BILL", "সুরক্ষিত পেমেন্ট ও রসিদ তৈরি"), color = if (cartItems.isNotEmpty()) Color.Black else SilverAccent, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
        }
    }

    if (showCustomerSelect) {
        Dialog(onDismissRequest = { showCustomerSelect = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(viewModel.t("LINK CUSTOMER ACCOUNT", "গ্রাহকের খাতা যুক্ত করুন"), color = GoldColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    LazyColumn(
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.cartCustomer = null
                                        showCustomerSelect = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard)
                            ) {
                                Text(viewModel.t("Walk-in General Guest", "সাধারণ ক্রেতা (ওয়াক-ইন)"), color = Color.White, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                            }
                        }

                        items(customers) { c ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.cartCustomer = c
                                        showCustomerSelect = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(c.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(c.phone, color = SilverAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }) {
                            Text(viewModel.t("CREATE NEW CUSTOMER", "নতুন গ্রাহক যুক্ত করুন"), color = GoldColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showCustomerSelect = false }) {
                            Text(viewModel.t("Cancel", "বাতিল"))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. INVOICE SUITE & BILL ARCHIVE DETAIL
// ==========================================
@Composable
fun InvoicesScreen(viewModel: JewelViewModel) {
    val invoices by viewModel.invoices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = viewModel.t("REGISTRY OFFICE", "রেজিস্ট্রি অফিস"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldColor
        )
        Text(
            text = viewModel.t("Showroom Invoice Log", "চালান লগ রেজিস্টার"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(viewModel.t("No bills produced yet. Run POS sales first.", "কোনো রসিদ এখনো তৈরি করা হয়নি। বিক্রয় করুন।"), color = SilverAccent, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(invoices) { inv ->
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(inv.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectedInvoice = inv
                                viewModel.navigateTo(AppScreen.INVOICE_DETAIL)
                             },
                        colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                        border = BorderStroke(1.dp, CardOutline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = GoldColor, fontSize = 15.sp)
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = "Receipt Log", tint = SilverAccent)
                            }

                            Text(text = viewModel.t("Customer: ", "ক্রেতা: ") + inv.customerName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            Text(text = viewModel.t("Items: ", "অলঙ্কারাদি: ") + inv.itemsJson, color = SilverAccent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = dateFormatted, color = SilverAccent, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))

                            Divider(color = CardOutline, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(viewModel.t("Total weight: ", "মোট ওজন: ") + "${inv.totalWeight}g", fontSize = 11.sp, color = SilverAccent)
                                Text(
                                    viewModel.t("Amount: ", "বিল: ") + "${viewModel.currencySymbol}${String.format("%.2f", inv.grandTotal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailScreen(viewModel: JewelViewModel) {
    val inv = viewModel.selectedInvoice ?: return
    val clipManager = LocalClipboardManager.current
    val context = LocalContext.current
    val invoiceDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(inv.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.INVOICES) }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(viewModel.t("BILLING RECEIPT", "বিক্রয় রশিদ (বিল)"), fontWeight = FontWeight.Bold, color = GoldColor)
            Spacer(modifier = Modifier.width(48.dp)) // balanced spacing
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thermal style Invoice paper
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Shop Branding Header
                Text(
                    text = inv.shopName.uppercase(),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = viewModel.t("Tax Invoice - Cash Memo", "ট্যাক্স চালান - ক্যাশ মেমো"),
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "--------------------------------------------------",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // ID details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("Invoice Code:", "চালান নম্বর:"), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    Text(inv.invoiceNumber, fontSize = 11.sp, color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("Date Issued:", "চালানের তারিখ:"), fontSize = 11.sp, color = Color.Black)
                    Text(invoiceDate, fontSize = 11.sp, color = Color.Black)
                }

                Text(
                    text = "--------------------------------------------------",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Customer info
                Text(viewModel.t("BILLED TO:", "ক্রেতার বিবরণ:"), fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Text(viewModel.t("Name: ", "নাম: ") + inv.customerName, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Text(viewModel.t("CRM Phone: ", "মোবাইল নম্বর: ") + inv.customerPhone, fontSize = 11.sp, color = Color.Black)

                Text(
                    text = "--------------------------------------------------",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Item description
                Text(viewModel.t("JEWELLERY LINEUP ARTICLES", "ক্রয়কৃত অলঙ্কারের বিবরণ"), fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                // Output itemization
                Text(text = inv.itemsJson, fontSize = 12.sp, color = Color.Black, lineHeight = 18.sp, fontStyle = FontStyle.Italic)

                Text(
                    text = "--------------------------------------------------",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Breakdown Calculation
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("Items Subtotal:", "অলঙ্কারের সাবটোটাল:"), fontSize = 11.sp, color = Color.Black)
                    Text("${viewModel.currencySymbol}${String.format("%.2f", inv.subtotal)}", fontSize = 11.sp, color = Color.Black)
                }

                if (inv.discount > 0.0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.t("Deducted Offer Discount:", "প্রদত্ত বিশেষ ছাড়:"), fontSize = 11.sp, color = Color.Red)
                        Text("-${viewModel.currencySymbol}${String.format("%.2f", inv.discount)}", fontSize = 11.sp, color = Color.Red)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("CGST (1.50%):", "সিজিএসটি (১.৫০%):"), fontSize = 11.sp, color = Color.Black)
                    Text("${viewModel.currencySymbol}${String.format("%.2f", inv.cgst)}", fontSize = 11.sp, color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(viewModel.t("SGST (1.50%):", "এসজিএসটি (১.৫০%):"), fontSize = 11.sp, color = Color.Black)
                    Text("${viewModel.currencySymbol}${String.format("%.2f", inv.sgst)}", fontSize = 11.sp, color = Color.Black)
                }

                Text(
                    text = "--------------------------------------------------",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.t("GRAND TOTAL PAID:", "সর্বমোট পরিশোধিত:"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(
                        "${viewModel.currencySymbol}${String.format("%.2f", inv.grandTotal)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = viewModel.t("Thank You for Buying Authentic Jewels!", "খাঁটি জুয়েলারি কেনার জন্য আপনাকে ধন্যবাদ!"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = viewModel.t("* This is a computer generated invoice *", "* এটি একটি স্বয়ংক্রিয় কম্পিউটার রসিদ *"),
                    color = Color.Gray,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val billText = "Shop: ${inv.shopName}\nInvoice: ${inv.invoiceNumber}\nCustomer: ${inv.customerName}\nGrand Total: ${viewModel.currencySymbol}${inv.grandTotal}\nThank you for billing."
                    clipManager.setText(AnnotatedString(billText))
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Copy", tint = GoldColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(viewModel.t("COPY INFO", "কপি করুন"), color = GoldColor, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    // Start marketing generation using Gemini right from invoice
                    // Use a placeholder item mockup from invoice descriptive text
                    val itemMock = JewelItem(
                        code = inv.invoiceNumber,
                        name = "Sold Jewelry Set Package",
                        metalType = "Gold",
                        purity = "22K",
                        weight = inv.totalWeight,
                        makingCharges = 0.0,
                        chargeType = "Flat",
                        iconType = "Necklace",
                        stockCount = 1
                    )
                    viewModel.generateMarketingSms(itemMock, 15.0)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
            ) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "AI", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(viewModel.t("AI MARKETING", "এআই মার্কেটিং"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================================
// ==========================================
// 6. CUSTOMER DATABASE SCREEN (CRM)
// ==========================================
@Composable
fun CustomersScreen(viewModel: JewelViewModel) {
    val customers by viewModel.customers.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = LuxuryDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = GoldColor,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = viewModel.t("LOYALTY CLUB CRM", "লয়ালটি ক্লাব সিআরএম"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldColor
            )
            Text(
                text = viewModel.t("Showroom Clientele", "গ্রাহক তালিকা"),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (customers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.t("No clients added yet. Press + to register a regular client.", "এখনো কোনো গ্রাহক যোগ করা হয়নি। নতুন গ্রাহকের অ্যাকাউন্ট খুলতে + চিহ্ন চাপুন।"), color = SilverAccent, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(customers) { c ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                            border = BorderStroke(1.dp, CardOutline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    IconButton(onClick = { viewModel.deleteCustomer(c) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Customer", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Ph", tint = GoldColor, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(c.phone, fontSize = 12.sp, color = SilverAccent)
                                    }

                                    c.email?.let { e ->
                                        if (e.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Email, contentDescription = "Mail", tint = GoldColor, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(e, fontSize = 12.sp, color = SilverAccent)
                                            }
                                        }
                                    }
                                }

                                c.address?.let { addr ->
                                    if (addr.isNotBlank()) {
                                        Text(
                                            text = viewModel.t("Address: ", "ঠিকানা: ") + addr,
                                            fontSize = 11.sp,
                                            color = SilverAccent,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardOutline)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(viewModel.t("REGISTER NEW CRM CUSTOMER", "নতুন গ্রাহক নিবন্ধন করুন"), fontWeight = FontWeight.Bold, color = GoldColor, fontSize = 14.sp)

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(viewModel.t("Customer Name *", "গ্রাহকের নাম *")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(viewModel.t("Mobile Contact *", "মোবাইল নম্বর *")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(viewModel.t("Email Address", "ইমেইল ঠিকানা")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text(viewModel.t("Residential Address", "আবাসিক ঠিকানা")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { showDialog = false }, modifier = Modifier.weight(1f)) {
                            Text(viewModel.t("Cancel", "বাতিল"))
                        }

                        Button(
                            onClick = {
                                if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                    viewModel.addCustomer(nameInput, phoneInput, emailInput, addressInput)
                                    // Clear fields
                                    nameInput = ""
                                    phoneInput = ""
                                    emailInput = ""
                                    addressInput = ""
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(viewModel.t("Save CRM", "সংরক্ষণ করুন"), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. GEMINI AI PROMOTIONAL TEMPLATE SCREEN
// ==========================================
@Composable
fun AiMarketingScreen(viewModel: JewelViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val textToCopy = viewModel.marketingText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.INVENTORY) }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(viewModel.t("AI PROMO DRAFT", "এআই বিজ্ঞাপন খসড়া"), fontWeight = FontWeight.Bold, color = GoldColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "AI Expert", tint = GoldColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewModel.t("GEMINI LUXURY AD COPYWRITER", "জেমিআই লাক্সারি অ্যাড কপিরাইটার"), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = GoldColor)
                }

                if (viewModel.isGeneratingMarketing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(viewModel.t("Styling luxury persuasive draft...", "উচ্চ মানের লাক্সারি বিজ্ঞাপন তৈরি হচ্ছে..."), color = SilverAccent, fontSize = 12.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuxurySurfaceCard, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = textToCopy,
                            fontSize = 14.sp,
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(textToCopy))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Copy", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.t("COPY TEMPLATE", "টেমপ্লেট কপি করুন"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                // Shared copy confirmation trigger can be mapped to custom SMS flow if needed
                                clipboardManager.setText(AnnotatedString(textToCopy))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Mail, contentDescription = "WhatsApp share", tint = GoldColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.t("PRESET READY", "সরাসরি শেয়ার করুন"), color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. METAL RATE MANAGER SCREEN
// ==========================================
@Composable
fun RateManagerScreen(viewModel: JewelViewModel) {
    val currentRate by viewModel.liveMetalRate.collectAsState()

    var gold24kInput by remember { mutableStateOf("") }
    var gold22kInput by remember { mutableStateOf("") }
    var gold18kInput by remember { mutableStateOf("") }
    var silverInput by remember { mutableStateOf("") }
    var currencyInput by remember { mutableStateOf("") }

    // Synchronize inputs on first loads
    LaunchedEffect(currentRate) {
        currentRate?.let {
            gold24kInput = it.gold24k.toString()
            gold22kInput = it.gold22k.toString()
            gold18kInput = it.gold18k.toString()
            silverInput = it.silver.toString()
            currencyInput = it.currency
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = viewModel.t("SHOWROOM CALIBRATOR", "শোরুম ক্যালিবারেশন"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldColor
        )
        Text(
            text = viewModel.t("Live Price Matrix", "লাইভ ধাতুর মূল্য তালিকা"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LuxurySurface),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = viewModel.t("EDIT CURRENT SHOWROOM METAL PRICING", "নির্দিষ্ট ধাতুর শোরুম মূল্য পরিবর্তন"),
                    fontWeight = FontWeight.Bold,
                    color = GoldColor,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = currencyInput,
                    onValueChange = { currencyInput = it },
                    label = { Text(viewModel.t("Currency Indicator Symbol", "মুদ্রার সূচক প্রতীক")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gold24kInput,
                    onValueChange = { gold24kInput = it },
                    label = { Text(viewModel.t("Gold 24K Rate (per gram)", "২৪ ক্যারেট স্বর্ণের মূল্য (প্রতি গ্রাম)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gold22kInput,
                    onValueChange = { gold22kInput = it },
                    label = { Text(viewModel.t("Gold 22K Rate (per gram)", "২২ ক্যারেট স্বর্ণের মূল্য (প্রতি গ্রাম)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gold18kInput,
                    onValueChange = { gold18kInput = it },
                    label = { Text(viewModel.t("Gold 18K Rate (per gram)", "১৮ ক্যারেট স্বর্ণের মূল্য (প্রতি গ্রাম)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = silverInput,
                    onValueChange = { silverInput = it },
                    label = { Text(viewModel.t("Silver Rate (per gram)", "রুপার মূল্য (প্রতি গ্রাম)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val g24 = gold24kInput.toDoubleOrNull() ?: 76.5
                        val g22 = gold22kInput.toDoubleOrNull() ?: 71.2
                        val g18 = gold18kInput.toDoubleOrNull() ?: 59.8
                        val sil = silverInput.toDoubleOrNull() ?: 0.95
                        viewModel.updateRates(g24, g22, g18, sil, currencyInput.ifBlank { "$" })
                        viewModel.navigateTo(AppScreen.DASHBOARD)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(viewModel.t("SAVE & UPDATE ALL METAL PRICING", "মূল ধাতু সমূহের মূল্য তালিকা আপডেট করুন"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}
