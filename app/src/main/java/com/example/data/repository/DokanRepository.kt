package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.model.CartItem
import com.example.model.CustomRole
import com.example.model.Customer
import com.example.model.CustomerTransaction
import com.example.model.DokanPermissions
import com.example.model.Expense
import com.example.model.Product
import com.example.model.Sale
import com.example.model.ShopProfile
import com.example.model.StaffMember
import com.example.model.StoreCodeEntry
import com.example.model.UserShopSession
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DokanRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // ---------------- Shop Profile & Store Code ----------------
    fun observeShopProfile(shopOwnerId: String): Flow<ShopProfile?> {
        val docRef = db.collection("users").document(shopOwnerId)
            .collection("shop_profile").document("profile")
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(ShopProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        }
    }

    suspend fun saveShopProfile(shopOwnerId: String, profile: ShopProfile) {
        val docRef = db.collection("users").document(shopOwnerId)
            .collection("shop_profile").document("profile")
        val exists = docRef.get().await().exists()
        val payload = mutableMapOf<String, Any>(
            "shopName" to profile.shopName,
            "ownerName" to profile.ownerName,
            "phone" to profile.phone,
            "currency" to profile.currency,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (profile.address.isNotBlank()) payload["address"] = profile.address
        if (profile.memoFooter.isNotBlank()) payload["memoFooter"] = profile.memoFooter
        if (profile.storeCode.isNotBlank()) payload["storeCode"] = profile.storeCode
        if (profile.memberUids.isNotEmpty()) payload["memberUids"] = profile.memberUids

        if (!exists) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (exists) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun generateOrGetStoreCode(shopOwnerId: String, shopName: String): String {
        val docRef = db.collection("users").document(shopOwnerId)
            .collection("shop_profile").document("profile")
        val snapshot = docRef.get().await()
        val existingCode = snapshot.getString("storeCode")
        if (!existingCode.isNullOrBlank()) {
            return existingCode
        }

        // Generate unique code e.g. NSB-4819
        val randomNum = (1000..9999).random()
        val newCode = "NSB-$randomNum"

        val codeRef = db.collection("store_codes").document(newCode)
        val codePayload = mapOf(
            "ownerId" to shopOwnerId,
            "shopName" to shopName.ifBlank { "NSB দোকান সেবা" },
            "code" to newCode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        codeRef.set(codePayload).await()

        docRef.set(mapOf("storeCode" to newCode, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge()).await()
        return newCode
    }

    // ---------------- Staff Management (Up to 50 members) ----------------
    fun observeStaffMembers(shopOwnerId: String): Flow<List<StaffMember>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("staff")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(StaffMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedBy { it.name }
        }
    }

    suspend fun addStaffMember(shopOwnerId: String, staff: StaffMember): String {
        val docRef = db.collection("users").document(shopOwnerId).collection("staff").document()
        val payload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "name" to staff.name,
            "role" to staff.role,
            "pin" to staff.pin,
            "permissions" to staff.permissions,
            "isActive" to staff.isActive,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (staff.customTitle.isNotBlank()) payload["customTitle"] = staff.customTitle
        if (staff.phone.isNotBlank()) payload["phone"] = staff.phone
        if (staff.assignedUid.isNotBlank()) payload["assignedUid"] = staff.assignedUid

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStaffMember(shopOwnerId: String, staff: StaffMember) {
        val docRef = db.collection("users").document(shopOwnerId).collection("staff").document(staff.id)
        val payload = mutableMapOf<String, Any>(
            "name" to staff.name,
            "role" to staff.role,
            "pin" to staff.pin,
            "permissions" to staff.permissions,
            "isActive" to staff.isActive
        )
        if (staff.customTitle.isNotBlank()) payload["customTitle"] = staff.customTitle
        if (staff.phone.isNotBlank()) payload["phone"] = staff.phone
        if (staff.assignedUid.isNotBlank()) payload["assignedUid"] = staff.assignedUid
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteStaffMember(shopOwnerId: String, staffId: String) {
        val docRef = db.collection("users").document(shopOwnerId).collection("staff").document(staffId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ---------------- Custom Roles ----------------
    fun observeCustomRoles(shopOwnerId: String): Flow<List<CustomRole>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("custom_roles")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(CustomRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedBy { it.createdAt }
        }
    }

    suspend fun addCustomRole(shopOwnerId: String, role: CustomRole): String {
        val docRef = db.collection("users").document(shopOwnerId).collection("custom_roles").document()
        val payload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "roleName" to role.roleName,
            "roleCode" to role.roleCode.ifBlank { role.roleName.replace(" ", "_").uppercase() },
            "permissions" to role.permissions,
            "colorHex" to role.colorHex,
            "isDefault" to role.isDefault,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (role.description.isNotBlank()) payload["description"] = role.description

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateCustomRole(shopOwnerId: String, role: CustomRole) {
        val docRef = db.collection("users").document(shopOwnerId).collection("custom_roles").document(role.id)
        val payload = mutableMapOf<String, Any>(
            "roleName" to role.roleName,
            "roleCode" to role.roleCode,
            "permissions" to role.permissions,
            "colorHex" to role.colorHex,
            "isDefault" to role.isDefault,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (role.description.isNotBlank()) payload["description"] = role.description
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCustomRole(shopOwnerId: String, roleId: String) {
        val docRef = db.collection("users").document(shopOwnerId).collection("custom_roles").document(roleId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun joinShopWithCodeAndPin(
        storeCode: String,
        pin: String,
        staffUid: String
    ): Result<UserShopSession> {
        return try {
            val codeDoc = db.collection("store_codes").document(storeCode.trim().uppercase()).get().await()
            if (!codeDoc.exists()) {
                return Result.failure(Exception("দোকান কোডটি সঠিক নয়"))
            }
            val ownerId = codeDoc.getString("ownerId") ?: return Result.failure(Exception("দোকান পাওয়া যায়নি"))
            val shopName = codeDoc.getString("shopName") ?: "দোকান"

            // Look up staff by PIN
            val staffQuery = db.collection("users").document(ownerId).collection("staff")
                .whereEqualTo("pin", pin.trim())
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (staffQuery.isEmpty) {
                return Result.failure(Exception("পিন কোড সঠিক নয় অথবা কর্মচারী সক্রিয় নয়"))
            }

            val staffDoc = staffQuery.documents.first()
            val staff = staffDoc.toObject(StaffMember::class.java)
            val staffName = staff?.name ?: staffDoc.getString("name") ?: "কর্মী"
            val role = staff?.role ?: staffDoc.getString("role") ?: "STAFF"
            val permissions = staff?.permissions?.ifEmpty { null }
                ?: (staffDoc.get("permissions") as? List<*>)?.filterIsInstance<String>()
                ?: DokanPermissions.defaultPermissionsForRole(role)
            val roleTitle = staff?.displayRoleTitle ?: role

            // Associate staff UID with shop member list
            val profileDoc = db.collection("users").document(ownerId).collection("shop_profile").document("profile")
            profileDoc.update("memberUids", FieldValue.arrayUnion(staffUid)).await()
            staffDoc.reference.update("assignedUid", staffUid).await()

            Result.success(
                UserShopSession(
                    shopOwnerId = ownerId,
                    userRole = role,
                    roleTitle = roleTitle,
                    staffName = staffName,
                    shopName = shopName,
                    storeCode = storeCode.trim().uppercase(),
                    permissions = permissions
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getShopMemberUids(shopOwnerId: String): List<String> {
        val profileDoc = db.collection("users").document(shopOwnerId).collection("shop_profile").document("profile").get().await()
        val list = profileDoc.get("memberUids") as? List<*>
        return list?.filterIsInstance<String>() ?: emptyList()
    }

    // ---------------- Products ----------------
    fun observeProducts(shopOwnerId: String): Flow<List<Product>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("products")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedBy { it.name }
        }
    }

    suspend fun addProduct(shopOwnerId: String, product: Product): String {
        val docRef = db.collection("users").document(shopOwnerId).collection("products").document()
        val memberUids = getShopMemberUids(shopOwnerId)
        val payload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "name" to product.name,
            "category" to product.category,
            "buyPrice" to product.buyPrice,
            "sellPrice" to product.sellPrice,
            "stockQuantity" to product.stockQuantity,
            "unit" to product.unit,
            "lowStockThreshold" to product.lowStockThreshold,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (product.barcode.isNotBlank()) payload["barcode"] = product.barcode
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProduct(shopOwnerId: String, product: Product) {
        require(product.id.isNotBlank()) { "Product ID required for update" }
        val docRef = db.collection("users").document(shopOwnerId).collection("products").document(product.id)
        val memberUids = getShopMemberUids(shopOwnerId)
        val payload = mutableMapOf<String, Any>(
            "name" to product.name,
            "category" to product.category,
            "buyPrice" to product.buyPrice,
            "sellPrice" to product.sellPrice,
            "stockQuantity" to product.stockQuantity,
            "unit" to product.unit,
            "lowStockThreshold" to product.lowStockThreshold,
            "memberUids" to memberUids,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (product.barcode.isNotBlank()) payload["barcode"] = product.barcode
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProduct(shopOwnerId: String, productId: String) {
        val docRef = db.collection("users").document(shopOwnerId).collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ---------------- Customers ----------------
    fun observeCustomers(shopOwnerId: String): Flow<List<Customer>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("customers")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(Customer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.totalDue }
        }
    }

    suspend fun addCustomer(shopOwnerId: String, customer: Customer): String {
        val docRef = db.collection("users").document(shopOwnerId).collection("customers").document()
        val memberUids = getShopMemberUids(shopOwnerId)
        val payload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "name" to customer.name,
            "phone" to customer.phone,
            "totalDue" to customer.totalDue,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (customer.address.isNotBlank()) payload["address"] = customer.address
        try {
            docRef.set(payload).await()
            if (customer.totalDue > 0) {
                val txRef = db.collection("users").document(shopOwnerId).collection("customer_transactions").document()
                txRef.set(mapOf(
                    "userId" to shopOwnerId,
                    "customerId" to docRef.id,
                    "type" to "DUE_GIVEN",
                    "amount" to customer.totalDue,
                    "balanceAfter" to customer.totalDue,
                    "note" to "প্রারম্ভিক বাকি (Opening Due)",
                    "memberUids" to memberUids,
                    "createdAt" to FieldValue.serverTimestamp()
                )).await()
            }
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addCustomerTransaction(
        shopOwnerId: String,
        customerId: String,
        type: String, // DUE_GIVEN or DUE_COLLECTED
        amount: Double,
        note: String,
        staffName: String = ""
    ) {
        val customerDoc = db.collection("users").document(shopOwnerId).collection("customers").document(customerId)
        val snapshot = customerDoc.get().await()
        val currentDue = (snapshot.get("totalDue") as? Number)?.toDouble() ?: 0.0
        val newDue = if (type == "DUE_GIVEN") currentDue + amount else maxOf(0.0, currentDue - amount)

        val txDoc = db.collection("users").document(shopOwnerId).collection("customer_transactions").document()
        val memberUids = getShopMemberUids(shopOwnerId)
        val txPayload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "customerId" to customerId,
            "type" to type,
            "amount" to amount,
            "balanceAfter" to newDue,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (note.isNotBlank()) txPayload["note"] = note
        if (staffName.isNotBlank()) txPayload["staffName"] = staffName

        try {
            customerDoc.update("totalDue", newDue, "updatedAt", FieldValue.serverTimestamp()).await()
            txDoc.set(txPayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txDoc.path)
            throw e
        }
    }

    fun observeCustomerTransactions(shopOwnerId: String, customerId: String): Flow<List<CustomerTransaction>> {
        val colRef = db.collection("users").document(shopOwnerId)
            .collection("customer_transactions")
            .whereEqualTo("customerId", customerId)
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(CustomerTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt }
        }
    }

    // ---------------- Sales / POS ----------------
    fun observeSales(shopOwnerId: String): Flow<List<Sale>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("sales")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(Sale::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun recordSale(
        shopOwnerId: String,
        sale: Sale,
        cartItems: List<CartItem>,
        staffName: String = ""
    ): String {
        val saleDoc = db.collection("users").document(shopOwnerId).collection("sales").document()
        val memberUids = getShopMemberUids(shopOwnerId)
        val salePayload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "invoiceNumber" to sale.invoiceNumber,
            "itemsSummary" to sale.itemsSummary,
            "totalAmount" to sale.totalAmount,
            "discount" to sale.discount,
            "taxRate" to sale.taxRate,
            "taxAmount" to sale.taxAmount,
            "grandTotal" to sale.grandTotal,
            "paidAmount" to sale.paidAmount,
            "dueAmount" to sale.dueAmount,
            "paymentMethod" to sale.paymentMethod,
            "profit" to sale.profit,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (sale.customerId.isNotBlank()) salePayload["customerId"] = sale.customerId
        if (sale.customerName.isNotBlank()) salePayload["customerName"] = sale.customerName
        if (staffName.isNotBlank()) salePayload["staffName"] = staffName

        try {
            saleDoc.set(salePayload).await()

            // Deduct stock for each cart item
            for (item in cartItems) {
                if (item.product.id.isNotBlank()) {
                    val prodDoc = db.collection("users").document(shopOwnerId).collection("products").document(item.product.id)
                    val newStock = maxOf(0.0, item.product.stockQuantity - item.quantity)
                    prodDoc.update("stockQuantity", newStock, "updatedAt", FieldValue.serverTimestamp()).await()
                }
            }

            // If there is due and customer is selected, record in dues book
            if (sale.dueAmount > 0 && sale.customerId.isNotBlank()) {
                addCustomerTransaction(
                    shopOwnerId = shopOwnerId,
                    customerId = sale.customerId,
                    type = "DUE_GIVEN",
                    amount = sale.dueAmount,
                    note = "ক্যাশমেমো #${sale.invoiceNumber} হতে বাকি",
                    staffName = staffName
                )
            }

            return saleDoc.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, saleDoc.path)
            throw e
        }
    }

    // ---------------- Expenses ----------------
    fun observeExpenses(shopOwnerId: String): Flow<List<Expense>> {
        val colRef = db.collection("users").document(shopOwnerId).collection("expenses")
        return colRef.snapshots().map { snapshot ->
            snapshot.toObjects(Expense::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun addExpense(shopOwnerId: String, expense: Expense): String {
        val docRef = db.collection("users").document(shopOwnerId).collection("expenses").document()
        val memberUids = getShopMemberUids(shopOwnerId)
        val payload = mutableMapOf<String, Any>(
            "userId" to shopOwnerId,
            "title" to expense.title,
            "category" to expense.category,
            "amount" to expense.amount,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (expense.note.isNotBlank()) payload["note"] = expense.note
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteExpense(shopOwnerId: String, expenseId: String) {
        val docRef = db.collection("users").document(shopOwnerId).collection("expenses").document(expenseId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
