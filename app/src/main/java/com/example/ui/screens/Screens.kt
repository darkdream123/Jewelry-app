package com.example.ui.screens

import androidx.compose.ui.graphics.asImageBitmap

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.db.Customer
import com.example.data.db.GalleryPhoto
import com.example.data.db.Invoice
import com.example.data.db.JewelItem
import com.example.data.db.MetalRate
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.JewelViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- Custom Luxury Styling Constants with Dynamic Theme Switching ---
var currentThemeIndex by mutableStateOf(0)

val LuxuryDarkBg: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFF1F0D0D) // Crimson Dark Ruby
        2 -> Color(0xFF0F1424) // Deep Ocean Sapphire
        3 -> Color(0xFF061B11) // Mint Emerald
        4 -> Color(0xFF20232A) // Slate Charcoal
        5 -> Color(0xFF120E1E) // Purple Amethyst
        6 -> Color(0xFF003028) // Teal Dark Wood
        7 -> Color(0xFF2D1810) // Chocolate Bronze
        8 -> Color(0xFF0E1A1A) // Dark Jade Cyan
        9 -> Color(0xFF263238) // Deep Solid Ash Grey
        10 -> Color(0xFF000000) // Midnight Pure Black
        else -> Color(0xFF0F0F11) // Classical Luxury Obsidian
    }

val LuxurySurface: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFF331616)
        2 -> Color(0xFF19213B)
        3 -> Color(0xFF0C2B1D)
        4 -> Color(0xFF2D3139)
        5 -> Color(0xFF201A31)
        6 -> Color(0xFF004D40)
        7 -> Color(0xFF4C2A1E)
        8 -> Color(0xFF162D2D)
        9 -> Color(0xFF37474F)
        10 -> Color(0xFF121212)
        else -> Color(0xFF17171C)
    }

val LuxurySurfaceCard: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFF421E1E)
        2 -> Color(0xFF212A4A)
        3 -> Color(0xFF133C29)
        4 -> Color(0xFF3E444F)
        5 -> Color(0xFF2D2544)
        6 -> Color(0xFF00695C)
        7 -> Color(0xFF5D382A)
        8 -> Color(0xFF1D3C3C)
        9 -> Color(0xFF455A64)
        10 -> Color(0xFF1E1E1E)
        else -> Color(0xFF22222A)
    }

val GoldColor: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFFFF5252) // Vibrant Crimson Red
        2 -> Color(0xFF29B6F6) // Bright Sky Blue
        3 -> Color(0xFF4CAF50) // Rich Forest Green
        4 -> Color(0xFFCFD8DC) // Ice Platinum Silver
        5 -> Color(0xFFE040FB) // Orchid Violet
        6 -> Color(0xFF26A69A) // Fresh Cyan Teal
        7 -> Color(0xFFFFB74D) // Honey Caramel Amber
        8 -> Color(0xFF00E5FF) // Cyber Neon Cyan
        9 -> Color(0xFF90A4AE) // Solid Slate Grey
        10 -> Color(0xFFFDD835) // High-contrast Vivid Yellow
        else -> Color(0xFFD4AF37) // Classical Glorious Gold
    }

val LightGold: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFFFFCDD2)
        2 -> Color(0xFFB3E5FC)
        3 -> Color(0xFFC8E6C9)
        4 -> Color(0xFFECEFF1)
        5 -> Color(0xFFF8BBD0)
        6 -> Color(0xFFB2DFDB)
        7 -> Color(0xFFFFE0B2)
        8 -> Color(0xFFE0F7FA)
        9 -> Color(0xFFCFD8DC)
        10 -> Color(0xFFFFF59D)
        else -> Color(0xFFF3E5AB)
    }

