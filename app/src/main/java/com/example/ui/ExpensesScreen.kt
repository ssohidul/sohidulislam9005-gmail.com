package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.Expense
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: DokanViewModel
) {
    val expenses by viewModel.expenses.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfDay = calendar.timeInMillis

    val todayExpenses = remember(expenses) {
        expenses.filter { (it.createdAt?.toDate()?.time ?: System.currentTimeMillis()) >= startOfDay }
    }
    val todayTotal = todayExpenses.sumOf { it.amount }
    val allTotal = expenses.sumOf { it.amount }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MoneyOff, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("খরচ খাতা (ব্যয় হিসাব)", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ExpenseRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expense Overview Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("আজকের খরচ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                        Text(
                            text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", todayTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC62828)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("মোট সর্বমোট খরচ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", allTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "কোন খরচ রেকর্ড নেই। নিচে (+) বাটনে চাপ দিয়ে খরচ এন্ট্রি করুন।",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses) { exp ->
                        val dateStr = exp.createdAt?.toDate()?.let {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
                        } ?: "আজ"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ExpenseRed.copy(alpha = 0.1f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.MoneyOff, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(exp.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text("${exp.category} • $dateStr", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        if (exp.note.isNotBlank()) {
                                            Text(exp.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", exp.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ExpenseRed
                                    )
                                    IconButton(onClick = { expenseToDelete = exp }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        AddExpenseDialog(
            currency = shopProfile.currency,
            onDismiss = { showAddDialog = false },
            onSave = { newExp ->
                viewModel.addExpense(
                    expense = newExp,
                    onSuccess = {
                        showAddDialog = false
                        scope.launch { snackbarHostState.showSnackbar("খরচ এন্ট্রি সম্পন্ন হয়েছে") }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }

    // Delete Confirm
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("খরচ মুছে ফেলা") },
            text = { Text("'${exp.title}' (${shopProfile.currency} ${exp.amount}) খরচটি ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(
                            expenseId = exp.id,
                            onSuccess = {
                                expenseToDelete = null
                                scope.launch { snackbarHostState.showSnackbar("খরচ ডিলিট হয়েছে") }
                            },
                            onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ডিলিট")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("দোকান খরচ") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "চা-নাস্তা", "যাতায়াত", "পণ্য পরিবহন", "দোকান খরচ", "অন্যান্য")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("নতুন খরচ লিখুন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("খরচের শিরোনাম *") },
                    placeholder = { Text("যেমন: চা-নাস্তা বা বিদ্যুৎ বিল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
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
                            if (title.isBlank()) {
                                errorMsg = "খরচের শিরোনাম দিন"
                                return@Button
                            }
                            val amt = amount.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "সঠিক টাকার পরিমাণ দিন"
                                return@Button
                            }
                            onSave(
                                Expense(
                                    title = title.trim(),
                                    category = category,
                                    amount = amt,
                                    note = note.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        modifier = Modifier.testTag("save_expense_button")
                    ) {
                        Text("সংরক্ষণ")
                    }
                }
            }
        }
    }
}
