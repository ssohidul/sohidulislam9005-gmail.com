package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CustomRole
import com.example.model.DokanPermissions
import com.example.ui.theme.DokanGreenPrimary
import kotlinx.coroutines.launch

private val PALETTE_COLORS = listOf(
    "#0A5C36", // Emerald Green
    "#1565C0", // Deep Blue
    "#4A148C", // Royal Purple
    "#00838F", // Cyan / Teal
    "#D97706", // Amber
    "#C62828", // Crimson Red
    "#6A1B9A"  // Indigo / Violet
)

fun parseHexColor(hex: String, defaultColor: Color = DokanGreenPrimary): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        defaultColor
    }
}

/**
 * UI Component that allows the admin to define custom roles (e.g. Manager, Staff, Partner)
 * and assign specific access permissions for each role to be stored in Firestore.
 */
@Composable
fun CustomRolesManagerComponent(
    viewModel: DokanViewModel,
    onShowMessage: (String) -> Unit = {}
) {
    val firestoreRoles by viewModel.customRoles.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var roleToEdit by remember { mutableStateOf<CustomRole?>(null) }
    var roleToDelete by remember { mutableStateOf<CustomRole?>(null) }

    // Combine built-in standard roles with admin custom roles from Firestore
    val defaultPresetRoles = remember {
        listOf(
            CustomRole(
                id = "preset_owner",
                roleName = "মূল মালিক (Owner)",
                roleCode = "OWNER",
                description = "দোকানের সকল ডেটা, সেটিংস ও কর্মী ব্যবস্থাপনায় পূর্ণ ক্ষমতা।",
                colorHex = "#0A5C36",
                permissions = DokanPermissions.ALL_PERMISSIONS,
                isDefault = true
            ),
            CustomRole(
                id = "preset_co_owner",
                roleName = "সহ-মালিক (Co-Owner)",
                roleCode = "CO_OWNER",
                description = "মালিকের সমকক্ষ অধিকার ও সকল ফিচারের পূর্ণ এক্সেস।",
                colorHex = "#2E7D32",
                permissions = DokanPermissions.ALL_PERMISSIONS,
                isDefault = true
            ),
            CustomRole(
                id = "preset_partner",
                roleName = "ব্যবসায়িক অংশীদার (Partner)",
                roleCode = "PARTNER",
                description = "বিক্রি, বাকি খাতা, স্টক, খরচ ও লাভ-ক্ষতির পূর্ণ রিপোর্ট দেখার অধিকার।",
                colorHex = "#4A148C",
                permissions = listOf(
                    DokanPermissions.PERM_POS_SALES,
                    DokanPermissions.PERM_DUES_BOOK,
                    DokanPermissions.PERM_STOCK_MANAGE,
                    DokanPermissions.PERM_EXPENSES,
                    DokanPermissions.PERM_REPORTS
                ),
                isDefault = true
            ),
            CustomRole(
                id = "preset_manager",
                roleName = "ম্যানেজার (Manager)",
                roleCode = "MANAGER",
                description = "দোকান পরিচালনা: পণ্য বিক্রি, স্টক ইনভেন্টরি, বাকি আদায় ও খরচ লিপিবদ্ধকরণ।",
                colorHex = "#1565C0",
                permissions = listOf(
                    DokanPermissions.PERM_POS_SALES,
                    DokanPermissions.PERM_DUES_BOOK,
                    DokanPermissions.PERM_STOCK_MANAGE,
                    DokanPermissions.PERM_EXPENSES,
                    DokanPermissions.PERM_REPORTS
                ),
                isDefault = true
            ),
            CustomRole(
                id = "preset_cashier",
                roleName = "ক্যাশিয়ার / হিসাবরক্ষক (Cashier)",
                roleCode = "CASHIER",
                description = "দ্রুত বিক্রি, নগদ আদায়, বাকি খাতা ও দৈনিক খরচ এন্ট্রি।",
                colorHex = "#00838F",
                permissions = listOf(
                    DokanPermissions.PERM_POS_SALES,
                    DokanPermissions.PERM_DUES_BOOK,
                    DokanPermissions.PERM_EXPENSES
                ),
                isDefault = true
            ),
            CustomRole(
                id = "preset_staff",
                roleName = "বিক্রয় কর্মী / সেলসম্যান (Staff)",
                roleCode = "STAFF",
                description = "বিক্রি করা, ক্যাশমেমো প্রদান এবং পণ্যের মজুদ সংখ্যা দেখা।",
                colorHex = "#D97706",
                permissions = listOf(
                    DokanPermissions.PERM_POS_SALES,
                    DokanPermissions.PERM_STOCK_MANAGE
                ),
                isDefault = true
            )
        )
    }

    val allDisplayRoles = remember(firestoreRoles) {
        defaultPresetRoles + firestoreRoles
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Info Card with Add Role CTA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        color = DokanGreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = DokanGreenPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "কাস্টম রোল ও পারমিশন নীতি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Firestore-এ সংরক্ষিত মোট পদবী: ${allDisplayRoles.size}টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                    modifier = Modifier.testTag("create_custom_role_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন রোল", fontSize = 12.sp)
                }
            }
        }

        // List of Roles
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allDisplayRoles, key = { it.id.ifBlank { it.roleCode } }) { role ->
                RoleCardItem(
                    role = role,
                    onEdit = if (!role.isDefault) { { roleToEdit = role } } else null,
                    onDelete = if (!role.isDefault) { { roleToDelete = role } } else null
                )
            }
        }
    }

    // Create / Edit Role Dialog
    if (showCreateDialog || roleToEdit != null) {
        CustomRoleFormDialog(
            initialRole = roleToEdit,
            onDismiss = {
                showCreateDialog = false
                roleToEdit = null
            },
            onSave = { updatedRole ->
                if (roleToEdit != null) {
                    viewModel.updateCustomRole(
                        role = updatedRole,
                        onSuccess = {
                            showCreateDialog = false
                            roleToEdit = null
                            onShowMessage("রোল সফলভাবে আপডেট হয়েছে")
                        },
                        onError = { err -> onShowMessage(err) }
                    )
                } else {
                    viewModel.addCustomRole(
                        role = updatedRole,
                        onSuccess = {
                            showCreateDialog = false
                            onShowMessage("নতুন কাস্টম রোল Firestore-এ সংরক্ষিত হয়েছে")
                        },
                        onError = { err -> onShowMessage(err) }
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    roleToDelete?.let { role ->
        AlertDialog(
            onDismissRequest = { roleToDelete = null },
            title = { Text("রোল মুছে ফেলা") },
            text = { Text("'${role.roleName}' রোলটি মুছে ফেলতে চান? এই রোলের পারমিশন সেটিং বাতিল হয়ে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomRole(
                            roleId = role.id,
                            onSuccess = {
                                roleToDelete = null
                                onShowMessage("রোল মুছে ফেলা হয়েছে")
                            },
                            onError = { err -> onShowMessage(err) }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleCardItem(
    role: CustomRole,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    val roleColor = parseHexColor(role.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = roleColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = roleColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = role.roleName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (role.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "ডিফল্ট",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        if (role.description.isNotBlank()) {
                            Text(
                                text = role.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!role.isDefault) {
                    Row {
                        onEdit?.let {
                            IconButton(onClick = it) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Role", tint = DokanGreenPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        onDelete?.let {
                            IconButton(onClick = it) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Role", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Permission Pills / Summary
            Text(
                text = "নির্ধারিত অনুমতি (${role.permissions.size}/৭টি):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = roleColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DokanPermissions.DEFINITIONS.forEach { permDef ->
                    val isGranted = role.permissions.contains(permDef.key)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isGranted) roleColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isGranted) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = permDef.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isGranted) roleColor else Color.Gray,
                                fontWeight = if (isGranted) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRoleFormDialog(
    initialRole: CustomRole?,
    onDismiss: () -> Unit,
    onSave: (CustomRole) -> Unit
) {
    var roleName by remember { mutableStateOf(initialRole?.roleName ?: "") }
    var description by remember { mutableStateOf(initialRole?.description ?: "") }
    var selectedColor by remember { mutableStateOf(initialRole?.colorHex ?: PALETTE_COLORS[0]) }
    var selectedPermissions by remember {
        mutableStateOf(
            initialRole?.permissions?.toSet()
                ?: setOf(DokanPermissions.PERM_POS_SALES, DokanPermissions.PERM_STOCK_MANAGE)
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialRole != null) "রোল ও পারমিশন সম্পাদনা" else "নতুন কাস্টম রোল তৈরি",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "পদবীর নাম দিন এবং নির্ধারিত কাজের পারমিশন নির্বাচন করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    label = { Text("রোলের নাম (যেমন: স্টোর কিপার, সুপারভাইজার) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_role_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("কাজের সংক্ষিপ্ত বিবরণ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selector
                Text(
                    text = "ব্যাজের রঙ নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PALETTE_COLORS.forEach { hex ->
                        val color = parseHexColor(hex)
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Granular Permissions Checklist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "এক্সেস পারমিশন নির্বাচন (${selectedPermissions.size}/৭টি):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { selectedPermissions = DokanPermissions.ALL_PERMISSIONS.toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("সব সিলেক্ট", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = {
                            selectedPermissions = setOf(
                                DokanPermissions.PERM_POS_SALES,
                                DokanPermissions.PERM_DUES_BOOK,
                                DokanPermissions.PERM_STOCK_MANAGE
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বেসিক রোল", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { selectedPermissions = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("সব মুছুন", fontSize = 11.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DokanPermissions.DEFINITIONS.forEach { perm ->
                            val isChecked = selectedPermissions.contains(perm.key)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedPermissions = if (checked) {
                                            selectedPermissions + perm.key
                                        } else {
                                            selectedPermissions - perm.key
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = parseHexColor(selectedColor))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = perm.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = perm.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (roleName.isBlank()) {
                                errorMessage = "রোলের নাম দিন"
                                return@Button
                            }
                            val updated = (initialRole ?: CustomRole()).copy(
                                roleName = roleName.trim(),
                                roleCode = (initialRole?.roleCode?.ifBlank { null }
                                    ?: roleName.trim().replace(" ", "_").uppercase()),
                                description = description.trim(),
                                colorHex = selectedColor,
                                permissions = selectedPermissions.toList(),
                                isDefault = false
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(selectedColor)),
                        modifier = Modifier.testTag("save_custom_role_button")
                    ) {
                        Text("Firestore-এ সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}
