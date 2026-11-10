package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Sale
import com.example.model.ShopProfile
import com.example.ui.theme.DokanGreenPrimary
import com.example.ui.theme.DueOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashMemoDialog(
    sale: Sale,
    shopProfile: ShopProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = sale.createdAt?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault()).format(it)
    } ?: SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault()).format(Date())

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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close & success icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = DokanGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "বিক্রি সম্পন্ন!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DokanGreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Printable Cash Memo Paper Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    color = Color(0xFFFCFDF9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shopProfile.shopName.ifBlank { "NSB দোকান সেবা" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (shopProfile.ownerName.isNotBlank()) {
                            Text(
                                text = "প্রোঃ ${shopProfile.ownerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (shopProfile.phone.isNotBlank()) {
                            Text(
                                text = "মোবাইল: ${shopProfile.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (shopProfile.address.isNotBlank()) {
                            Text(
                                text = shopProfile.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "--- ডিজিটাল ক্যাশমেমো ---",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DokanGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Memo details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মেমো নং: ${sale.invoiceNumber}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("খদ্দের: ${sale.customerName}", style = MaterialTheme.typography.bodySmall)
                            Text("মাধ্যম: ${sale.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items Breakdown
                        Text(
                            text = "পণ্যের বিবরণ:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sale.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Totals
                        MemoRow("মোট মূল্য (Subtotal):", "${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.totalAmount)}")
                        if (sale.discount > 0) {
                            MemoRow("ছাড় (Discount):", "- ${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.discount)}", color = Color(0xFFC62828))
                        }
                        if (sale.taxAmount > 0) {
                            MemoRow("ভ্যাট ও কর (${sale.taxRate.toInt()}% VAT):", "+ ${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.taxAmount)}", color = DokanGreenPrimary)
                        }
                        MemoRow("সর্বমোট বিল (Net Total):", "${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.grandTotal)}", isBold = true)
                        MemoRow("নগদ পরিশোধ (Paid):", "${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.paidAmount)}", color = DokanGreenPrimary)
                        if (sale.dueAmount > 0) {
                            MemoRow("বাকি রইল (Due):", "${shopProfile.currency} ${String.format(Locale.US, "%.2f", sale.dueAmount)}", isBold = true, color = DueOrange)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = shopProfile.memoFooter.ifBlank { "ধন্যবাদ, আবার আসবেন!" },
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share & Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareInvoice(context, sale, shopProfile, dateStr)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DokanGreenPrimary)
                    ) {
                        Text("সম্পন্ন")
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

fun shareInvoice(context: Context, sale: Sale, shop: ShopProfile, dateStr: String) {
    val text = buildString {
        appendLine("🏪 ${shop.shopName.ifBlank { "NSB দোকান সেবা" }}")
        if (shop.phone.isNotBlank()) appendLine("📞 মোবাইল: ${shop.phone}")
        appendLine("📄 মেমো নং: ${sale.invoiceNumber}")
        appendLine("📅 তারিখ: $dateStr")
        appendLine("👤 খদ্দের: ${sale.customerName}")
        appendLine("--------------------------")
        appendLine("🛒 পণ্য:")
        appendLine(sale.itemsSummary)
        appendLine("--------------------------")
        if (sale.discount > 0) {
            appendLine("🏷️ ছাড়: ${shop.currency} ${String.format(Locale.US, "%.2f", sale.discount)}")
        }
        if (sale.taxAmount > 0) {
            appendLine("🏛️ ভ্যাট (${sale.taxRate.toInt()}%): ${shop.currency} ${String.format(Locale.US, "%.2f", sale.taxAmount)}")
        }
        appendLine("💵 মোট বিল: ${shop.currency} ${String.format(Locale.US, "%.2f", sale.grandTotal)}")
        appendLine("✅ জমা: ${shop.currency} ${String.format(Locale.US, "%.2f", sale.paidAmount)}")
        if (sale.dueAmount > 0) {
            appendLine("⚠️ বাকি: ${shop.currency} ${String.format(Locale.US, "%.2f", sale.dueAmount)}")
        }
        appendLine("--------------------------")
        appendLine(shop.memoFooter.ifBlank { "ধন্যবাদ, আবার আসবেন!" })
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "ক্যাশমেমো - ${shop.shopName}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "ক্যাশমেমো পাঠান"))
}
