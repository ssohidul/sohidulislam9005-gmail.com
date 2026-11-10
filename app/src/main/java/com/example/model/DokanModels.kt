package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object DokanPermissions {
    const val PERM_POS_SALES = "perm_pos_sales"          // বিক্রি ও ক্যাশমেমো তৈরি
    const val PERM_DUES_BOOK = "perm_dues_book"          // বাকি খাতা ও আদায়
    const val PERM_STOCK_MANAGE = "perm_stock_manage"    // পণ্য ও স্টক পরিবর্তন/যুক্ত
    const val PERM_EXPENSES = "perm_expenses"            // দৈনিক খরচ হিসাব
    const val PERM_REPORTS = "perm_reports"              // লাভ-ক্ষতি ও আর্থিক হিসাব
    const val PERM_SETTINGS = "perm_settings"            // দোকানের তথ্য সেটিংস
    const val PERM_STAFF_MANAGE = "perm_staff_manage"    // কর্মী ও পার্টনার পরিচালনা

    val ALL_PERMISSIONS = listOf(
        PERM_POS_SALES,
        PERM_DUES_BOOK,
        PERM_STOCK_MANAGE,
        PERM_EXPENSES,
        PERM_REPORTS,
        PERM_SETTINGS,
        PERM_STAFF_MANAGE
    )

    data class PermissionDef(
        val key: String,
        val title: String,
        val description: String
    )

    val DEFINITIONS = listOf(
        PermissionDef(
            key = PERM_POS_SALES,
            title = "বিক্রি ও ডিজিটাল ক্যাশমেমো",
            description = "পণ্য বিক্রয় করা, মূল্য ছাড় দেওয়া ও খদ্দেরকে রসিদ প্রদান"
        ),
        PermissionDef(
            key = PERM_DUES_BOOK,
            title = "বাকি খাতা ও কালেকশন",
            description = "খদ্দেরের বাকি দেখা, টাকা আদায় এবং বকেয়া এন্ট্রি করা"
        ),
        PermissionDef(
            key = PERM_STOCK_MANAGE,
            title = "পণ্য ও ইনভেন্টরি স্টক",
            description = "নতুন পণ্য যোগ, মূল্য ও স্টক পরিমাণ বাড়ানো বা কমানো"
        ),
        PermissionDef(
            key = PERM_EXPENSES,
            title = "দোকানের খরচ হিসাব",
            description = "দৈনন্দিন যাবতীয় দোকান ব্যয়ের হিসাব লিপিবদ্ধ করা"
        ),
        PermissionDef(
            key = PERM_REPORTS,
            title = "লাভ-ক্ষতি ও আর্থিক ড্যাশবোর্ড",
            description = "দৈনিক নিট লাভ, মোট বিক্রি ও আর্থিক পরিসংখ্যান দেখা"
        ),
        PermissionDef(
            key = PERM_SETTINGS,
            title = "দোকানের তথ্য সেটিংস",
            description = "দোকানের নাম, ঠিকানা, মোবাইল নম্বর ও প্রোফাইল পরিবর্তন"
        ),
        PermissionDef(
            key = PERM_STAFF_MANAGE,
            title = "কর্মী ও পার্টনার পরিচালনা",
            description = "অন্যান্য কর্মী যোগ, পদবী ও কাজের পারমিশন নির্ধারণ"
        )
    )

    fun defaultPermissionsForRole(role: String): List<String> = when (role) {
        "OWNER", "CO_OWNER" -> ALL_PERMISSIONS
        "PARTNER" -> listOf(PERM_POS_SALES, PERM_DUES_BOOK, PERM_STOCK_MANAGE, PERM_EXPENSES, PERM_REPORTS)
        "MANAGER" -> listOf(PERM_POS_SALES, PERM_DUES_BOOK, PERM_STOCK_MANAGE, PERM_EXPENSES, PERM_REPORTS)
        "CASHIER" -> listOf(PERM_POS_SALES, PERM_DUES_BOOK, PERM_EXPENSES)
        "STAFF" -> listOf(PERM_POS_SALES, PERM_STOCK_MANAGE)
        else -> listOf(PERM_POS_SALES)
    }
}

