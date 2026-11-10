package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.ShopProfile
import com.example.model.StaffMember
import com.example.model.StaffRole
import com.example.ui.theme.DokanGreenPrimary
import kotlinx.coroutines.launch

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.model.CustomRole
import com.example.model.DokanPermissions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    viewModel: DokanViewModel,
    onNavigateBack: () -> Unit
) {
    val staffList by viewModel.staffMembers.collectAsState()
    val customRoles by viewModel.customRoles.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedSubTab by remember { mutableStateOf(0) }
    var storeCode by remember { mutableStateOf(shopProfile.storeCode) }
    var showAddDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<StaffMember?>(null) }
    var staffToDelete by remember { mutableStateOf<StaffMember?>(null) }

    LaunchedEffect(Unit) {
        viewModel.ensureStoreCode(
            onSuccess = { code -> storeCode = code },
            onError = { /* fallback */ }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("কর্মী, ম্যানেজার ও পার্টনার (৫০ জন)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedSubTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = DokanGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_staff_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Staff")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedSubTab) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("কর্মী ও পার্টনার (${staffList.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_staff")
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("কাস্টম রোল নীতি", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_roles")
                )
            }

            if (selectedSubTab == 1) {
                CustomRolesManagerComponent(
                    viewModel = viewModel,
                    onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
            // Store Code Share Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "আপনার দোকানের কোড (Store Code)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = storeCode.ifBlank { "তৈরি হচ্ছে..." },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = DokanGreenPrimary,
                                letterSpacing = 2.sp
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                if (storeCode.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Store Code", storeCode))
                                    scope.launch { snackbarHostState.showSnackbar("দোকান কোড কপি হয়েছে") }
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = DokanGreenPrimary)
                            }

                            IconButton(onClick = {
                                if (storeCode.isNotBlank()) {
                                    val shareText = "দোকান: ${shopProfile.shopName}\nদোকান কোড: $storeCode\nআপনার নির্ধারিত ৪-ডিজিট পিন দিয়ে NSB দোকান সেবা অ্যাপে প্রবেশ করুন।"
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "দোকান কোড শেয়ার করুন"))
                                }
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share Code", tint = DokanGreenPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ম্যানেজার বা কর্মচারী এই কোড ও তাদের ৪-ডিজিট পিন দিয়ে সরাসরি একই দোকানের হিসাব চালাতে পারবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট যুক্ত কর্মী: ${staffList.size} / ৫০ জন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { staffList.size / 50f },
                            modifier = Modifier.width(120.dp),
                            color = DokanGreenPrimary,
                        )
                    }
                }
            }

            if (staffList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "এখনও কোন কর্মচারী বা ম্যানেজার যুক্ত করা হয়নি।",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নিচে (+) বাটনে চাপ দিয়ে ম্যানেজার বা ক্যাশিয়ার যোগ করুন (সর্বোচ্চ ৫০ জন)।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffList) { staff ->
                        StaffCard(
                            staff = staff,
                            onEdit = { staffToEdit = staff },
                            onDelete = { staffToDelete = staff }
                        )
                    }
                }
            }
        }
    }
}
}

    // Add or Edit Staff Dialog
    if (showAddDialog || staffToEdit != null) {
        StaffFormDialog(
            initialStaff = staffToEdit,
            customRoles = customRoles,
            onDismiss = {
                showAddDialog = false
                staffToEdit = null
            },
            onSave = { staff ->
                if (staffToEdit != null) {
                    viewModel.updateStaffMember(
                        staff = staff,
                        onSuccess = {
                            showAddDialog = false
                            staffToEdit = null
                            scope.launch { snackbarHostState.showSnackbar("তথ্য আপডেট হয়েছে") }
                        },
                        onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                    )
                } else {
                    viewModel.addStaffMember(
                        staff = staff,
                        onSuccess = {
                            showAddDialog = false
                            scope.launch { snackbarHostState.showSnackbar("নতুন কর্মী যুক্ত হয়েছে") }
                        },
                        onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                    )
                }
            }
        )
    }

    // Delete Staff Dialog
    staffToDelete?.let { staff ->
        AlertDialog(
            onDismissRequest = { staffToDelete = null },
            title = { Text("কর্মী মুছে ফেলা") },
            text = { Text("'${staff.name}' (${staff.role}) কর্মীর প্রবেশাধিকার বাতিল ও মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStaffMember(
                            staffId = staff.id,
                            onSuccess = {
                                staffToDelete = null
                                scope.launch { snackbarHostState.showSnackbar("কর্মী মুছে ফেলা হয়েছে") }
                            },
                            onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { staffToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun StaffCard(
    staff: StaffMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val roleColor = when (staff.role) {
        "OWNER", "CO_OWNER" -> DokanGreenPrimary
        "PARTNER" -> Color(0xFF4A148C)
        "MANAGER" -> Color(0xFF1565C0)
        "CASHIER" -> Color(0xFF00838F)
        "CUSTOM" -> Color(0xFF6A1B9A)
        else -> Color(0xFFD97706)
    }

    val activePermCount = if (staff.role == "OWNER" || staff.role == "CO_OWNER") {
        DokanPermissions.ALL_PERMISSIONS.size
    } else {
        staff.permissions.size
    }

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
                        color = roleColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = roleColor, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(staff.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = roleColor.copy(alpha = 0.12f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = staff.displayRoleTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = roleColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (staff.phone.isNotBlank()) {
                            Text("মোবাইল: ${staff.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = "PIN", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পিন: ${staff.pin}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
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
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            // Permissions Badge summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = roleColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (staff.role in listOf("OWNER", "CO_OWNER")) {
                            "পূর্ণ পারমিশন (৭/৭টি সক্রিয়)"
                        } else {
                            "পারমিশন সক্রিয়: $activePermCount/৭টি"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }

                Text(
                    text = if (staff.isActive) "সক্রিয়" else "নিষ্ক্রিয়",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (staff.isActive) DokanGreenPrimary else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffFormDialog(
    initialStaff: StaffMember?,
    customRoles: List<CustomRole> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (StaffMember) -> Unit
) {
    var name by remember { mutableStateOf(initialStaff?.name ?: "") }
    var role by remember { mutableStateOf(initialStaff?.role ?: "STAFF") }
    var customTitle by remember { mutableStateOf(initialStaff?.customTitle ?: "") }
    var phone by remember { mutableStateOf(initialStaff?.phone ?: "") }
    var pin by remember { mutableStateOf(initialStaff?.pin ?: (1000..9999).random().toString()) }
    var selectedPermissions by remember {
        mutableStateOf(
            if (initialStaff != null) {
                if (initialStaff.role in listOf("OWNER", "CO_OWNER")) {
                    DokanPermissions.ALL_PERMISSIONS.toSet()
                } else {
                    initialStaff.permissions.toSet()
                }
            } else {
                DokanPermissions.defaultPermissionsForRole("STAFF").toSet()
            }
        )
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isRoleDropdownExpanded by remember { mutableStateOf(false) }

    val roleOptions = remember(customRoles) {
        val list = mutableListOf(
            "CO_OWNER" to "সহ-মালিক (Co-Owner) — সম্পূর্ণ নিয়ন্ত্রণ",
            "PARTNER" to "ব্যবসায়িক অংশীদার (Partner) — লাভ-ক্ষতি ও অডিট",
            "MANAGER" to "ম্যানেজার (Manager) — বিক্রি, স্টক, বাকি ও খরচ",
            "CASHIER" to "ক্যাশিয়ার / হিসাবরক্ষক (Cashier) — বিক্রি ও ক্যাশ",
            "STAFF" to "বিক্রয় কর্মী / সেলসম্যান (Staff) — বিক্রি ও স্টক"
        )
        customRoles.filter { !it.isDefault }.forEach { cr ->
            list.add("CR_${cr.id}" to "${cr.roleName} (কাস্টম রোল)")
        }
        list.add("CUSTOM" to "অন্যান্য / কাস্টম পদবী লিখুন")
        list
    }

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
                    text = if (initialStaff != null) "তথ্য ও পারমিশন সম্পাদনা" else "নতুন কর্মী / পার্টনার যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "পদবী ও প্রয়োজনীয় কাজের পারমিশন নির্ধারণ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ব্যক্তি / কর্মীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = isRoleDropdownExpanded,
                    onExpandedChange = { isRoleDropdownExpanded = !isRoleDropdownExpanded }
                ) {
                    val currentRoleLabel = if (role == "CUSTOM" && customTitle.isNotBlank()) {
                        "$customTitle (কাস্টম)"
                    } else {
                        roleOptions.firstOrNull { it.first == role }?.second ?: "পদবী নির্বাচন করুন"
                    }
                    OutlinedTextField(
                        value = currentRoleLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("পদবী / ভূমিকা (Position) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isRoleDropdownExpanded,
                        onDismissRequest = { isRoleDropdownExpanded = false }
                    ) {
                        roleOptions.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    isRoleDropdownExpanded = false
                                    if (code.startsWith("CR_")) {
                                        val cr = customRoles.firstOrNull { "CR_${it.id}" == code }
                                        if (cr != null) {
                                            role = "CUSTOM"
                                            customTitle = cr.roleName
                                            selectedPermissions = cr.permissions.toSet()
                                        }
                                    } else {
                                        role = code
                                        if (code != "CUSTOM") {
                                            customTitle = ""
                                        }
                                        selectedPermissions = DokanPermissions.defaultPermissionsForRole(code).toSet()
                                    }
                                }
                            )
                        }
                    }
                }

                // If CUSTOM role selected, show input for custom title
                if (role == "CUSTOM") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("কাস্টম পদবীর নাম লিখুন *") },
                        placeholder = { Text("যেমন: সহকারী ম্যানেজার, স্টোর কিপার, সুপারভাইজার") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_title_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Granular Permissions Checklist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "কাজের অনুমতি / এক্সেস পারমিশন",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "তিনি দোকানে কী কী কাজ করতে পারবেন:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { selectedPermissions = DokanPermissions.ALL_PERMISSIONS.toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("সব অনুমতি", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { selectedPermissions = DokanPermissions.defaultPermissionsForRole(role).toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("রোল ডিফল্ট", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { selectedPermissions = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("সব মুছুন", fontSize = 11.sp)
                    }
                }

                // Permissions List with Checkboxes
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
                                    colors = CheckboxDefaults.colors(checkedColor = DokanGreenPrimary)
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

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Security PIN
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("৪-ডিজিট সিকিউরিটি পিন *") },
                    placeholder = { Text("যেমন: 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_pin_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                                errorMsg = "কর্মীর নাম দিন"
                                return@Button
                            }
                            if (role == "CUSTOM" && customTitle.isBlank()) {
                                errorMsg = "কাস্টম পদবীর নাম দিন"
                                return@Button
                            }
                            if (pin.length < 4) {
                                errorMsg = "কমপক্ষে ৪ ডিজিটের পিন কোড দিন"
                                return@Button
                            }
                            val updated = (initialStaff ?: StaffMember()).copy(
                                name = name.trim(),
                                role = role,
                                customTitle = customTitle.trim(),
                                permissions = selectedPermissions.toList(),
                                pin = pin.trim(),
                                phone = phone.trim(),
                                isActive = true
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                        modifier = Modifier.testTag("save_staff_button")
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}
