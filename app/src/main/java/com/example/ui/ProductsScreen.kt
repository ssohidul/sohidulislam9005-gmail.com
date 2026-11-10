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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.Product
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: DokanViewModel
) {
    val products by viewModel.products.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব") }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productForStockAdjust by remember { mutableStateOf<Product?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categories = remember(products) {
        listOf("সব") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesCategory = selectedCategory == "সব" || prod.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = DokanGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পণ্য ও স্টক ব্যবস্থাপনা", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DokanGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("product_search_input"),
                placeholder = { Text("পণ্যের নাম বা বারকোড খুঁজুন...") },
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

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (products.isEmpty()) "কোন পণ্য যোগ করা হয়নি। নিচে (+) বাটনে চাপ দিয়ে প্রথম পণ্য যুক্ত করুন।" else "কোন পণ্য মেলেনি।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { prod ->
                        ProductItemCard(
                            product = prod,
                            currency = shopProfile.currency,
                            onEdit = { productToEdit = prod },
                            onDelete = { productToDelete = prod },
                            onAdjustStock = { productForStockAdjust = prod }
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        ProductFormDialog(
            initialProduct = productToEdit,
            currency = shopProfile.currency,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { product ->
                if (productToEdit != null) {
                    viewModel.updateProduct(
                        product = product,
                        onSuccess = {
                            showAddDialog = false
                            productToEdit = null
                            scope.launch { snackbarHostState.showSnackbar("পণ্য আপডেট হয়েছে") }
                        },
                        onError = { err ->
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                } else {
                    viewModel.addProduct(
                        product = product,
                        onSuccess = {
                            showAddDialog = false
                            scope.launch { snackbarHostState.showSnackbar("নতুন পণ্য যুক্ত হয়েছে") }
                        },
                        onError = { err ->
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                }
            }
        )
    }

    // Stock Adjust Dialog
    productForStockAdjust?.let { prod ->
        StockAdjustDialog(
            product = prod,
            onDismiss = { productForStockAdjust = null },
            onUpdateStock = { newStock ->
                viewModel.updateProduct(
                    product = prod.copy(stockQuantity = newStock),
                    onSuccess = {
                        productForStockAdjust = null
                        scope.launch { snackbarHostState.showSnackbar("স্টক আপডেট হয়েছে") }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }

    // Delete Product Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("পণ্য ডিলিট নিশ্চিতকরণ") },
            text = { Text("'${prod.name}' পণ্যটি তালিকা থেকে মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(
                            productId = prod.id,
                            onSuccess = {
                                productToDelete = null
                                scope.launch { snackbarHostState.showSnackbar("পণ্য মুছে ফেলা হয়েছে") }
                            },
                            onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ডিলিট")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: Product,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.lowStockThreshold
    val profitPerUnit = product.sellPrice - product.buyPrice

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ক্যাটাগরি: ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DokanGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "বিক্রয় মূল্য: $currency ${String.format(Locale.US, "%.2f", product.sellPrice)} / ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ক্রয়: $currency ${String.format(Locale.US, "%.2f", product.buyPrice)} • লাভ: $currency ${String.format(Locale.US, "%.2f", profitPerUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfitGreen
                    )
                }

                // Stock Badge (Clickable to adjust stock)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    modifier = Modifier.clickable { onAdjustStock() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        if (isLowStock) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${product.stockQuantity.toInt()} ${product.unit}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFC62828) else DokanGreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initialProduct: Product?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "মুদি (Grocery)") }
    var buyPrice by remember { mutableStateOf(initialProduct?.buyPrice?.toString() ?: "") }
    var sellPrice by remember { mutableStateOf(initialProduct?.sellPrice?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "পিস") }
    var lowStockThreshold by remember { mutableStateOf(initialProduct?.lowStockThreshold?.toString() ?: "5") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (initialProduct != null) "পণ্য সম্পাদনা" else "নতুন পণ্য যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("ক্যাটাগরি") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক (পিস/কেজি)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it },
                        label = { Text("ক্রয় মূল্য ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        label = { Text("বিক্রয় মূল্য ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_sell_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("বর্তমান স্টক *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lowStockThreshold,
                        onValueChange = { lowStockThreshold = it },
                        label = { Text("সতর্কতা সীমা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "পণ্যের নাম দিন"
                                return@Button
                            }
                            val bPrice = buyPrice.toDoubleOrNull() ?: 0.0
                            val sPrice = sellPrice.toDoubleOrNull()
                            if (sPrice == null || sPrice <= 0) {
                                errorMessage = "সঠিক বিক্রয় মূল্য দিন"
                                return@Button
                            }
                            val stock = stockQuantity.toDoubleOrNull() ?: 0.0
                            val threshold = lowStockThreshold.toDoubleOrNull() ?: 5.0

                            val updated = (initialProduct ?: Product()).copy(
                                name = name.trim(),
                                category = category.trim().ifBlank { "সাধারণ" },
                                buyPrice = bPrice,
                                sellPrice = sPrice,
                                stockQuantity = stock,
                                unit = unit.trim().ifBlank { "পিস" },
                                lowStockThreshold = threshold,
                                barcode = barcode.trim()
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("সংরক্ষণ")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockAdjustDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdateStock: (Double) -> Unit
) {
    var stockValue by remember { mutableStateOf(product.stockQuantity.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("স্টক সমন্বয় — ${product.name}") },
        text = {
            Column {
                Text("বর্তমান স্টক: ${product.stockQuantity.toInt()} ${product.unit}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = stockValue,
                    onValueChange = { stockValue = it },
                    label = { Text("নতুন স্টক সংখ্যা") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStock = stockValue.toDoubleOrNull() ?: product.stockQuantity
                    onUpdateStock(newStock)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary)
            ) {
                Text("আপডেট")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
