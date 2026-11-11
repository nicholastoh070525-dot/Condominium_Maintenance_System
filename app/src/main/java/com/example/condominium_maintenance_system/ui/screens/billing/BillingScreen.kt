package com.example.condominium_maintenance_system.ui.screens.billing

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.condominium_maintenance_system.R
import com.example.condominium_maintenance_system.ui.*
import com.example.condominium_maintenance_system.ui.components.*
import com.example.condominium_maintenance_system.ui.screens.common.*
import com.example.condominium_maintenance_system.ui.theme.*

@Composable
fun BillingScreen(
    fee: MaintenanceFee,
    feePaid: Boolean,
    onBack: () -> Unit,
    onPay: (String) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M6",
            title = "Billing",
            onBack = onBack,
            trailing = GlyphType.CARD,
            onTrailing = { showPaymentDialog = true }
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
                    "Review your monthly charge and pay securely from this resident account.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                            if (feePaid) "PAID THIS MONTH" else "CURRENT MAINTENANCE FEE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB9C9EE)
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            fee.amount,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            if (feePaid) "${fee.month} maintenance fee · Paid" else "${fee.month} maintenance fee · Due ${fee.dueDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD8E2F6)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { if (!feePaid) showPaymentDialog = true },
                            enabled = !feePaid,
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = BlueDeep,
                                disabledContainerColor = Color(0xFFDCE6FA),
                                disabledContentColor = BlueDeep
                            )
                        ) {
                            Text(if (feePaid) "Fee paid" else "Pay ${fee.amount}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                InfoBanner(
                    title = "Maintenance fee only",
                    body = "Pay by FPX or DuitNow. Your payment receipt is saved automatically.",
                    glyph = GlyphType.CARD,
                    badge = "Secure"
                )
            }
            item {
                SectionTitle("Payment history", action = "View all") { }
            }
            item {
                PaymentHistoryRow("October maintenance fee", "Paid · 12 Oct 2026", "RM 285")
            }
            item {
                PaymentHistoryRow("September maintenance fee", "Paid · 09 Sep 2026", "RM 285")
            }
            if (feePaid) {
                item {
                    PaymentHistoryRow(fee.month, "Paid · Just now", fee.amount, highlighted = true)
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            amount = fee.amount,
            onDismiss = { showPaymentDialog = false },
            onPay = {
                showPaymentDialog = false
                onPay(it)
            }
        )
    }
}

@Composable
private fun PaymentHistoryRow(
    title: String,
    detail: String,
    amount: String,
    highlighted: Boolean = false
) {
    CondoCard(color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
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
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(amount, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentDialog(
    amount: String,
    onDismiss: () -> Unit,
    onPay: (String) -> Unit
) {
    var method by remember { mutableStateOf("DuitNow") }
    FormDialog(title = "Pay maintenance fee", onDismiss = onDismiss) {
        Text(
            "$amount · November 2026",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "This payment applies only to the monthly maintenance fee.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        listOf("DuitNow", "FPX online banking").forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { method = option }
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
        PrimaryActionButton("Pay $amount with $method") { onPay(method) }
    }
}
