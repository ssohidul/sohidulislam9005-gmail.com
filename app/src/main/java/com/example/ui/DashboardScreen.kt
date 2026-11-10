package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Sale
import com.example.ui.theme.CashBlue
import com.example.ui.theme.DokanGoldSecondary
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DokanViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToDues: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStaffManagement: () -> Unit
) {
    val shopProfile by viewModel.shopProfile.collectAsState()
    val session by viewModel.session.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val products by viewModel.products.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var selectedMemoSale by remember { mutableStateOf<Sale?>(null) }

    val lowStockProducts = remember(products) {
        products.filter { it.stockQuantity <= it.lowStockThreshold }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = DokanGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shopProfile.shopName.ifBlank { "NSB দোকান সেবা" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (viewModel.isOwner) DokanGreenPrimary.copy(alpha = 0.15f) else Color(0xFF1565C0).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = session.roleTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.isOwner) DokanGreenPrimary else Color(0xFF1565C0),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (shopProfile.ownerName.isNotBlank()) "প্রোঃ ${shopProfile.ownerName}" else "ব্যবসার হিসাব ও পিওএস",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Staff & Multi-login capacity banner
            item {
                if (viewModel.canManageStaff) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = DokanGreenPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Group, contentDescription = null, tint = DokanGreenPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "কর্মী, ম্যানেজার ও পার্টনার (৫০ জন)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "কোড: ${shopProfile.storeCode.ifBlank { "..." }} • কর্মী: ${staffMembers.size}/৫০",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Button(
                                onClick = onOpenStaffManagement,
                                colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                                modifier = Modifier.testTag("dashboard_manage_staff_button")
                            ) {
                                Text("পরিচালনা", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1565C0))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "লগইন: ${session.staffName} (${session.userRole})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1)
                                    )
                                    Text(
                                        text = "দোকান: ${session.shopName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.switchBackToOwnerShop() }
                            ) {
                                Text("মালিক মোড", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 1. Total Customer Outstanding Due Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (metrics.totalCustomerDue > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDues() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (metrics.totalCustomerDue > 0) DueOrange.copy(alpha = 0.15f) else ProfitGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Book,
                                        contentDescription = null,
                                        tint = if (metrics.totalCustomerDue > 0) DueOrange else ProfitGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "দোকানের মোট বাকি পাওনা",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", metrics.totalCustomerDue)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (metrics.totalCustomerDue > 0) DueOrange else ProfitGreen
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "View Dues",
                            tint = Color.Gray
                        )
                    }
                }
            }

            // 2. Today's Summary Dashboard Cards
            item {
                Text(
                    text = "আজকের হিসাব",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            title = "মোট বিক্রি",
                            amount = "${shopProfile.currency} ${String.format(Locale.US, "%.0f", metrics.todaySales)}",
                            icon = Icons.Default.PointOfSale,
                            accentColor = DokanGreenPrimary,
                            bgColor = Color(0xFFE8F5E9)
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            title = "নগদ আদায়",
                            amount = "${shopProfile.currency} ${String.format(Locale.US, "%.0f", metrics.todayCash)}",
                            icon = Icons.Default.AttachMoney,
                            accentColor = CashBlue,
                            bgColor = Color(0xFFE3F2FD)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            title = "নতুন বাকি",
                            amount = "${shopProfile.currency} ${String.format(Locale.US, "%.0f", metrics.todayDue)}",
                            icon = Icons.Default.Book,
                            accentColor = DueOrange,
                            bgColor = Color(0xFFFFF3E0)
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            title = "আজকের খরচ",
                            amount = "${shopProfile.currency} ${String.format(Locale.US, "%.0f", metrics.todayExpenses)}",
                            icon = Icons.Default.MoneyOff,
                            accentColor = ExpenseRed,
                            bgColor = Color(0xFFFFEBEE)
                        )
                    }

                    // Net Profit Banner Card (Visible to Owner, Partner, or staff with reports permission)
                    if (viewModel.canViewReports) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (metrics.todayNetProfit >= 0) DokanGreenPrimary else ExpenseRed
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "আজকের নিট লাভ (লাভ - খরচ)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", metrics.todayNetProfit)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Quick Action Shortcuts
            item {
                Text(
                    text = "দ্রুত কাজ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "নতুন বিক্রি\n(ক্যাশমেমো)",
                        icon = Icons.Default.PointOfSale,
                        color = DokanGreenPrimary,
                        onClick = onNavigateToPos,
                        testTag = "quick_pos_button"
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "বাকি খাতা\n(খদ্দের হিসাব)",
                        icon = Icons.Default.Book,
                        color = DueOrange,
                        onClick = onNavigateToDues,
                        testTag = "quick_dues_button"
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "পণ্য ও স্টক\n(ইনভেন্টরি)",
                        icon = Icons.Default.Inventory,
                        color = CashBlue,
                        onClick = onNavigateToStock,
                        testTag = "quick_stock_button"
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "খরচ খাতা\n(ব্যয় হিসাব)",
                        icon = Icons.Default.MoneyOff,
                        color = ExpenseRed,
                        onClick = onNavigateToExpenses,
                        testTag = "quick_expense_button"
                    )
                }
            }

            // 4. Low Stock Alerts Section (if any)
            if (lowStockProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF57F17),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "কম স্টকের সতর্কতা (${lowStockProducts.size}টি পণ্য)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57F17)
                                    )
                                }
                                Text(
                                    text = "সব দেখুন",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DokanGreenPrimary,
                                    modifier = Modifier.clickable { onNavigateToStock() }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            lowStockProducts.take(3).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFCDD2)
                                    ) {
                                        Text(
                                            text = "বাকি মাত্র: ${prod.stockQuantity.toInt()} ${prod.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB71C1C),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recent Sales Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক বিক্রি ও ক্যাশমেমো",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "মোট ${sales.size}টি",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (sales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "এখনও কোন বিক্রি রেকর্ড করা হয়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToPos,
                                colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary)
                            ) {
                                Text("প্রথম বিক্রি শুরু করুন")
                            }
                        }
                    }
                }
            } else {
                items(sales.take(8)) { sale ->
                    RecentSaleItem(
                        sale = sale,
                        currency = shopProfile.currency,
                        onClick = { selectedMemoSale = sale }
                    )
                }
            }
        }
    }

    selectedMemoSale?.let { sale ->
        CashMemoDialog(
            sale = sale,
            shopProfile = shopProfile,
            onDismiss = { selectedMemoSale = null }
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RecentSaleItem(
    sale: Sale,
    currency: String,
    onClick: () -> Unit
) {
    val dateStr = sale.createdAt?.toDate()?.let {
        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(it)
    } ?: "আজ"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = DokanGreenPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = DokanGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sale.invoiceNumber} • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = sale.itemsSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency ${String.format(Locale.US, "%.2f", sale.grandTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DokanGreenPrimary
                )
                if (sale.dueAmount > 0) {
                    Text(
                        text = "বাকি: $currency ${String.format(Locale.US, "%.0f", sale.dueAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DueOrange
                    )
                } else {
                    Text(
                        text = "পরিশোধিত",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfitGreen
                    )
                }
            }
        }
    }
}
