package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.UserShopSession
import com.example.ui.theme.DokanGreenPrimary

@Composable
fun JoinShopDialog(
    onDismiss: () -> Unit,
    onJoin: (code: String, pin: String, onSuccess: (UserShopSession) -> Unit, onError: (String) -> Unit) -> Unit,
    onSwitchBackToOwner: (() -> Unit)? = null
) {
    var storeCode by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = DokanGreenPrimary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "দোকানে লগইন করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মালিকের কাছ থেকে পাওয়া দোকান কোড ও ৪-ডিজিট পিন দিয়ে ম্যানেজার, অংশীদার বা কর্মী হিসেবে প্রবেশ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = storeCode,
                    onValueChange = { storeCode = it.uppercase() },
                    label = { Text("দোকান কোড (যেমন: NSB-1234)") },
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_store_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("আপনার ৪-ডিজিট পিন কোড") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_pin_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (storeCode.isBlank()) {
                            errorMessage = "দোকান কোড দিন"
                            return@Button
                        }
                        if (pin.isBlank()) {
                            errorMessage = "পিন কোড দিন"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        onJoin(
                            storeCode.trim(),
                            pin.trim(),
                            {
                                isLoading = false
                                onDismiss()
                            },
                            { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_join_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("দোকানে প্রবেশ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (onSwitchBackToOwner != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = {
                            onSwitchBackToOwner()
                            onDismiss()
                        }
                    ) {
                        Text("আমার নিজস্ব দোকানে ফিরুন (মালিক মোড)")
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("বাতিল")
                }
            }
        }
    }
}
