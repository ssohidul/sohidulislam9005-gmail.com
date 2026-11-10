package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.model.ShopProfile
import com.example.ui.theme.DokanGreenPrimary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DokanViewModel,
    onNavigateBack: () -> Unit,
    onOpenStaffManagement: () -> Unit
) {
    val shopProfile by viewModel.shopProfile.collectAsState()
    val session by viewModel.session.collectAsState()
    val staffList by viewModel.staffMembers.collectAsState()

    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var shopName by remember(shopProfile) { mutableStateOf(shopProfile.shopName) }
    var ownerName by remember(shopProfile) { mutableStateOf(shopProfile.ownerName) }
    var phone by remember(shopProfile) { mutableStateOf(shopProfile.phone) }
    var address by remember(shopProfile) { mutableStateOf(shopProfile.address) }
    var currency by remember(shopProfile) { mutableStateOf(shopProfile.currency) }
    var memoFooter by remember(shopProfile) { mutableStateOf(shopProfile.memoFooter) }

    var showJoinDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("দোকানের তথ্য ও সেটিংস", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Account & Active Role Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DokanGreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DokanGreenPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: session.staffName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (viewModel.isOwner) DokanGreenPrimary.copy(alpha = 0.12f) else Color(0xFF1565C0).copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "ভূমিকা: ${session.roleTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isOwner) DokanGreenPrimary else Color(0xFF1565C0),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Staff, Manager & Partner Management Option
            if (viewModel.canManageStaff) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = DokanGreenPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = DokanGreenPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "কর্মী, ম্যানেজার ও পার্টনার (৫০ জন)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "মোট কর্মী: ${staffList.size} জন • পদবী ও পারমিশন নিয়ন্ত্রণ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = onOpenStaffManagement,
                            colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                            modifier = Modifier.testTag("manage_staff_button")
                        ) {
                            Text("ব্যবস্থাপনা")
                        }
                    }
                }
            }

            // Join another shop button
            OutlinedButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (viewModel.isOwner) "কর্মচারী / ম্যানেজার হিসেবে অন্য দোকানে লগইন" else "অন্য দোকানে লগইন পরিবর্তন করুন")
            }

            if (!viewModel.isOwner) {
                Button(
                    onClick = {
                        viewModel.switchBackToOwnerShop()
                        coroutineScope.launch { snackbarHostState.showSnackbar("নিজস্ব দোকানে ফিরে এসেছেন") }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary)
                ) {
                    Text("আমার নিজস্ব দোকানে ফিরুন (মালিক মোড)")
                }
            }

            // Shop Profile Settings (Available to Owner and authorized users)
            if (viewModel.canManageSettings) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("দোকানের প্রোফাইল (ক্যাশমেমোতে মুদ্রিত হবে)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("দোকানের নাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_name_input")
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("মালিকের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("দোকানের মোবাইল নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("দোকানের ঠিকানা / বাজার") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("মুদ্রা প্রতীক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = memoFooter,
                        onValueChange = { memoFooter = it },
                        label = { Text("মেমো ফুটার বার্তা") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }

                Button(
                    onClick = {
                        if (shopName.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("দোকানের নাম দিন") }
                            return@Button
                        }
                        val updated = shopProfile.copy(
                            shopName = shopName.trim(),
                            ownerName = ownerName.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            currency = currency.trim().ifBlank { "৳" },
                            memoFooter = memoFooter.trim()
                        )
                        viewModel.saveShopProfile(
                            profile = updated,
                            onSuccess = {
                                coroutineScope.launch { snackbarHostState.showSnackbar("দোকানের তথ্য সংরক্ষিত হয়েছে") }
                            },
                            onError = { err ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("তথ্য সংরক্ষণ করুন")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Out
            OutlinedButton(
                onClick = {
                    signOut(credentialManager, coroutineScope)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সাইন আউট করুন")
            }
        }
    }

    if (showJoinDialog) {
        JoinShopDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code, pin, onSuccess, onError ->
                viewModel.joinShopAsStaff(
                    storeCode = code,
                    pin = pin,
                    onSuccess = { session ->
                        onSuccess(session)
                        coroutineScope.launch { snackbarHostState.showSnackbar("${session.shopName} দোকানে ${session.roleTitle} হিসেবে প্রবেশ করেছেন") }
                    },
                    onError = onError
                )
            },
            onSwitchBackToOwner = if (!viewModel.isOwner) {
                { viewModel.switchBackToOwnerShop() }
            } else null
        )
    }
}