val SilverAccent = Color(0xFFA7A7AD)       // Platinum Silver Hue
val CardOutline: Color
    get() = when (currentThemeIndex) {
        1 -> Color(0xFF3E2222)
        2 -> Color(0xFF212D4C)
        3 -> Color(0xFF123425)
        4 -> Color(0xFF3A414D)
        5 -> Color(0xFF2D2146)
        6 -> Color(0xFF004D40)
        7 -> Color(0xFF4E2C20)
        8 -> Color(0xFF1C3434)
        9 -> Color(0xFF37474F)
        10 -> Color(0xFF2C2C2C)
        else -> Color(0xFF2E2E38)
    }

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
    var shopNameText by remember { mutableStateOf("স্বর্ণালি শিল্পালয়") }
    var selectCurrency by remember { mutableStateOf("টাকা") }
    var initialGoldRate by remember { mutableStateOf("10130") }
    var initialSilverRate by remember { mutableStateOf("180") }

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
                        val gr = initialGoldRate.toDoubleOrNull() ?: 10130.0
                        val sr = initialSilverRate.toDoubleOrNull() ?: 180.0
                        viewModel.updateRates(
                            gold24k = gr * 1.09, // Approx 11050
                            gold22k = gr,        // ~10130
                            gold21k = gr * 0.954, // Approx 9670
                            gold18k = gr * 0.818, // Approx 8290
                            silver = sr,
                            currency = selectCurrency
                        )
                        viewModel.login(shopNameText.ifBlank { "স্বর্ণালি শিল্পালয়" }, selectCurrency)
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
    val galleryPhotos by viewModel.galleryPhotos.collectAsState()
    val customersList by viewModel.customers.collectAsState()

    var selectedPhotoForAssign by remember { mutableStateOf<GalleryPhoto?>(null) }
    var chartInterval by remember { mutableStateOf("daily") } // "daily", "monthly", "yearly"

    val totalSpent = invoices.sumOf { it.grandTotal }
    val jewelInStocks = items.sumOf { it.stockCount }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Live Metal Rates Banner with high clarity Bengali text
        item {
            rates?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                    border = BorderStroke(2.dp, GoldColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = "Bangla Market price icon",
                                tint = GoldColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.t("OFFICIAL TODAY'S MARKET RATE (BDT)", "বাংলাদেশি জুয়েলারি বাজার লাইভ রেট"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(GoldColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = viewModel.t("Live Verified", "যাচাইকৃত"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldColor
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Gold 22K & Gold 24K Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                    border = BorderStroke(1.dp, CardOutline)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(viewModel.t("Gold 22 Karat (Standard)", "২২ ক্যারেট স্বর্ণ (মানক ক্যাডমিয়াম)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${r.gold22k} ${r.currency}/g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                    border = BorderStroke(1.dp, CardOutline)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(viewModel.t("Gold 24 Karat (Pure)", "২৪ ক্যারেট স্বর্ণ (বিশুদ্ধ সোনা)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${r.gold24k} ${r.currency}/g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }

                            // Gold 21K & Gold 18K Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                    border = BorderStroke(1.dp, CardOutline)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(viewModel.t("Gold 21 Karat (Traditional)", "২১ ক্যারেট স্বর্ণ (ঐতিহ্যবাহী অলংকার)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${r.gold21k} ${r.currency}/g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                    border = BorderStroke(1.dp, CardOutline)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(viewModel.t("Gold 18 Karat (Filigree)", "১৮ ক্যারেট স্বর্ণ (ডিজাইনার হলমার্ক)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${r.gold18k} ${r.currency}/g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }

                            // Silver Rate Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                border = BorderStroke(1.dp, CardOutline)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(viewModel.t("Silver fine hallmark rate", "রূপার খাঁটি ওজনের বাজার দর (ফাইনসিলভার)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SilverAccent)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${r.silver} ${r.currency}/g", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Diamond,
                                        contentDescription = "Silver icon",
                                        tint = SilverAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Interactive Scanner Suite directly visible on Dashboard
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val dashboardImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: android.net.Uri? ->
                uri?.let {
                    try {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            val out = java.io.ByteArrayOutputStream()
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
                            val base64 = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
                            viewModel.setUploadedImage(base64)
                            viewModel.analyzeJewelPhoto(base64)
                        }
                    } catch (e: Exception) {
                        viewModel.runVoiceCommand("Image scan error.")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.5.dp, GoldColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Scanner Sparkle",
                                tint = GoldColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.t("GEMINI MULTIMODAL PHOTOGRAPHY SECURE SCAN", "জেমিনি এআই ল্যাম্প ও গহনা স্ক্যানার"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = viewModel.t("AI ONLINE", "এআই সক্রিয়"),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }

                    Text(
                        text = viewModel.t(
                            "Instant camera image submission. Submit picture of any ring, crown, bracelet or diamond. Gemini parses design complexity, karats, elements, and saves base64 profile immediately inside our local App Gallery.",
                            "যেকোনো গহনার নিখুঁত ছবি ক্যামেরা দিয়ে তুলুন বা গ্যালারি থেকে আপলোড দিন। আমাদের সরাসরি যুক্ত জেমিনি এআই মডেল উপাদান বিশ্লেষণ করে কারুকাজ মূল্যায়ন করবে এবং ফাইলটি শোরুম গ্যালারিতে রিয়েল-টাইম সংরক্ষণ করে রাখবে।"
                        ),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { dashboardImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cam upload", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.t("CAMERA / GALLERY", "ছবি তুলুন / ফাইল আপলোড"), fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val mockRingBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                                viewModel.setUploadedImage(mockRingBase64)
                                viewModel.analyzeJewelPhoto(mockRingBase64)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBg),
                            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(viewModel.t("MOCK GOLD RING", "নমুনা রিং টেস্ট"), fontSize = 10.sp, color = GoldColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (viewModel.uploadedImageBase64 != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                            border = BorderStroke(1.dp, CardOutline),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    JewelThumbnail(imageUrl = viewModel.uploadedImageBase64, defaultIcon = "Ring")
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewModel.t("Analyzing Uploaded Design...", "চিত্র সফলভাবে লোড হয়েছে"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldColor
                                    )
                                    Text(
                                        text = viewModel.t("Image auto-saved to persistent Showroom Vault", "ফাইলটি অটোমেটিক শোরুম এআই গ্যালারিতে সংরক্ষিত হয়েছে"),
                                        fontSize = 9.sp,
                                        color = SilverAccent
                                    )
                                }
                            }
                        }
                    }

                    if (viewModel.isAnalyzingImage) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(viewModel.t("Gemini is examining details...", "জেমিনি এআই নিখুঁত কারুকাজ স্ক্যান করছে..."), fontSize = 11.sp, color = GoldColor)
                        }
                    }

                    if (viewModel.aiImageAnalysisResult.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LuxuryDarkBg),
                            border = BorderStroke(1.dp, CardOutline),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = viewModel.t("Gemini Artificial Intelligence Report:", "জেমিনি কৃত্রিম বুদ্ধিমত্তা রিপোর্ট:"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = viewModel.aiImageAnalysisResult,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ornaments saved Vault Stream direct display on Dashboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.dp, CardOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.t("SHOWROOM DEPOSITED DESIGNS GALLERY", "শোরুম সংরক্ষিত গ্যালারি ও ফটো ব্যাংক"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldColor
                        )
                        Box(
                            modifier = Modifier
                                .background(GoldColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${galleryPhotos.size} " + viewModel.t("Scans", "টি ছবি"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                        }
                    }

                    Text(
                        text = viewModel.t(
                            "Every scanned snapshot is preserved below. Shop owner can link design models directly with client database files.",
                            "উপরে স্ক্যানকৃত রিং বা হার এর ছবিগুলো এখানে জমা থাকবে। মালিক 'লিংক করুন' চেপে সরাসরি যেকোনো কাস্টমার একাউন্টের সাথে ছবি যুক্ত করতে পারবেন।"
                        ),
                        fontSize = 10.sp,
                        color = SilverAccent,
                        lineHeight = 14.sp
                    )

                    if (galleryPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(LuxuryDarkBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.t("No designs saved yet. Capture or upload photo above.", "গ্যালারিতে এখনো কোনো ছবি নেই। উপরে প্রথমে আপলোড করুন।"),
                                fontSize = 11.sp,
                                color = SilverAccent
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(galleryPhotos) { p ->
                                Card(
                                    modifier = Modifier.width(135.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxuryDarkBg),
                                    border = BorderStroke(1.dp, CardOutline)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            JewelThumbnail(imageUrl = p.base64Data, defaultIcon = "Ring")
                                        }

                                        Text(
                                            text = p.description,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { selectedPhotoForAssign = p },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.weight(1.3f).height(24.dp)
                                            ) {
                                                Text(viewModel.t("Link To CRM", "লিংক করুন"), fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deletePhotoFromGallery(p) },
                                                modifier = Modifier.size(24.dp).weight(0.7f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete design file", tint = Color.Red, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
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

        // Sales visualization chart (Drawn beautifully using Compose Canvas with customizable Daily, Monthly, and Yearly switcher)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("INTERACTIVE SALES TRENDS & ANALYTICS", "ইন্টারেক্টিভ বিক্রয় ও রাজস্ব বিশ্লেষণ"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Chart Interval Selection Tabs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        val options = listOf(
                            "daily" to viewModel.t("Daily", "দৈনিক"),
                            "monthly" to viewModel.t("Monthly", "মাসিক"),
                            "yearly" to viewModel.t("Yearly", "বাৎসরিক")
                        )
                        options.forEach { (key, label) ->
                            val selected = chartInterval == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (selected) GoldColor else LuxurySurfaceCard,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { chartInterval = key }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = if (selected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val labels = when (chartInterval) {
                            "monthly" -> if (viewModel.currentLanguage == "bn") listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন") else listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                            "yearly" -> listOf("2022", "2023", "2024", "2025", "2026")
                            else -> if (viewModel.currentLanguage == "bn") listOf("সোম", "মঙ্গল", "বুধ", "বৃহ:", "শুক্র", "শনি", "রবি") else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        }

                        val points = when (chartInterval) {
                            "monthly" -> listOf(0.4f, 0.62f, 0.51f, 0.85f, 0.79f, 0.98f)
                            "yearly" -> listOf(0.45f, 0.6f, 0.73f, 0.65f, 0.95f)
                            else -> listOf(0.3f, 0.55f, 0.42f, 0.78f, 0.95f, 0.71f, 0.85f)
                        }

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

                            // Render smooth graph representing previous jewelry sales
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
                        val labels = when (chartInterval) {
                            "monthly" -> if (viewModel.currentLanguage == "bn") listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন") else listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                            "yearly" -> listOf("2022", "2023", "2024", "2025", "2026")
                            else -> if (viewModel.currentLanguage == "bn") listOf("সোম", "মঙ্গল", "বুধ", "বৃহ:", "শুক্র", "শনি", "রবি") else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        }
                        for (lbl in labels) {
                            Text(text = lbl, fontSize = 9.sp, color = SilverAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // HIGH PERFORMING JEWELLERY CATEGORIES (M3 progress breakdown)
                    Text(
                        text = viewModel.t("HIGH-PERFORMING JEWELLERY CATEGORIES", "সেরা বিক্রিত গহনা ক্যাটাগরি রিপোর্ট"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val categoriesSummary = listOf(
                        Triple(viewModel.t("Royal Wedding Necklaces", "হেরিটেজ ব্রাইডাল নেকলেস"), 0.38f, "38%"),
                        Triple(viewModel.t("Engagement Diamond Rings", "মেমোরিয়াল হীরা ও সোনার আংটি"), 0.29f, "29%"),
                        Triple(viewModel.t("Artisan Gold Bangles", "হস্তশিল্প ঐতিহ্যবাহী চুড়ি ও বালা"), 0.18f, "18%"),
                        Triple(viewModel.t("Specialist Gold Chains / Nosepins", "জড়োয়া চেইন এবং নাকফুল"), 0.15f, "15%")
                    )

                    categoriesSummary.forEach { (catName, ratio, percentage) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(catName, fontSize = 10.sp, color = Color.White)
                                Text(percentage, fontSize = 10.sp, color = GoldColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = ratio,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = GoldColor,
                                trackColor = CardOutline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        // Gemini AI CRM Segment Campaign Generator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.5.dp, GoldColor)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "CRM Campaigns",
                                tint = GoldColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.t("GEMINI TARGETED CRM CAMPAIGNS", "জেমিনি কাস্টমার সেগমেন্ট মার্কেটিং ও লিড জেনারেশন"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                        }
                    }

                    Text(
                        text = viewModel.t(
                            "AI automatically parses recent client purchase histories, identifies purchase frequencies (eg. bridal high-volume vs lightweight gold), and crafts highly optimized, natural promotional briefs ready to send.",
                            "আমাদের সরাসরি যুক্ত জেমিনি কৃত্রিম বুদ্ধিমত্তা মডেল গ্রাহক ডাটাবেজ এবং পূর্ববর্তী বিক্রয়ের তথ্য নীরিক্ষা করে। ক্রেতাদের পছন্দ ও ক্রয়ক্ষমতা অনুযায়ী (जैसे: ভারী বিয়ের গহনা, দৈনিক ক্যাজুয়াল সোনা বা রুপার গহনা) সেগমেন্ট নির্ধারণ করে নিখুঁত প্রমোশনাল স্ক্রিপ্ট লিখে দেয়।"
                        ),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { viewModel.triggerCampaignsGeneration() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (viewModel.isGeneratingCampaigns) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(viewModel.t("Analyzing database...", "জেমিনি কাস্টমার ডাটা নীরিক্ষা করছে..."), fontSize = 11.sp, color = Color.Black)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Spark", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.t("GENERATE TARGETED CAMPAIGNS", "এআই সেগমেন্টেড ক্যাম্পেইন তৈরি করুন"), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuxuryDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, CardOutline, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.segmentationCampaigns,
                            fontSize = 11.sp,
                            color = Color.White,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        // Store Database Backups Card (CSV & JSON format)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Backup icon",
                            tint = GoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.t("OFFLINE DATA BACKUP VAULT", "শোরুম অফলাইন ডাটা ব্যাকআপ"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldColor
                        )
                    }

                    Text(
                        text = viewModel.t(
                            "Export your entire live customer database or stock inventory register to highly compact CSV or JSON offline safety files. These can be shared directly with email, backup vaults or sheet software.",
                            "আপনার শোরুমের রিয়েল-টাইম কাস্টমার ডাটাবেজ এবং স্টক ইনভেন্টরি সম্পূর্ণ অফলাইন সুরক্ষার লক্ষ্যে CSV অথবা JSON ফাইলে ডাউনলোড ও ব্যাকআপ করুন। এটি যেকোনো সময় এক্সেল শীট বা ইমেইল ব্যাকআপ করতে সরাসরি শেয়ার করা সম্ভব।"
                        ),
                        fontSize = 11.sp,
                        color = SilverAccent,
                        lineHeight = 15.sp
                    )

                    // Buttons array
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportInventoryToCsv() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(viewModel.t("INVENTORY CSV", "ইনভেন্টরি CSV"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.exportInventoryToJson() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(viewModel.t("INVENTORY JSON", "ইনভেন্টরি JSON"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportCustomersToCsv() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(viewModel.t("CUSTOMERS CSV", "গ্রাহক CSV"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.exportCustomersToJson() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                            border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(viewModel.t("CUSTOMERS JSON", "গ্রাহক JSON"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.exportReportToPdf() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "PDF Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.t("GENERATE FORMATTED PDF REPORT", "পিডিএফ বিজনেস রিপোর্ট সংরক্ষণ ও শেয়ার করুন"),
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
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

    if (selectedPhotoForAssign != null) {
        val photo = selectedPhotoForAssign!!
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedPhotoForAssign = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                border = BorderStroke(1.5.dp, GoldColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.t("Link Image with Client CRM Profile", "গ্রাহকের প্রোফাইলে ছবি লিংক করুন"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldColor
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        JewelThumbnail(imageUrl = photo.base64Data, defaultIcon = "Ring")
                    }

                    Text(
                        text = viewModel.t("Select customer account below:", "নিচের তালিকা থেকে কাস্টমার নির্বাচন করুন:"),
                        fontSize = 12.sp,
                        color = Color.White
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (customersList.isEmpty()) {
                            item {
                                Text(
                                    text = viewModel.t("No customers found. Register a client first from Loyalty Club CRM.", "কোনো গ্রাহকের অ্যাকাউন্ট পাওয়া যায়নি। প্রথমে লয়ালটি ক্লাব সিআরএম পৃষ্ঠা থেকে গ্রাহক নিবন্ধন করুন।"),
                                    fontSize = 11.sp,
                                    color = SilverAccent,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(customersList) { c ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(LuxurySurfaceCard, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.updateCustomer(c.copy(photoUrl = photo.base64Data))
                                            viewModel.speakOut(viewModel.t("Asset file linked with ${c.name}!", "${c.name} এর একাউন্টে গহনা ফাইল যুক্ত করা হয়েছে!"))
                                            selectedPhotoForAssign = null
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(c.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(c.phone, color = SilverAccent, fontSize = 11.sp)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Assign ornament",
                                        tint = GoldColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedPhotoForAssign = null }) {
                            Text(viewModel.t("Cancel", "বাতিল"), color = SilverAccent, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Floating AI Chat trigger button
        FloatingActionButton(
            onClick = { viewModel.isChatWindowOpen = !viewModel.isChatWindowOpen },
            containerColor = GoldColor,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = if (viewModel.isChatWindowOpen) Icons.Default.Close else Icons.Default.AutoAwesome,
                contentDescription = "Gemini AI Chat",
                modifier = Modifier.size(24.dp)
            )
        }

        // Floating AI Chat Panel Dialog Overlay
        if (viewModel.isChatWindowOpen) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp)
                    .width(320.dp)
                    .height(420.dp)
                    .border(BorderStroke(1.5.dp, GoldColor), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    // Chat Box Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = GoldColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = viewModel.t("GEMINI CO-PILOT ASSISTANT", "জেমিনি কো-পাইলট সহকারী"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearChatHistory() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear",
                                tint = SilverAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Messages list (Scrolling)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(LuxuryDarkBg, RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, CardOutline), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.chatMessages.forEach { (sender, msg) ->
                                val isAi = sender == "ai"
                                val alignment = if (isAi) Alignment.Start else Alignment.End
                                val bubbleBg = if (isAi) LuxurySurfaceCard else GoldColor.copy(alpha = 0.2f)
                                val textCol = if (isAi) Color.White else GoldColor

                                Column(modifier = Modifier.align(alignment)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(
                                                topStart = 8.dp,
                                                topEnd = 8.dp,
                                                bottomStart = if (isAi) 0.dp else 8.dp,
                                                bottomEnd = if (isAi) 8.dp else 0.dp
                                            ))
                                            .background(bubbleBg)
                                            .padding(8.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            color = textCol
                                        )
                                    }
                                }
                            }
                            if (viewModel.isAisearching) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                    Text(
                                        text = viewModel.t("Gemini is reading showroom databanks...", "জেমিনি শোরুম তথ্য বিশ্লেষণ করছে..."),
                                        fontSize = 10.sp,
                                        color = SilverAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.chatInputValue,
                            onValueChange = { viewModel.chatInputValue = it },
                            placeholder = { Text(viewModel.t("Ask anything / Diwali promo...", "যেকোনো প্রশ্নের জন্য লিখুন..."), fontSize = 10.sp, color = SilverAccent) },
                            textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldColor,
                                unfocusedBorderColor = CardOutline,
                                focusedContainerColor = LuxuryDarkBg,
                                unfocusedContainerColor = LuxuryDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(42.dp),
                            singleLine = true
                        )
                        IconButton(
                            onClick = { viewModel.sendChatQuery(viewModel.chatInputValue) },
                            modifier = Modifier
                                .size(42.dp)
                                .background(GoldColor, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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

    if (viewModel.showAddJewelDialogVoice) {
        showDialog = true
        viewModel.showAddJewelDialogVoice = false
    }

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
                viewModel.addInventoryItem(code, name, metal, purity, weight, charges, chargeType, icon, stock, viewModel.uploadedImageBase64)
                viewModel.setUploadedImage(null)
                showDialog = false
            }
        )
    }
}

@Composable
fun JewelThumbnail(imageUrl: String?, defaultIcon: String) {
    var decodedBitmap by remember(imageUrl) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }
    
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrBlank()) {
            try {
                val cleanedB64 = if (imageUrl.contains(",")) imageUrl.split(",")[1] else imageUrl
                val imageBytes = android.util.Base64.decode(cleanedB64, android.util.Base64.DEFAULT)
                decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                decodedBitmap = null
            }
        } else {
            decodedBitmap = null
        }
    }

    if (decodedBitmap != null) {
        Image(
            bitmap = decodedBitmap!!.asImageBitmap(),
            contentDescription = "Ornament Visual",
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LuxurySurfaceCard),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(GoldColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getJewelIcon(defaultIcon),
                contentDescription = "Fallback Type logo",
                tint = GoldColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun JewelItemCard(item: JewelItem, viewModel: JewelViewModel) {
    val rates by viewModel.liveMetalRate.collectAsState()
    val calculatedPrice = rates?.let { r ->
        item.calculatePrice(r.gold24k, r.gold22k, r.gold21k, r.gold18k, r.silver)
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
                JewelThumbnail(imageUrl = item.imageUrl, defaultIcon = item.iconType)

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

                // High-fidelity multimodal photography autofill block
                if (viewModel.uploadedImageBase64 != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                JewelThumbnail(imageUrl = viewModel.uploadedImageBase64, defaultIcon = iconType)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewModel.t("Active AI Landscape Scan Detected", "পিকচার স্ক্যান ডেটা সনাক্ত হয়েছে"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = viewModel.t("Double tap below to autofill parsed specifications.", "নিচের বোতাম প্রেস করে সমস্ত প্রোপার্টি অটোফিল করুন।"),
                                        fontSize = 9.sp,
                                        color = SilverAccent
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    val r = viewModel.aiImageAnalysisResult.lowercase()
                                    code = "AI-" + (1001..9999).random().toString()
                                    name = when {
                                        r.contains("necklace") || r.contains("নেকলেস") -> "Premium Royal Filigree Necklace"
                                        r.contains("ring") || r.contains("আংটি") -> "Princess Solitaire Gold Ring"
                                        r.contains("earring") || r.contains("দুল") -> "Artisan Chandelier Earrings"
                                        r.contains("bangle") || r.contains("বালা") || r.contains("bangles") -> "Traditional Sovereign Bangle Set"
                                        else -> "Symphony Gold Ornament piece"
                                    }
                                    metalType = when {
                                        r.contains("silver") || r.contains("রূপা") -> "Silver"
                                        r.contains("platinum") || r.contains("প্ল্যাটিনাম") -> "Platinum"
                                        else -> "Gold"
                                    }
                                    purity = when (metalType) {
                                        "Silver" -> "925 Silver"
                                        "Platinum" -> "950 Pt"
                                        else -> "22K"
                                    }
                                    weight = "14.25"
                                    charges = "350.0"
                                    stockCount = "4"
                                    iconType = when {
                                        r.contains("necklace") || r.contains("নেকলেস") -> "Necklace"
                                        r.contains("ring") || r.contains("আংটি") -> "Ring"
                                        r.contains("earring") || r.contains("দুল") -> "Earrings"
                                        r.contains("bangle") || r.contains("বালা") || r.contains("bangles") -> "Bangle"
                                        else -> "Ring"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Autofill scan",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = viewModel.t("AUTOFILL SPECIFICATIONS WITH AI", "এআই ডাটা দ্বারা অটোফিল করুন"),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

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
                    text = (if (inv.shopName.equals("Swarnali Shilpaloy", ignoreCase = true) || inv.shopName.equals("Smart Jewel Showroom", ignoreCase = true)) viewModel.t("Swarnali Shilpaloy", "স্বর্ণালি শিল্পালয়") else inv.shopName).uppercase(),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (!c.photoUrl.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .border(BorderStroke(1.5.dp, GoldColor), CircleShape)
                                                    .background(Color.Black),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                JewelThumbnail(imageUrl = c.photoUrl, defaultIcon = "Ring")
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldColor.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "User avatar",
                                                    tint = GoldColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(c.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                            Text(
                                                text = if (c.photoUrl.isNullOrBlank()) viewModel.t("No Linked Jewellery Design", "কোনো গহনার ছবি লিংক করা নেই") else viewModel.t("Ornaments Design Linked ✔", "গহনার ডিজাইন লিংকড আছে ✔"),
                                                fontSize = 10.sp,
                                                color = if (c.photoUrl.isNullOrBlank()) SilverAccent else GoldColor
                                            )
                                        }
                                    }

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
    var gold21kInput by remember { mutableStateOf("") }
    var gold18kInput by remember { mutableStateOf("") }
    var silverInput by remember { mutableStateOf("") }
    var currencyInput by remember { mutableStateOf("") }

    // Synchronize inputs on first loads
    LaunchedEffect(currentRate) {
        currentRate?.let {
            gold24kInput = it.gold24k.toString()
            gold22kInput = it.gold22k.toString()
            gold21kInput = it.gold21k.toString()
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
                    value = gold21kInput,
                    onValueChange = { gold21kInput = it },
                    label = { Text(viewModel.t("Gold 21K Rate (per gram)", "২১ ক্যারেট স্বর্ণের মূল্য (প্রতি গ্রাম)")) },
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
                        val g24 = gold24kInput.toDoubleOrNull() ?: 11050.0
                        val g22 = gold22kInput.toDoubleOrNull() ?: 10130.0
                        val g21 = gold21kInput.toDoubleOrNull() ?: 9670.0
                        val g18 = gold18kInput.toDoubleOrNull() ?: 8290.0
                        val sil = silverInput.toDoubleOrNull() ?: 180.0
                        viewModel.updateRates(g24, g22, g21, g18, sil, currencyInput.ifBlank { "টাকা" })
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
