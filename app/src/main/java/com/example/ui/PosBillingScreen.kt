package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.Customer
import com.example.model.Product
import com.example.model.Sale
import com.example.model.ShopProfile
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosBillingScreen(
    viewModel: DokanViewModel
) {
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val taxRate by viewModel.taxRate.collectAsState()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব") }
    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showClearCartConfirm by remember { mutableStateOf(false) }

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
                    prod.barcode.contains(searchQuery, ignoreCase = true) ||
                    prod.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val cartTotal = cart.sumOf { it.total }
    val cartItemCount = cart.sumOf { it.quantity.toInt() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DokanGreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = DokanGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "বিক্রি ও ডিজিটাল বিলিং (POS)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${shopProfile.shopName} • ক্যাশ ও বাকি হিসাব",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    if (cart.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearCartConfirm = true },
                            modifier = Modifier.testTag("pos_clear_cart_top_btn")
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear Cart",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = cart.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cart Items & Total Summary
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showCartSheet = true }
                                .padding(vertical = 4.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = DokanGreenPrimary) {
                                        Text("$cartItemCount", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = DokanGreenPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "কার্টে মোট (${cart.size} পদ)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", cartTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DokanGreenPrimary
                                )
                            }
                        }

                        // Action Buttons: Cart Review & Checkout
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCartSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("pos_view_cart_btn")
                            ) {
                                Text("কার্ট", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showCheckoutSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("open_checkout_button")
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("বিল ও পরিশোধ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input with Barcode Affordance
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("pos_search_input"),
                placeholder = { Text("পণ্যের নাম বা বারকোড দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DokanGreenPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = {
                            scope.launch { snackbarHostState.showSnackbar("বারকোড স্ক্যানার প্রস্তুত: ইনপুটে বারকোড টাইপ করতে পারেন") }
                        }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DokanGreenPrimary,
                    focusedLabelColor = DokanGreenPrimary
                )
            )

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val count = if (cat == "সব") products.size else products.count { it.category == cat }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = "$cat ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DokanGreenPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = DokanGreenPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (products.isEmpty()) "দোকানে এখনও কোন পণ্য যোগ করা হয়নি।" else "কোন পণ্য পাওয়া যায়নি।",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (products.isEmpty()) "নিচের 'স্টক' মেন্যু হতে নতুন পণ্য যুক্ত করুন।" else "ভিন্ন নাম বা বারকোড দিয়ে চেষ্টা করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 6.dp,
                        bottom = if (cart.isNotEmpty()) 88.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { prod ->
                        val inCartItem = cart.find { it.product.id == prod.id }
                        PosProductCard(
                            product = prod,
                            currency = shopProfile.currency,
                            cartQuantity = inCartItem?.quantity ?: 0.0,
                            onAddToCart = {
                                if (prod.stockQuantity > (inCartItem?.quantity ?: 0.0)) {
                                    viewModel.addToCart(prod)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("মজুদের বেশি কার্টে নেওয়া সম্ভব নয় (${prod.stockQuantity.toInt()} ${prod.unit})") }
                                }
                            },
                            onIncrement = {
                                val currentQty = inCartItem?.quantity ?: 0.0
                                if (prod.stockQuantity > currentQty) {
                                    viewModel.updateCartItemQuantity(prod.id, currentQty + 1.0)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("মজুদ শেষ (${prod.stockQuantity.toInt()} ${prod.unit})") }
                                }
                            },
                            onDecrement = {
                                val currentQty = inCartItem?.quantity ?: 0.0
                                viewModel.updateCartItemQuantity(prod.id, currentQty - 1.0)
                            }
                        )
                    }
                }
            }
        }
    }

    // Full Cart Management Sheet
    if (showCartSheet) {
        val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = cartSheetState
        ) {
            CartManagementSheetContent(
                cart = cart,
                currency = shopProfile.currency,
                onIncrement = { prod ->
                    val inCart = cart.find { it.product.id == prod.id }
                    if (prod.stockQuantity > (inCart?.quantity ?: 0.0)) {
                        viewModel.updateCartItemQuantity(prod.id, (inCart?.quantity ?: 0.0) + 1.0)
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("মজুদ শেষ (${prod.stockQuantity.toInt()} ${prod.unit})") }
                    }
                },
                onDecrement = { prod ->
                    val inCart = cart.find { it.product.id == prod.id }
                    viewModel.updateCartItemQuantity(prod.id, (inCart?.quantity ?: 0.0) - 1.0)
                },
                onRemove = { prodId -> viewModel.removeFromCart(prodId) },
                onUpdatePrice = { prodId, newPrice -> viewModel.updateCartItemCustomPrice(prodId, newPrice) },
                onClearCart = {
                    viewModel.clearCart()
                    showCartSheet = false
                },
                onProceedToCheckout = {
                    showCartSheet = false
                    showCheckoutSheet = true
                },
                onClose = { showCartSheet = false }
            )
        }
    }

    // Checkout & Tax Settlement Bottom Sheet
    if (showCheckoutSheet) {
        val checkoutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = checkoutSheetState
        ) {
            CheckoutSheetContent(
                viewModel = viewModel,
                customers = customers,
                shopProfile = shopProfile,
                onClose = { showCheckoutSheet = false },
                onSuccess = {
                    showCheckoutSheet = false
                },
                onError = { err ->
                    scope.launch { snackbarHostState.showSnackbar(err) }
                }
            )
        }
    }

    // Clear Cart Confirmation Alert
    if (showClearCartConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCartConfirm = false },
            title = { Text("কার্ট খালি করবেন?") },
            text = { Text("কার্টের সমস্ত পণ্য (${cartItemCount} টি) মুছে ফেলা হবে। আপনি কি নিশ্চিত?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearCartConfirm = false
                        scope.launch { snackbarHostState.showSnackbar("কার্ট খালি করা হয়েছে") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, খালি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Digital Cash Memo Dialog upon Completed Sale
    lastCompletedSale?.let { sale ->
        CashMemoDialog(
            sale = sale,
            shopProfile = shopProfile,
            onDismiss = { viewModel.dismissLastSaleDialog() }
        )
    }
}

// ---------------- Product Card with Reactive Stepper ----------------
@Composable
private fun PosProductCard(
    product: Product,
    currency: String,
    cartQuantity: Double,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isOutOfStock = product.stockQuantity <= 0
    val isLowStock = !isOutOfStock && product.stockQuantity <= product.lowStockThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pos_product_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cartQuantity > 0) DokanGreenPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (cartQuantity > 0) {
            androidx.compose.foundation.BorderStroke(1.5.dp, DokanGreenPrimary.copy(alpha = 0.6f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (cartQuantity > 0) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Product Information
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stock Status Badge
                    when {
                        isOutOfStock -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "স্টক নেই",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        isLowStock -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DueOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "সীমিত: ${product.stockQuantity.toInt()} ${product.unit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DueOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "মজুদ: ${product.stockQuantity.toInt()} ${product.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currency ${String.format(Locale.US, "%.2f", product.sellPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DokanGreenPrimary
                    )
                    Text(
                        text = " / ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action: Add Button or Reactive Stepper
            if (cartQuantity <= 0) {
                Button(
                    onClick = onAddToCart,
                    enabled = !isOutOfStock,
                    colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_to_cart_btn_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add to Cart", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("কার্ট +", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.border(1.dp, DokanGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Minus",
                                tint = DokanGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${cartQuantity.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DokanGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = onIncrement,
                            enabled = product.stockQuantity > cartQuantity,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Plus",
                                tint = if (product.stockQuantity > cartQuantity) DokanGreenPrimary else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Cart Management Sheet Content ----------------
@Composable
private fun CartManagementSheetContent(
    cart: List<CartItem>,
    currency: String,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onRemove: (String) -> Unit,
    onUpdatePrice: (String, Double) -> Unit,
    onClearCart: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onClose: () -> Unit
) {
    var editingPriceProductId by remember { mutableStateOf<String?>(null) }
    var priceInput by remember { mutableStateOf("") }

    val subtotal = cart.sumOf { it.total }
    val totalUnits = cart.sumOf { it.quantity.toInt() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "কার্ট ব্যবস্থাপনা",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মোট $totalUnits টি পণ্য (${cart.size} টি আইটেম)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row {
                TextButton(
                    onClick = onClearCart,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সব মুছুন")
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Cart Items List
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .height(280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cart, key = { it.product.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%.2f", item.customPrice)} x ${item.quantity.toInt()} ${item.product.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        editingPriceProductId = item.product.id
                                        priceInput = item.customPrice.toString()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = DokanGreenPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        // Line Total & Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$currency ${String.format(Locale.US, "%.2f", item.total)}",
                                fontWeight = FontWeight.Bold,
                                color = DokanGreenPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            // Compact Stepper
                            IconButton(onClick = { onDecrement(item.product) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                            }
                            Text("${item.quantity.toInt()}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onIncrement(item.product) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                            }

                            IconButton(onClick = { onRemove(item.product.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Edit Custom Price Dialog
        editingPriceProductId?.let { prodId ->
            AlertDialog(
                onDismissRequest = { editingPriceProductId = null },
                title = { Text("দর বা একক মূল্য পরিবর্তন") },
                text = {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("নতুন একক মূল্য (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val newP = priceInput.toDoubleOrNull() ?: 0.0
                        if (newP >= 0) {
                            onUpdatePrice(prodId, newP)
                        }
                        editingPriceProductId = null
                    }) {
                        Text("সংরক্ষণ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingPriceProductId = null }) { Text("বাতিল") }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Running Subtotal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("মোট মূল্য (Subtotal):", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$currency ${String.format(Locale.US, "%.2f", subtotal)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DokanGreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onProceedToCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("পরবর্তী: ভ্যাট ও বিল পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ---------------- Checkout & Tax Settlement Sheet Content ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutSheetContent(
    viewModel: DokanViewModel,
    customers: List<Customer>,
    shopProfile: ShopProfile,
    onClose: () -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val discount by viewModel.discount.collectAsState()
    val taxRate by viewModel.taxRate.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()

    var discountInput by remember { mutableStateOf(if (discount > 0) discount.toString() else "") }
    var customTaxInput by remember { mutableStateOf("") }
    var isCustomTaxActive by remember { mutableStateOf(false) }
    var paidInput by remember { mutableStateOf("") }
    var isCustomerMenuExpanded by remember { mutableStateOf(false) }

    val subtotal = cart.sumOf { it.total }
    val discValue = discountInput.toDoubleOrNull() ?: 0.0
    val taxableAmount = maxOf(0.0, subtotal - discValue)
    val effectiveTaxRate = if (isCustomTaxActive) {
        customTaxInput.toDoubleOrNull() ?: 0.0
    } else {
        taxRate
    }
    val taxAmount = taxableAmount * (effectiveTaxRate / 100.0)
    val grandTotal = maxOf(0.0, taxableAmount + taxAmount)

    val paidValue = paidInput.toDoubleOrNull() ?: grandTotal
    val changeToReturn = maxOf(0.0, paidValue - grandTotal)
    val dueAmount = maxOf(0.0, grandTotal - paidValue)

    val paymentMethods = listOf("নগদ (Cash)", "বিকাশ (bKash)", "নগদ (Nagad)", "বাকি (Due)", "মিশ্র (Mixed)")
    val taxPresets = listOf(0.0 to "০% (কর নেই)", 5.0 to "৫% ভ্যাট (খুচরা)", 7.5 to "৭.৫% ভ্যাট", 15.0 to "১৫% ভ্যাট")
    val discountPresets = listOf(10.0 to "+৳১০", 20.0 to "+৳২০", 50.0 to "+৳৫০", 100.0 to "+৳১০০")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ক্যাশমেমো ও পরিশোধ বিল", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("ভ্যাট, ছাড় ও ক্যাশ হিসাব", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Customer Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = isCustomerMenuExpanded,
            onExpandedChange = { isCustomerMenuExpanded = !isCustomerMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "ক্যাশ খদ্দের (Walk-in Buyer)",
                onValueChange = {},
                readOnly = true,
                label = { Text("খদ্দের / ক্রেতা নির্বাচন করুন") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCustomerMenuExpanded) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DokanGreenPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = isCustomerMenuExpanded,
                onDismissRequest = { isCustomerMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("ক্যাশ খদ্দের (Walk-in Buyer)", fontWeight = FontWeight.Bold) },
                    onClick = {
                        viewModel.selectCustomerForSale(null)
                        isCustomerMenuExpanded = false
                    }
                )
                customers.forEach { cust ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(cust.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "মোবাইল: ${cust.phone} • পূর্বের বাকি: ৳${cust.totalDue.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (cust.totalDue > 0) DueOrange else Color.Gray
                                )
                            }
                        },
                        onClick = {
                            viewModel.selectCustomerForSale(cust)
                            isCustomerMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cart Summary Glance
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "নির্বাচিত পণ্যসমূহ (${cart.size} টি পদ, ${cart.sumOf { it.quantity.toInt() }} একক):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                cart.take(4).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.product.name} x ${item.quantity.toInt()} ${item.product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", item.total)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (cart.size > 4) {
                    Text("...এবং আরও ${cart.size - 4} টি পণ্য", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Financial Inputs: Discount (ছাড়)
        Text("১. মূল্যছাড় (Discount):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = discountInput,
                onValueChange = {
                    discountInput = it
                    viewModel.setDiscount(it.toDoubleOrNull() ?: 0.0)
                },
                placeholder = { Text("০.০০") },
                label = { Text("ছাড়ের পরিমাণ (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            // Clear discount button
            if (discValue > 0) {
                IconButton(onClick = {
                    discountInput = ""
                    viewModel.setDiscount(0.0)
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Discount", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Quick Discount Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(discountPresets) { (amount, label) ->
                FilterChip(
                    selected = discValue == amount,
                    onClick = {
                        discountInput = amount.toInt().toString()
                        viewModel.setDiscount(amount)
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Financial Inputs: Tax / VAT (ভ্যাট ও কর)
        Text("২. ভ্যাট ও সরকারি কর (Tax / VAT):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        // Tax Preset Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(taxPresets) { (rate, label) ->
                val isSelected = !isCustomTaxActive && effectiveTaxRate == rate
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        isCustomTaxActive = false
                        viewModel.setTaxRate(rate)
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DokanGreenPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = DokanGreenPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = isCustomTaxActive,
                    onClick = { isCustomTaxActive = true },
                    label = { Text("কাস্টম %", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        if (isCustomTaxActive) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = customTaxInput,
                onValueChange = {
                    customTaxInput = it
                    val rate = it.toDoubleOrNull() ?: 0.0
                    viewModel.setTaxRate(rate)
                },
                placeholder = { Text("উদা: ১০%") },
                label = { Text("কাস্টম ভ্যাট হার (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Icon(Icons.Default.Percent, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payment Method Selection
        Text("৩. পরিশোধের মাধ্যম (Payment Method):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(paymentMethods) { method ->
                FilterChip(
                    selected = paymentMethod == method,
                    onClick = { viewModel.setPaymentMethod(method) },
                    label = { Text(method, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DokanGreenPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = DokanGreenPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tender Amount Field (জমা টাকা)
        Text("৪. জমা ও ক্যাশ হিসাব (Tender / Cash Received):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = paidInput,
            onValueChange = {
                paidInput = it
                viewModel.setPaidAmount(it.toDoubleOrNull())
            },
            placeholder = { Text(String.format(Locale.US, "%.0f", grandTotal)) },
            label = { Text("গ্রাহকের নিকট হতে জমা টাকা (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // Quick Tender Shortcuts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = paidValue == grandTotal,
                    onClick = {
                        paidInput = String.format(Locale.US, "%.0f", grandTotal)
                        viewModel.setPaidAmount(grandTotal)
                    },
                    label = { Text("ঠিক পরিমাণ (${grandTotal.toInt()}৳)") }
                )
            }
            listOf(50, 100, 500, 1000).forEach { note ->
                val roundAmount = ((grandTotal / note).toInt() + 1) * note
                if (roundAmount > grandTotal) {
                    item {
                        FilterChip(
                            selected = paidValue == roundAmount.toDouble(),
                            onClick = {
                                paidInput = roundAmount.toString()
                                viewModel.setPaidAmount(roundAmount.toDouble())
                            },
                            label = { Text("$roundAmount ৳") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Final Bill Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Subtotal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মোট মূল্য (Subtotal):", style = MaterialTheme.typography.bodyMedium)
                    Text("${shopProfile.currency} ${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium)
                }

                // Discount
                if (discValue > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মূল্যছাড় (Discount):", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                        Text("- ${shopProfile.currency} ${String.format(Locale.US, "%.2f", discValue)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                    }
                }

                // Tax / VAT
                if (taxAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ভ্যাট ও কর (${effectiveTaxRate.toInt()}% VAT):", style = MaterialTheme.typography.bodyMedium, color = DokanGreenPrimary)
                        Text("+ ${shopProfile.currency} ${String.format(Locale.US, "%.2f", taxAmount)}", style = MaterialTheme.typography.bodyMedium, color = DokanGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Grand Total
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট প্রদেয় বিল (Net Total):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${shopProfile.currency} ${String.format(Locale.US, "%.2f", grandTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DokanGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tender Result: Change to return or Due remaining
                if (changeToReturn > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProfitGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ফেরত দিন (Change Return):", fontWeight = FontWeight.Bold, color = ProfitGreen)
                        Text("${shopProfile.currency} ${String.format(Locale.US, "%.2f", changeToReturn)}", fontWeight = FontWeight.ExtraBold, color = ProfitGreen)
                    }
                } else if (dueAmount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DueOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("বাকি থাকবে (Due):", fontWeight = FontWeight.Bold, color = DueOrange)
                        Text("${shopProfile.currency} ${String.format(Locale.US, "%.2f", dueAmount)}", fontWeight = FontWeight.ExtraBold, color = DueOrange)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm & Record Sale Button
        Button(
            onClick = {
                viewModel.completeSale(
                    onSuccess = {
                        onSuccess()
                    },
                    onError = { err ->
                        onError(err)
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_sale_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ক্যাশমেমো প্রিন্ট ও নিশ্চিত করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
