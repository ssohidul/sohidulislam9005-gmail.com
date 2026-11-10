package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DokanRepository
import com.example.model.CartItem
import com.example.model.CustomRole
import com.example.model.Customer
import com.example.model.CustomerTransaction
import com.example.model.Expense
import com.example.model.Product
import com.example.model.Sale
import com.example.model.ShopProfile
import com.example.model.StaffMember
import com.example.model.UserShopSession
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.model.DokanPermissions

data class DashboardMetrics(
    val todaySales: Double = 0.0,
    val todayCash: Double = 0.0,
    val todayDue: Double = 0.0,
    val todayProfit: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val totalCustomerDue: Double = 0.0,
    val lowStockCount: Int = 0
)

class DokanViewModel(
    private val repository: DokanRepository,
    val currentUserId: String
) : ViewModel() {

    // Active Shop Session (Owner, Partner, Manager or Staff)
    private val _session = MutableStateFlow(
        UserShopSession(
            shopOwnerId = currentUserId,
            userRole = "OWNER",
            roleTitle = "মূল মালিক",
            staffName = "মালিক",
            shopName = "আমার দোকান",
            permissions = DokanPermissions.ALL_PERMISSIONS
        )
    )
    val session: StateFlow<UserShopSession> = _session.asStateFlow()

    val isOwner: Boolean get() = _session.value.userRole == "OWNER" || _session.value.userRole == "CO_OWNER"
    val isManagerOrAbove: Boolean get() = _session.value.userRole in listOf("OWNER", "CO_OWNER", "PARTNER", "MANAGER")

    fun hasPermission(permKey: String): Boolean = _session.value.hasPermission(permKey)
    val canSell: Boolean get() = hasPermission(DokanPermissions.PERM_POS_SALES)
    val canManageDues: Boolean get() = hasPermission(DokanPermissions.PERM_DUES_BOOK)
    val canManageStock: Boolean get() = hasPermission(DokanPermissions.PERM_STOCK_MANAGE)
    val canManageExpenses: Boolean get() = hasPermission(DokanPermissions.PERM_EXPENSES)
    val canViewReports: Boolean get() = hasPermission(DokanPermissions.PERM_REPORTS)
    val canManageSettings: Boolean get() = hasPermission(DokanPermissions.PERM_SETTINGS)
    val canManageStaff: Boolean get() = isOwner || hasPermission(DokanPermissions.PERM_STAFF_MANAGE)

    // Reactive streams bound to current session's shopOwnerId
    val shopProfile: StateFlow<ShopProfile> = _session
        .flatMapLatest { sess -> repository.observeShopProfile(sess.shopOwnerId) }
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShopProfile()
        ).let { flow ->
            MutableStateFlow(ShopProfile()).also { stateFlow ->
                viewModelScope.launch {
                    flow.collect { profile ->
                        if (profile != null) {
                            stateFlow.value = profile
                        }
                    }
                }
            }
        }

    val products: StateFlow<List<Product>> = _session
        .flatMapLatest { sess -> repository.observeProducts(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<Customer>> = _session
        .flatMapLatest { sess -> repository.observeCustomers(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sales: StateFlow<List<Sale>> = _session
        .flatMapLatest { sess -> repository.observeSales(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenses: StateFlow<List<Expense>> = _session
        .flatMapLatest { sess -> repository.observeExpenses(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val staffMembers: StateFlow<List<StaffMember>> = _session
        .flatMapLatest { sess -> repository.observeStaffMembers(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customRoles: StateFlow<List<CustomRole>> = _session
        .flatMapLatest { sess -> repository.observeCustomRoles(sess.shopOwnerId) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        products, customers, sales, expenses
    ) { prods, custs, allSales, allExpenses ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        val todaySalesList = allSales.filter { sale ->
            val time = sale.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            time >= startOfDay
        }

        val todayExpenseList = allExpenses.filter { exp ->
            val time = exp.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            time >= startOfDay
        }

        val totalSales = todaySalesList.sumOf { it.grandTotal }
        val totalCash = todaySalesList.sumOf { it.paidAmount }
        val totalDue = todaySalesList.sumOf { it.dueAmount }
        val totalProfit = todaySalesList.sumOf { it.profit }
        val totalExp = todayExpenseList.sumOf { it.amount }
        val netProfit = totalProfit - totalExp

        val totalAllCustDue = custs.sumOf { it.totalDue }
        val lowStock = prods.count { it.stockQuantity <= it.lowStockThreshold }

        DashboardMetrics(
            todaySales = totalSales,
            todayCash = totalCash,
            todayDue = totalDue,
            todayProfit = totalProfit,
            todayExpenses = totalExp,
            todayNetProfit = netProfit,
            totalCustomerDue = totalAllCustDue,
            lowStockCount = lowStock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    // ---------------- POS Cart State ----------------
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _taxRate = MutableStateFlow(0.0)
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()

    private val _paidAmount = MutableStateFlow<Double?>(null)
    val paidAmount: StateFlow<Double?> = _paidAmount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("নগদ (Cash)")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _lastCompletedSale = MutableStateFlow<Sale?>(null)
    val lastCompletedSale: StateFlow<Sale?> = _lastCompletedSale.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            current.add(CartItem(product = product, quantity = 1.0, customPrice = product.sellPrice))
        }
        _cart.value = current
    }

    fun updateCartItemQuantity(productId: String, newQty: Double) {
        if (newQty <= 0) {
            removeFromCart(productId)
            return
        }
        _cart.value = _cart.value.map {
            if (it.product.id == productId) it.copy(quantity = newQty) else it
        }
    }

    fun updateCartItemCustomPrice(productId: String, newPrice: Double) {
        if (newPrice < 0) return
        _cart.value = _cart.value.map {
            if (it.product.id == productId) it.copy(customPrice = newPrice) else it
        }
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomer.value = null
        _discount.value = 0.0
        _taxRate.value = 0.0
        _paidAmount.value = null
        _paymentMethod.value = "নগদ (Cash)"
    }

    fun selectCustomerForSale(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setDiscount(amount: Double) {
        _discount.value = maxOf(0.0, amount)
    }

    fun setTaxRate(rate: Double) {
        _taxRate.value = maxOf(0.0, rate)
    }

    fun setPaidAmount(amount: Double?) {
        _paidAmount.value = amount
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun completeSale(onSuccess: (Sale) -> Unit, onError: (String) -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) {
            onError("কার্টে কোন পণ্য নেই")
            return
        }
        val subtotal = items.sumOf { it.total }
        val disc = _discount.value
        val taxableAmount = maxOf(0.0, subtotal - disc)
        val rate = _taxRate.value
        val taxAmount = taxableAmount * (rate / 100.0)
        val grandTotal = maxOf(0.0, taxableAmount + taxAmount)
        val customer = _selectedCustomer.value

        val paid = _paidAmount.value ?: grandTotal
        val due = maxOf(0.0, grandTotal - paid)

        if (due > 0 && customer == null) {
            onError("বাকি থাকলে গ্রাহক নির্বাচন করা আবশ্যক")
            return
        }

        val invoiceNumber = "INV-${System.currentTimeMillis() % 1000000}"
        val itemsSummary = items.joinToString(", ") { "${it.product.name} (${it.quantity.toInt()} ${it.product.unit})" }
        val profit = items.sumOf { it.profit } - disc
        val staffName = _session.value.staffName

        val sale = Sale(
            userId = _session.value.shopOwnerId,
            invoiceNumber = invoiceNumber,
            customerId = customer?.id ?: "",
            customerName = customer?.name ?: "ক্যাশ খদ্দের",
            itemsSummary = itemsSummary,
            totalAmount = subtotal,
            discount = disc,
            taxRate = rate,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            paidAmount = paid,
            dueAmount = due,
            paymentMethod = _paymentMethod.value,
            profit = profit,
            staffName = staffName,
            createdAt = Timestamp.now()
        )

        viewModelScope.launch {
            try {
                repository.recordSale(_session.value.shopOwnerId, sale, items, staffName)
                _lastCompletedSale.value = sale
                clearCart()
                onSuccess(sale)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "বিক্রি সম্পন্ন করা যায়নি")
            }
        }
    }

    fun dismissLastSaleDialog() {
        _lastCompletedSale.value = null
    }

    // ---------------- Product Actions ----------------
    fun addProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addProduct(_session.value.shopOwnerId, product)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "পণ্য যোগ করা যায়নি")
            }
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateProduct(_session.value.shopOwnerId, product)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "পণ্য আপডেট করা যায়নি")
            }
        }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(_session.value.shopOwnerId, productId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "পণ্য ডিলিট করা যায়নি")
            }
        }
    }

    // ---------------- Customer & Dues Actions ----------------
    fun addCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addCustomer(_session.value.shopOwnerId, customer)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "গ্রাহক যোগ করা যায়নি")
            }
        }
    }

    fun addCustomerTransaction(
        customerId: String,
        type: String,
        amount: Double,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addCustomerTransaction(_session.value.shopOwnerId, customerId, type, amount, note, _session.value.staffName)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "লেনদেন সংরক্ষণ করা যায়নি")
            }
        }
    }

    fun getCustomerTransactions(customerId: String): StateFlow<List<CustomerTransaction>> {
        return repository.observeCustomerTransactions(_session.value.shopOwnerId, customerId)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // ---------------- Expense Actions ----------------
    fun addExpense(expense: Expense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addExpense(_session.value.shopOwnerId, expense)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "খরচ যোগ করা যায়নি")
            }
        }
    }

    fun deleteExpense(expenseId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(_session.value.shopOwnerId, expenseId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "খরচ ডিলিট করা যায়নি")
            }
        }
    }

    // ---------------- Profile & Staff Actions ----------------
    fun saveShopProfile(profile: ShopProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveShopProfile(_session.value.shopOwnerId, profile)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "দোকানের তথ্য সেভ করা যায়নি")
            }
        }
    }

    fun ensureStoreCode(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val code = repository.generateOrGetStoreCode(_session.value.shopOwnerId, shopProfile.value.shopName)
                onSuccess(code)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "দোকান কোড তৈরি করা যায়নি")
            }
        }
    }

    fun addStaffMember(staff: StaffMember, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentStaffCount = staffMembers.value.size
                if (currentStaffCount >= 50) {
                    onError("সর্বোচ্চ ৫০ জন কর্মচারী যুক্ত করা যাবে")
                    return@launch
                }
                repository.addStaffMember(_session.value.shopOwnerId, staff)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "কর্মচারী যুক্ত করা যায়নি")
            }
        }
    }

    fun updateStaffMember(staff: StaffMember, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateStaffMember(_session.value.shopOwnerId, staff)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "কর্মচারী আপডেট করা যায়নি")
            }
        }
    }

    fun deleteStaffMember(staffId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteStaffMember(_session.value.shopOwnerId, staffId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "কর্মচারী ডিলিট করা যায়নি")
            }
        }
    }

    fun addCustomRole(role: CustomRole, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addCustomRole(_session.value.shopOwnerId, role)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "রোল তৈরি করা যায়নি")
            }
        }
    }

    fun updateCustomRole(role: CustomRole, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCustomRole(_session.value.shopOwnerId, role)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "রোল আপডেট করা যায়নি")
            }
        }
    }

    fun deleteCustomRole(roleId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteCustomRole(_session.value.shopOwnerId, roleId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "রোল ডিলিট করা যায়নি")
            }
        }
    }

    fun joinShopAsStaff(storeCode: String, pin: String, onSuccess: (UserShopSession) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.joinShopWithCodeAndPin(storeCode, pin, currentUserId)
            result.fold(
                onSuccess = { newSession ->
                    _session.value = newSession
                    clearCart()
                    onSuccess(newSession)
                },
                onFailure = { err ->
                    onError(err.localizedMessage ?: "দোকানে লগইন করা যায়নি")
                }
            )
        }
    }

    fun switchBackToOwnerShop() {
        _session.value = UserShopSession(
            shopOwnerId = currentUserId,
            userRole = "OWNER",
            roleTitle = "মূল মালিক",
            staffName = "মালিক",
            shopName = shopProfile.value.shopName.ifBlank { "আমার দোকান" },
            storeCode = shopProfile.value.storeCode,
            permissions = DokanPermissions.ALL_PERMISSIONS
        )
        clearCart()
    }
}
