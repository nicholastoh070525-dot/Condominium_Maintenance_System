package com.example.condominium_maintenance_system.ui.screens.billing

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.InfoBanner
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.screens.common.FormDialog
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep

@Composable
fun BillingScreen(
    bills: List<MaintenanceFee>,
    payments: List<PaymentRecord>,
    loading: Boolean,
    paying: Boolean,
    onBack: () -> Unit,
    onPay: (MaintenanceFee, String) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    val currentBill = bills.firstOrNull { it.status.equals("UNPAID", true) } ?: bills.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M6",
            title = "Billing",
            onBack = onBack,
            trailing = GlyphType.CARD,
            onTrailing = {
                if (currentBill != null && currentBill.status.equals("UNPAID", true) && !paying) {
                    showPaymentDialog = true
                }
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Maintenance fee payment", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Review your unit's monthly charge and pay securely.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (loading && currentBill == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (currentBill == null) {
                item {
                    EmptyStateCard(
                        "No maintenance bill yet",
                        "Your current bill will appear after it is prepared by the condominium office."
                    )
                }
            } else {
                val paid = currentBill.status.equals("PAID", true)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        color = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(listOf(BlueDeep, MaterialTheme.colorScheme.primary)),
                                    RoundedCornerShape(26.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Text(
                                if (paid) "PAID THIS MONTH" else "CURRENT MAINTENANCE FEE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB9C9EE)
                            )
                            Spacer(Modifier.height(9.dp))
                            Text(currentBill.amount, style = MaterialTheme.typography.displaySmall, color = Color.White)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                if (paid) "${currentBill.month} maintenance fee · Paid"
                                else "${currentBill.month} maintenance fee · Due ${currentBill.dueDate}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD8E2F6)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { if (!paid && !paying) showPaymentDialog = true },
                                enabled = !paid && !paying,
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = BlueDeep,
                                    disabledContainerColor = Color(0xFFDCE6FA),
                                    disabledContentColor = BlueDeep
                                )
                            ) {
                                if (paying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = BlueDeep
                                    )
                                } else {
                                    Text(if (paid) "Fee paid" else "Pay ${currentBill.amount}")
                                }
                            }
                        }
                    }
                }
                item {
                    InfoBanner(
                        title = "Maintenance fee only",
                        body = "DuitNow and FPX payments are simulated in this assignment. A receipt is saved locally in Supabase.",
                        glyph = GlyphType.CARD,
                        badge = "Secure"
                    )
                }
            }

            item {
                SectionTitle("Payment history", action = if (payments.isEmpty()) null else "Receipts") { }
            }
            if (payments.isEmpty() && !loading) {
                item {
                    EmptyStateCard("No payments yet", "Completed maintenance-fee payments will appear here.")
                }
            }
            items(payments, key = { it.id }) { payment ->
                PaymentHistoryRow(payment)
            }
        }
    }

    if (showPaymentDialog && currentBill != null) {
        PaymentDialog(
            amount = currentBill.amount,
            busy = paying,
            onDismiss = { if (!paying) showPaymentDialog = false },
            onPay = { method ->
                onPay(currentBill, method)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun PaymentHistoryRow(payment: PaymentRecord) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.CARD, MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.method, style = MaterialTheme.typography.labelLarge)
                Text(
                    "${payment.receiptNumber} · ${displayDate(payment.paidAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(payment.amount, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentDialog(
    amount: String,
    busy: Boolean,
    onDismiss: () -> Unit,
    onPay: (String) -> Unit
) {
    var method by remember { mutableStateOf("DuitNow") }
    FormDialog(title = "Pay maintenance fee", onDismiss = onDismiss) {
        Text(amount, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "This is a simulated payment recorded in Supabase. No banking transaction is processed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        listOf("DuitNow", "FPX online banking").forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !busy) { method = option }
                    .background(
                        if (method == option) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .border(
                            2.dp,
                            if (method == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .padding(4.dp)
                ) {
                    if (method == option) {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        PrimaryActionButton(
            text = if (busy) "Processing…" else "Pay $amount with $method",
            enabled = !busy
        ) {
            onPay(method)
        }
    }
}

private fun displayDate(value: String): String =
    value.replace("T", " ").take(16).ifBlank { "—" }
