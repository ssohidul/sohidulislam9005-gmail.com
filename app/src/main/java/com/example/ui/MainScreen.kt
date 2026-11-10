package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("ড্যাশবোর্ড", Icons.Default.Dashboard),
    POS("বিক্রি", Icons.Default.PointOfSale),
    STOCK("স্টক", Icons.Default.Inventory),
    DUES("বাকি খাতা", Icons.Default.Book),
    EXPENSES("খরচ", Icons.Default.MoneyOff)
}

enum class ScreenState {
    MAIN_TABS,
    SETTINGS,
    STAFF_MANAGEMENT
}

@Composable
fun MainScreen(
    viewModel: DokanViewModel
) {
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_TABS) }
    val session by viewModel.session.collectAsState()

    when (currentScreen) {
        ScreenState.STAFF_MANAGEMENT -> {
            BackHandler {
                currentScreen = ScreenState.SETTINGS
            }
            StaffManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = ScreenState.SETTINGS }
            )
        }
        ScreenState.SETTINGS -> {
            BackHandler {
                currentScreen = ScreenState.MAIN_TABS
            }
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = ScreenState.MAIN_TABS },
                onOpenStaffManagement = { currentScreen = ScreenState.STAFF_MANAGEMENT }
            )
        }
        ScreenState.MAIN_TABS -> {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        MainTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        MainTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToPos = { selectedTab = MainTab.POS },
                            onNavigateToStock = { selectedTab = MainTab.STOCK },
                            onNavigateToDues = { selectedTab = MainTab.DUES },
                            onNavigateToExpenses = { selectedTab = MainTab.EXPENSES },
                            onOpenSettings = { currentScreen = ScreenState.SETTINGS },
                            onOpenStaffManagement = { currentScreen = ScreenState.STAFF_MANAGEMENT }
                        )
                        MainTab.POS -> {
                            if (viewModel.canSell) {
                                PosBillingScreen(viewModel = viewModel)
                            } else {
                                RestrictedAccessScreen(
                                    sectionTitle = "বিক্রি ও ক্যাশমেমো",
                                    hint = "আপনার আইডিতে 'বিক্রি ও ডিজিটাল ক্যাশমেমো' অনুমতি নেই। অনুমতি পেতে দোকানের মালিকের সাথে যোগাযোগ করুন।"
                                )
                            }
                        }
                        MainTab.STOCK -> {
                            if (viewModel.canManageStock) {
                                ProductsScreen(viewModel = viewModel)
                            } else {
                                RestrictedAccessScreen(
                                    sectionTitle = "পণ্য ও স্টক ইনভেন্টরি",
                                    hint = "আপনার আইডিতে 'পণ্য ও ইনভেন্টরি স্টক' দেখার বা পরিবর্তনের অনুমতি নেই।"
                                )
                            }
                        }
                        MainTab.DUES -> {
                            if (viewModel.canManageDues) {
                                CustomersScreen(viewModel = viewModel)
                            } else {
                                RestrictedAccessScreen(
                                    sectionTitle = "বাকি খাতা",
                                    hint = "আপনার আইডিতে 'বাকি খাতা ও কালেকশন' দেখার অনুমতি নেই।"
                                )
                            }
                        }
                        MainTab.EXPENSES -> {
                            if (viewModel.canManageExpenses) {
                                ExpensesScreen(viewModel = viewModel)
                            } else {
                                RestrictedAccessScreen(
                                    sectionTitle = "দোকানের খরচ",
                                    hint = "আপনার আইডিতে 'দোকানের খরচ হিসাব' দেখার অনুমতি নেই।"
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
fun RestrictedAccessScreen(sectionTitle: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "অনুমতি সীমাবদ্ধ ($sectionTitle)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

