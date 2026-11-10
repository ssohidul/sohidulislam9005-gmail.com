package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Customer
import com.example.model.CustomerTransaction
import com.example.model.ShopProfile
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: DokanViewModel
) {
    val customers by viewModel.customers.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForLedger by remember { mutableStateOf<Customer?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val totalDues = customers.sumOf { it.totalDue }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DokanGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বাকি খাতা ও খদ্দের হিসাব", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = DokanGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Total Outstanding Dues Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট বাকি পাওনা",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", totalDues)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                    }
                    Text(
                        text = "মোট ${customers.size} জন খদ্দের",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("customer_search_input"),
                placeholder = { Text("খদ্দেরের নাম বা মোবাইল নম্বর দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (customers.isEmpty()) "কোন খদ্দেরের হিসাব নেই। নিচে (+) বাটনে চাপ দিয়ে প্রথম খদ্দের যোগ করুন।" else "কোন খদ্দের মেলেনি।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        CustomerCard(
                            customer = cust,
                            currency = shopProfile.currency,
                            onClick = { selectedCustomerForLedger = cust },
                            onCall = {
                                if (cust.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onRemind = {
                                sendDueReminder(context, cust, shopProfile)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            currency = shopProfile.currency,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { newCust ->
                viewModel.addCustomer(
                    customer = newCust,
                    onSuccess = {
                        showAddCustomerDialog = false
                        scope.launch { snackbarHostState.showSnackbar("নতুন খদ্দের যুক্ত হয়েছে") }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }

    // Customer Ledger Sheet
    selectedCustomerForLedger?.let { cust ->
        CustomerLedgerSheet(
            customer = cust,
            shopProfile = shopProfile,
            viewModel = viewModel,
            onDismiss = { selectedCustomerForLedger = null },
            onRefreshCustomer = { updatedDue ->
                selectedCustomerForLedger = cust.copy(totalDue = updatedDue)
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    currency: String,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onRemind: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    color = DokanGreenPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customer.name.firstOrNull()?.toString() ?: "C",
                            fontWeight = FontWeight.Bold,
                            color = DokanGreenPrimary,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency ${String.format(Locale.US, "%.0f", customer.totalDue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (customer.totalDue > 0) DueOrange else ProfitGreen
                )
                Text(
                    text = if (customer.totalDue > 0) "বাকি পাওনা" else "পরিশোধিত",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (customer.totalDue > 0) DueOrange else ProfitGreen
                )

                if (customer.totalDue > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        IconButton(onClick = onCall, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = DokanGreenPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRemind, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Message, contentDescription = "Remind", tint = DueOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerLedgerSheet(
    customer: Customer,
    shopProfile: ShopProfile,
    viewModel: DokanViewModel,
    onDismiss: () -> Unit,
    onRefreshCustomer: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val transactionsFlow = remember(customer.id) { viewModel.getCustomerTransactions(customer.id) }
    val transactions by transactionsFlow.collectAsState()

    var showAddDueDialog by remember { mutableStateOf(false) }
    var showCollectDueDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("মোবাইল: ${customer.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (customer.totalDue > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("বর্তমান বাকি পাওনা:", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", customer.totalDue)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (customer.totalDue > 0) DueOrange else ProfitGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: বাকি দেওয়া (+) ও বাকি আদায় (-)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDueDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DueOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ বাকি দিন")
                }
                Button(
                    onClick = { showCollectDueDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("- বাকি আদায়")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction History Title
            Text("খাতার বিস্তারিত বিবরণী (Ledger History):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("কোন লেনদেন বিবরণী পাওয়া যায়নি", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        val dateStr = tx.createdAt?.toDate()?.let {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
                        } ?: "সাম্প্রতিক"
                        val isDueGiven = tx.type == "DUE_GIVEN"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isDueGiven) "বাকি দেওয়া হয়েছে" else "বাকি জমা নেওয়া হয়েছে",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDueGiven) DueOrange else ProfitGreen
                                    )
                                    if (tx.note.isNotBlank()) {
                                        Text(tx.note, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isDueGiven) "+" else "-"} ${shopProfile.currency} ${String.format(Locale.US, "%.0f", tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDueGiven) DueOrange else ProfitGreen
                                    )
                                    Text(
                                        text = "অবশিষ্ট: ${shopProfile.currency} ${String.format(Locale.US, "%.0f", tx.balanceAfter)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Add Due Dialog
    if (showAddDueDialog) {
        AmountNoteDialog(
            title = "বাকি দিন — ${customer.name}",
            actionLabel = "বাকি নিশ্চিত করুন",
            currency = shopProfile.currency,
            onDismiss = { showAddDueDialog = false },
            onConfirm = { amount, note ->
                viewModel.addCustomerTransaction(
                    customerId = customer.id,
                    type = "DUE_GIVEN",
                    amount = amount,
                    note = note.ifBlank { "বাকি কেনাকাটা" },
                    onSuccess = {
                        showAddDueDialog = false
                        onRefreshCustomer(customer.totalDue + amount)
                    },
                    onError = {}
                )
            }
        )
    }

    // Collect Due Dialog
    if (showCollectDueDialog) {
        AmountNoteDialog(
            title = "বাকি আদায় / জমা — ${customer.name}",
            actionLabel = "জমা নিশ্চিত করুন",
            currency = shopProfile.currency,
            suggestedAmount = customer.totalDue,
            onDismiss = { showCollectDueDialog = false },
            onConfirm = { amount, note ->
                viewModel.addCustomerTransaction(
                    customerId = customer.id,
                    type = "DUE_COLLECTED",
                    amount = amount,
                    note = note.ifBlank { "বাকি পরিশোধ" },
                    onSuccess = {
                        showCollectDueDialog = false
                        onRefreshCustomer(maxOf(0.0, customer.totalDue - amount))
                    },
                    onError = {}
                )
            }
        )
    }
}

@Composable
private fun AddCustomerDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDue by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("নতুন খদ্দের যুক্ত করুন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("খদ্দেরের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_phone_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা / গ্রাম (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = initialDue,
                    onValueChange = { initialDue = it },
                    label = { Text("পূর্বের বাকি টাকা ($currency)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = "খদ্দেরের নাম দিন"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMsg = "মোবাইল নম্বর দিন"
                                return@Button
                            }
                            val due = initialDue.toDoubleOrNull() ?: 0.0
                            val cust = Customer(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                totalDue = due
                            )
                            onSave(cust)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                        modifier = Modifier.testTag("save_customer_button")
                    ) {
                        Text("সংরক্ষণ")
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountNoteDialog(
    title: String,
    actionLabel: String,
    currency: String,
    suggestedAmount: Double = 0.0,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (suggestedAmount > 0) suggestedAmount.toInt().toString() else "") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("টাকার পরিমাণ ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট / বিবরণ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "সঠিক টাকার পরিমাণ দিন"
                                return@Button
                            }
                            onConfirm(amt, note.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary)
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

fun sendDueReminder(context: Context, customer: Customer, shop: ShopProfile) {
    val message = "শ্রদ্ধেয় ${customer.name}, '${shop.shopName.ifBlank { "দোকান" }}' হতে জানানো যাচ্ছে যে, আপনার বকেয়া বাকি আছে ${shop.currency} ${String.format(Locale.US, "%.0f", customer.totalDue)} টাকা। অনুগ্রহ করে বকেয়া পরিশোধ করার জন্য বিনীত অনুরোধ করা হচ্ছে। ধন্যবাদ।"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${customer.phone}")
        putExtra("sms_body", message)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "বাকি তাগাদা মেসেজ পাঠান"))
    }
}