enum class StaffRole(val code: String, val label: String, val badgeColorHex: Long) {
    OWNER("OWNER", "মূল মালিক (Owner)", 0xFF0A5C36),
    CO_OWNER("CO_OWNER", "সহ-মালিক (Co-Owner)", 0xFF2E7D32),
    PARTNER("PARTNER", "ব্যবসায়িক অংশীদার (Partner)", 0xFF4A148C),
    MANAGER("MANAGER", "ম্যানেজার (Manager)", 0xFF1565C0),
    CASHIER("CASHIER", "ক্যাশিয়ার / হিসাবরক্ষক (Cashier)", 0xFF00838F),
    STAFF("STAFF", "বিক্রয় কর্মী / সেলসম্যান (Staff)", 0xFFD97706),
    CUSTOM("CUSTOM", "কাস্টম পদবী (Custom Title)", 0xFF6A1B9A)
}

@IgnoreExtraProperties
data class ShopProfile(
    @DocumentId val id: String = "",
    val shopName: String = "আমার দোকান",
    val ownerName: String = "দোকানদার",
    val phone: String = "",
    val address: String = "",
    val currency: String = "৳",
    val memoFooter: String = "ধন্যবাদ, আবার আসবেন!",
    val storeCode: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StaffMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "STAFF", // OWNER, CO_OWNER, PARTNER, MANAGER, CASHIER, STAFF, CUSTOM
    val customTitle: String = "",
    val permissions: List<String> = emptyList(),
    val pin: String = "1234",
    val assignedUid: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val displayRoleTitle: String
        get() = when (role) {
            "OWNER" -> "মূল মালিক (Owner)"
            "CO_OWNER" -> "সহ-মালিক (Co-Owner)"
            "PARTNER" -> if (customTitle.isNotBlank()) customTitle else "ব্যবসায়িক অংশীদার"
            "MANAGER" -> if (customTitle.isNotBlank()) customTitle else "ম্যানেজার"
            "CASHIER" -> if (customTitle.isNotBlank()) customTitle else "ক্যাশিয়ার"
            "STAFF" -> if (customTitle.isNotBlank()) customTitle else "বিক্রয় কর্মী"
            "CUSTOM" -> if (customTitle.isNotBlank()) customTitle else "কাস্টম কর্মী"
            else -> if (customTitle.isNotBlank()) customTitle else role
        }

    fun hasPermission(permKey: String): Boolean {
        if (role == "OWNER" || role == "CO_OWNER") return true
        return permissions.contains(permKey)
    }
}

@IgnoreExtraProperties
data class CustomRole(
    @DocumentId val id: String = "",
    val userId: String = "",
    val roleName: String = "",
    val roleCode: String = "",
    val description: String = "",
    val colorHex: String = "#1565C0",
    val permissions: List<String> = emptyList(),
    val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)


@IgnoreExtraProperties
data class StoreCodeEntry(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val shopName: String = "",
    val code: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "মুদি (Grocery)",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val unit: String = "পিস",
    val lowStockThreshold: Double = 5.0,
    val barcode: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val totalDue: Double = 0.0,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomerTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerId: String = "",
    val type: String = "DUE_GIVEN", // "DUE_GIVEN" or "DUE_COLLECTED"
    val amount: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val note: String = "",
    val staffName: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Sale(
    @DocumentId val id: String = "",
    val userId: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val customerName: String = "ক্যাশ খদ্দের (Walk-in)",
    val itemsSummary: String = "",
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = "নগদ (Cash)", // Cash, bKash, Nagad, Due
    val profit: Double = 0.0,
    val staffName: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val customPrice: Double = product.sellPrice
) {
    val total: Double get() = quantity * customPrice
    val profit: Double get() = quantity * (customPrice - product.buyPrice)
}

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "দোকান খরচ",
    val amount: Double = 0.0,
    val note: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class UserShopSession(
    val shopOwnerId: String = "",
    val userRole: String = "OWNER", // OWNER, CO_OWNER, PARTNER, MANAGER, CASHIER, STAFF, CUSTOM
    val roleTitle: String = "মূল মালিক",
    val staffName: String = "মালিক",
    val shopName: String = "",
    val storeCode: String = "",
    val permissions: List<String> = DokanPermissions.ALL_PERMISSIONS
) {
    fun hasPermission(permKey: String): Boolean {
        if (userRole == "OWNER" || userRole == "CO_OWNER") return true
        return permissions.contains(permKey)
    }
}

