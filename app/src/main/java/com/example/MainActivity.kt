package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.repository.JewelRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JewelViewModel
import com.example.ui.viewmodel.JewelViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: JewelViewModel

    private val speechRecognizerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                viewModel.runVoiceCommand(spokenText)
            }
        }
    }

    private fun triggerVoiceRecognition() {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "স্বর্ণালি শিল্পালয় ভয়েস রাডার - বলুন...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.runVoiceCommand("hello")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialise Room Database and repository elements
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = JewelRepository(
            jewelDao = database.jewelDao(),
            customerDao = database.customerDao(),
            invoiceDao = database.invoiceDao(),
            metalRateDao = database.metalRateDao(),
            galleryPhotoDao = database.galleryPhotoDao()
        )
        
        // 2. Build JewelViewModel using standard provider factory
        val factory = JewelViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[JewelViewModel::class.java]

        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                // If not logged in, force show login setup
                if (!viewModel.isLoggedIn) {
                    LoginScreen(viewModel = viewModel)
                } else {
                    val activeRates by viewModel.liveMetalRate.collectAsState()
                    val activeCurrency = activeRates?.currency ?: "$"
                    
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
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

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = LuxuryDarkBg,
                                drawerContentColor = Color.White,
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Header Branded logo
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(bottom = 4.dp, top = 12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(GoldColor, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "স্ব",
                                                color = Color.Black,
                                                fontSize = 18.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "স্বর্ণালি শিল্পালয়",
                                                fontSize = 16.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                                color = GoldColor
                                            )
                                            Text(
                                                text = "ESTD 1998 • GOLD & PRECIOUS GEMS",
                                                fontSize = 8.sp,
                                                color = SilverAccent
                                            )
                                        }
                                    }
                                    
                                    HorizontalDivider(color = CardOutline, thickness = 1.dp)
                                    
                                    // 1. Proprietor (Owner) Details
                                    Text(
                                        text = viewModel.t("SHOWROOM PROPRIETOR CONCIERGE", "স্বত্বাধিকারী ও শোরুম বিবরণী"),
                                        fontSize = 10.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = SilverAccent
                                    )
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.25f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(GoldColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Owner Avatar",
                                                        tint = GoldColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = viewModel.t("Shri Swarnendu Roy & Family", "শ্রী স্বর্ণেন্দু রায় ও পরিবার"),
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = viewModel.t("Proprietor & Designer-in-Chief", "মালিক ও প্রধান নকশাকার"),
                                                        fontSize = 10.sp,
                                                        color = GoldColor
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Ph", tint = GoldColor, modifier = Modifier.size(12.dp))
                                                Text(text = "+880 1712-345678", fontSize = 11.sp, color = SilverAccent)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Email, contentDescription = "Mail", tint = GoldColor, modifier = Modifier.size(12.dp))
                                                Text(text = "swarnendu@shilpaloy.com", fontSize = 11.sp, color = SilverAccent)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Addr", tint = GoldColor, modifier = Modifier.size(12.dp))
                                                Text(text = viewModel.t("74 Kanak Bhavan, Gold Bazar, Kolkata & Dhaka", "৭৪ কনক ভবন, সোনা পট্টি, কলকাতা ও ঢাকা"), fontSize = 11.sp, color = SilverAccent)
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = viewModel.t("\"Purity of gold and trust of hearts is what we preserve at স্বর্ণালি শিল্পালয় for generations.\"", "\"সোনার বিশুদ্ধতা এবং গ্রাহকদের আজীবন বিশ্বাসই আমাদের 'স্বর্ণালি শিল্পালয়' এর মূল শক্তি।\""),
                                                fontSize = 9.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = GoldColor.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                    
                                    HorizontalDivider(color = CardOutline, thickness = 1.dp)

                                    // --- 1.5 Dynamic Theme Switcher ---
                                    Text(
                                        text = viewModel.t("PALETTE CUSTOMIZATION", "অ্যাপ থিম ও সলিড কালার পরিবর্তন"),
                                        fontSize = 10.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = SilverAccent
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard),
                                        border = BorderStroke(1.dp, CardOutline)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = viewModel.t("Select Custom Dashboard Look:", "প্রিয় মেটাল থিম নির্বাচন করুন:"),
                                                fontSize = 11.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    listOf(
                                                        0 to Color(0xFFD4AF37), // Classical Gold
                                                        1 to Color(0xFFFF5252), // Crimson Ruby
                                                        2 to Color(0xFF29B6F6), // Ocean Sapphire
                                                        3 to Color(0xFF4CAF50), // Mint Emerald
                                                        4 to Color(0xFFCFD8DC), // Slate Charcoal
                                                        5 to Color(0xFFE040FB)  // Purple Violet
                                                    ).forEach { (index, color) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(color)
                                                                .border(
                                                                    width = if (currentThemeIndex == index) 3.dp else 1.dp,
                                                                    color = if (currentThemeIndex == index) Color.White else Color.Transparent,
                                                                    shape = CircleShape
                                                                )
                                                                .clickable { currentThemeIndex = index },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (currentThemeIndex == index) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = if (index == 4) Color.Black else Color.White,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    listOf(
                                                        6 to Color(0xFF26A69A), // Cyan Teal
                                                        7 to Color(0xFFFFB74D), // Honey Amber
                                                        8 to Color(0xFF00E5FF), // Cyber Cyan
                                                        9 to Color(0xFF90A4AE), // Solid Ash Grey
                                                        10 to Color(0xFFFDD835) // Midnight Pure Yellow
                                                    ).forEach { (index, color) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(color)
                                                                .border(
                                                                    width = if (currentThemeIndex == index) 3.dp else 1.dp,
                                                                    color = if (currentThemeIndex == index) Color.White else Color.Transparent,
                                                                    shape = CircleShape
                                                                )
                                                                .clickable { currentThemeIndex = index },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (currentThemeIndex == index) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = if (index == 9) Color.Black else Color.White,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            val activeThemeName = when(currentThemeIndex) {
                                                1 -> viewModel.t("Crimson Ruby Rose", "ক্রিমসন রুবি লাল থিম")
                                                2 -> viewModel.t("Deep Blue Sapphire", "নীল নীলা ডিল্যাক্স থিম")
                                                3 -> viewModel.t("Mint Emerald Jade", "সবুজ পান্না ক্যাসেল থিম")
                                                4 -> viewModel.t("Modern Slate Silver", "আধুনিক স্লেট সিলভার থিম")
                                                5 -> viewModel.t("Royal Purple Aura", "রয়্যাল বেগুনি আভা থিম")
                                                6 -> viewModel.t("Teal Forest Symphony", "নীলাভ সবুজ টিল থিম")
                                                7 -> viewModel.t("Chocolate Honey Amber", "চকোলেট ও মধুর সোনালী থিম")
                                                8 -> viewModel.t("Cyber Cyan Neon", "সাইবার নিয়ন আলো থিম")
                                                9 -> viewModel.t("Solid Solid Slate Ash", "সলিড অ্যাশ ধূসর থিম")
                                                10 -> viewModel.t("Midnight Contrast Pure", "গাঢ় কৃষ্ণচূড়া উচ্চ বৈসাদৃশ্য থিম")
                                                else -> viewModel.t("Original Gold Obsidian (Luxury)", "ঐতিহ্যবাহী স্বর্নালী কালো থিম (রয়্যাল)")
                                            }
                                            
                                            Text(
                                                text = "${viewModel.t("Active Style:", "চলতি স্টাইল:")} $activeThemeName",
                                                fontSize = 9.sp,
                                                color = GoldColor,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                    
                                    HorizontalDivider(color = CardOutline, thickness = 1.dp)
                                    
                                    // 2. Owner Voice Modes
                                    Text(
                                        text = viewModel.t("PROPRIETOR SMART VOICE CODES", "মালিকের ভয়েস কমান্ড রাডার"),
                                        fontSize = 10.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = SilverAccent
                                    )
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(
                                                        imageVector = Icons.Default.Mic,
                                                        contentDescription = "Voice Mode Mic",
                                                        tint = if (viewModel.isListeningVoice) Color.Red else GoldColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = if (viewModel.isListeningVoice) viewModel.t("Listening Mode Active", "ভয়েস রাডার সক্রিয়") else viewModel.t("Active Voice Ready", "ভয়েস রিডার প্রস্তুত"),
                                                        fontSize = 11.sp,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                if (viewModel.isTtsReady) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(Color.Green, CircleShape)
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { this@MainActivity.triggerVoiceRecognition() },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = "Trigger Mic",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (viewModel.isListeningVoice) viewModel.t("LISTENING...", "শুনছি...") else viewModel.t("TAP TO SPEAK HANDS-FREE", "ভয়েস স্পিচ রেকর্ডার শুনুন"),
                                                    fontSize = 11.sp,
                                                    color = Color.Black,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                            }
                                            
                                            Text(viewModel.t("Tap custom phrases or click button above:", "ভয়েস কমান্ড ট্যাপ করুন বা কথা বলুন:"), fontSize = 9.sp, color = SilverAccent)
                                            
                                            val voicePhrases = listOf(
                                                viewModel.t("What are the live gold and silver rates?", "আজকের সোনার দাম বলুন"),
                                                viewModel.t("Tell me total active stock levels", "স্টকের আপডেট দিন"),
                                                viewModel.t("How much did we sell in cash bills today?", "মোট কত বিক্রি হলো আজ")
                                            )
                                            
                                            voicePhrases.forEach { phrase ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(LuxuryDarkBg, RoundedCornerShape(6.dp))
                                                        .clickable { viewModel.runVoiceCommand(phrase) }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = phrase, fontSize = 10.sp, color = GoldColor, modifier = Modifier.weight(1f))
                                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = SilverAccent, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            
                                            if (viewModel.voiceResponseText.isNotEmpty()) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = LuxuryDarkBg),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    Text(
                                                        text = viewModel.voiceResponseText,
                                                        fontSize = 10.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    HorizontalDivider(color = CardOutline, thickness = 1.dp)
                                    
                                    // 3. Multimodal Image Appraisal Helper
                                    Text(
                                        text = viewModel.t("GEMINI MULTIMODAL PHOTOGRAPHY SCAN", "জেমিনি এআই গহনা ইমেজ স্ক্যানার"),
                                        fontSize = 10.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = SilverAccent
                                    )
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceCard)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { imagePickerLauncher.launch("image/*") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1.3f),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Pick Photo", tint = Color.Black, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(viewModel.t("UPLOAD PHOTO", "ছবি আপলোড"), fontSize = 8.sp, color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                }
                                                
                                                Button(
                                                    onClick = {
                                                        val mockRingBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                                                        viewModel.setUploadedImage(mockRingBase64)
                                                        viewModel.analyzeJewelPhoto(mockRingBase64)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBg),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.5f)),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Text(viewModel.t("SAMPLE MOCK", "নমুনা রিং"), fontSize = 8.sp, color = GoldColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                }
                                            }
                                            
                                            if (viewModel.uploadedImageBase64 != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(60.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(LuxuryDarkBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.Image, contentDescription = "Active upload image", tint = GoldColor, modifier = Modifier.size(24.dp))
                                                    Text(viewModel.t("Image Scanned", "ছবি সফলভাবে যুক্ত!"), fontSize = 9.sp, color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp))
                                                }
                                            }
                                            
                                            if (viewModel.isAnalyzingImage) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(viewModel.t("AI Scanning details...", "এআই ডিজাইন যাচাই করছে..."), fontSize = 9.sp, color = GoldColor)
                                                }
                                            }
                                            
                                            if (viewModel.aiImageAnalysisResult.isNotEmpty()) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = LuxuryDarkBg),
                                                    border = BorderStroke(1.dp, CardOutline)
                                                ) {
                                                    Text(
                                                        text = viewModel.aiImageAnalysisResult,
                                                        fontSize = 9.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Showroom Drawer Bar",
                                                tint = GoldColor
                                            )
                                        }
                                    },
                                    title = {
                                        Column {
                                            Text(
                                                text = viewModel.getTranslatedShopName().uppercase(),
                                                fontSize = 16.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                                color = GoldColor,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                                            )
                                            Text(
                                                text = viewModel.t("Showroom Billing Suite", "শোরুম বিলিং স্যুট"),
                                                fontSize = 10.sp,
                                                color = SilverAccent
                                            )
                                        }
                                    },
                                actions = {
                                    // Dynamic language toggle button with high styling definition
                                    TextButton(
                                        onClick = { viewModel.toggleLanguage() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = GoldColor),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (viewModel.currentLanguage == "en") "বাংলা" else "ENG",
                                            fontSize = 12.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                            color = GoldColor
                                        )
                                    }

                                    // Live rate status button indicator
                                    Button(
                                        onClick = { viewModel.navigateTo(AppScreen.RATE_MANAGER) },
                                        colors = ButtonDefaults.buttonColors(containerColor = LuxurySurfaceCard),
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .border(
                                                androidx.compose.foundation.BorderStroke(1.dp, GoldColor.copy(alpha = 0.4f)),
                                                RoundedCornerShape(30.dp)
                                            ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Rates info",
                                            tint = GoldColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = viewModel.t("Gold ", "স্বর্ণ ") + "${activeCurrency}${activeRates?.gold22k ?: 70.0}/g",
                                            fontSize = 10.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Log off button
                                    IconButton(onClick = { viewModel.logout() }) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Log out of showroom",
                                            tint = Color.Red
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = LuxuryDarkBg,
                                    titleContentColor = Color.White
                                )
                            )
                        },
                        bottomBar = {
                            val currentItem = viewModel.currentScreen
                            val itemList by viewModel.items.collectAsState()
                            val lowStockCount = itemList.count { it.stockCount in 1..2 }
                            val cartCount = viewModel.cartItems.size

                            Card(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 2.dp)
                                    .fillMaxWidth()
                                    .height(72.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = LuxurySurface),
                                border = BorderStroke(1.dp, CardOutline),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Item 1: Dashboard
                                    val isDashSelected = currentItem == AppScreen.DASHBOARD
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.DASHBOARD) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isDashSelected) GoldColor.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = "Dashboard",
                                                    tint = if (isDashSelected) GoldColor else SilverAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = viewModel.t("Dashboard", "ড্যাশবোর্ড"),
                                                fontSize = 9.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = if (isDashSelected) GoldColor else SilverAccent
                                            )
                                        }
                                    }

                                    // Item 2: Stock (Inventory)
                                    val isStockSelected = currentItem == AppScreen.INVENTORY || currentItem == AppScreen.AI_MARKETING
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.INVENTORY) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isStockSelected) GoldColor.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Box {
                                                    Icon(
                                                        imageVector = Icons.Default.List,
                                                        contentDescription = "Stock",
                                                        tint = if (isStockSelected) GoldColor else SilverAccent,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    if (lowStockCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFFF9800))
                                                                .align(Alignment.TopEnd)
                                                                .offset(x = 2.dp, y = (-2).dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = viewModel.t("Stock", "স্টক"),
                                                    fontSize = 9.sp,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = if (isStockSelected) GoldColor else SilverAccent
                                                )
                                                if (lowStockCount > 0) {
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF4C3015))
                                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "$lowStockCount",
                                                            fontSize = 7.sp,
                                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                            color = Color(0xFFFFB74D)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Item 3: POS Counter (Checkout)
                                    val isPosSelected = currentItem == AppScreen.POS
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.POS) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isPosSelected) GoldColor.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Box {
                                                    Icon(
                                                        imageVector = Icons.Default.ShoppingCart,
                                                        contentDescription = "POS",
                                                        tint = if (isPosSelected) GoldColor else SilverAccent,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    if (cartCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(Color.Red)
                                                                .align(Alignment.TopEnd)
                                                                .offset(x = 2.dp, y = (-2).dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = viewModel.t("Counter", "কাউন্টার"),
                                                    fontSize = 9.sp,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = if (isPosSelected) GoldColor else SilverAccent
                                                )
                                                if (cartCount > 0) {
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF3F1616))
                                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "$cartCount",
                                                            fontSize = 7.sp,
                                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                            color = Color(0xFFFF5252)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Item 4: Customers
                                    val isCustSelected = currentItem == AppScreen.CUSTOMERS
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.CUSTOMERS) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isCustSelected) GoldColor.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.People,
                                                    contentDescription = "CRM",
                                                    tint = if (isCustSelected) GoldColor else SilverAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = viewModel.t("CRM", "গ্রাহকরা"),
                                                fontSize = 9.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = if (isCustSelected) GoldColor else SilverAccent
                                            )
                                        }
                                    }

                                    // Item 5: Invoices
                                    val isInvSelected = currentItem == AppScreen.INVOICES || currentItem == AppScreen.INVOICE_DETAIL
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.INVOICES) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isInvSelected) GoldColor.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Receipt,
                                                    contentDescription = "Invoices",
                                                    tint = if (isInvSelected) GoldColor else SilverAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = viewModel.t("Bills", "চালানলগ"),
                                                fontSize = 9.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = if (isInvSelected) GoldColor else SilverAccent
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = LuxuryDarkBg
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (viewModel.currentScreen) {
                                AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
                                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                                AppScreen.POS -> PosScreen(viewModel = viewModel)
                                AppScreen.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                                AppScreen.INVOICES -> InvoicesScreen(viewModel = viewModel)
                                AppScreen.AI_MARKETING -> AiMarketingScreen(viewModel = viewModel)
                                AppScreen.RATE_MANAGER -> RateManagerScreen(viewModel = viewModel)
                                AppScreen.INVOICE_DETAIL -> InvoiceDetailScreen(viewModel = viewModel)
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
