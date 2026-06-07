package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialise Room Database and repository elements
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = JewelRepository(
            jewelDao = database.jewelDao(),
            customerDao = database.customerDao(),
            invoiceDao = database.invoiceDao(),
            metalRateDao = database.metalRateDao()
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
                    
                    Scaffold(
                        topBar = {
                            TopAppBar(
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
                            NavigationBar(
                                containerColor = LuxuryDarkBg,
                                tonalElevation = 8.dp
                            ) {
                                val currentItem = viewModel.currentScreen
                                
                                NavigationBarItem(
                                    selected = currentItem == AppScreen.DASHBOARD,
                                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                    label = { Text(viewModel.t("Dashboard", "ড্যাশবোর্ড"), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home dashboard") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GoldColor,
                                        unselectedIconColor = SilverAccent,
                                        unselectedTextColor = SilverAccent,
                                        indicatorColor = GoldColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentItem == AppScreen.INVENTORY || currentItem == AppScreen.AI_MARKETING,
                                    onClick = { viewModel.navigateTo(AppScreen.INVENTORY) },
                                    label = { Text(viewModel.t("Stock", "স্টক"), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Inventory stock") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GoldColor,
                                        unselectedIconColor = SilverAccent,
                                        unselectedTextColor = SilverAccent,
                                        indicatorColor = GoldColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentItem == AppScreen.POS,
                                    onClick = { viewModel.navigateTo(AppScreen.POS) },
                                    label = { Text(viewModel.t("POS Counter", "কাউন্টার"), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "POS Counter checkout") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GoldColor,
                                        unselectedIconColor = SilverAccent,
                                        unselectedTextColor = SilverAccent,
                                        indicatorColor = GoldColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentItem == AppScreen.CUSTOMERS,
                                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                                    label = { Text(viewModel.t("CRM Club", "গ্রাহকরা"), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    icon = { Icon(imageVector = Icons.Default.People, contentDescription = "CRM Clients list") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GoldColor,
                                        unselectedIconColor = SilverAccent,
                                        unselectedTextColor = SilverAccent,
                                        indicatorColor = GoldColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentItem == AppScreen.INVOICES || currentItem == AppScreen.INVOICE_DETAIL,
                                    onClick = { viewModel.navigateTo(AppScreen.INVOICES) },
                                    label = { Text(viewModel.t("Invoice logs", "চালানলগ"), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = "Bill logs archive") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GoldColor,
                                        unselectedIconColor = SilverAccent,
                                        unselectedTextColor = SilverAccent,
                                        indicatorColor = GoldColor
                                    )
                                )
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
