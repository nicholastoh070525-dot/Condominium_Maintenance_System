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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.data.model.BillingSelectionRules
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.LoadingCard
import com.example.condominium_maintenance_system.ui.components.PillTone
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.components.StatusPill
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.Success
import com.example.condominium_maintenance_system.ui.theme.SuccessPale
import java.text.NumberFormat
import java.util.Locale

private enum class BillingTab(val label: String) {
    BILLS("Bills"),
    PAYMENT_HISTORY("Payment History")
}

@Composable
fun BillingScreen(
    bills: List<MaintenanceFee>,
    payments: List<PaymentRecord>,
    loading: Boolean,
    paying: Boolean,
    selectedBillId: String?,
    onBillSelected: (String) -> Unit,
    onContinuePayment: (MaintenanceFee) -> Unit,
    onOpenReceipt: (PaymentRecord) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BillingTab.BILLS) }
    val today = BillingRules.todayIso()
    val outstanding = BillingSelectionRules.outstandingBills(bills, today)
    val paid = BillingSelectionRules.paidBills(bills)
    val selectedBill = outstanding.firstOrNull { it.id == selectedBillId }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Billing", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (selectedTab == BillingTab.BILLS) "Your bills" else "Payment history",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (selectedTab == BillingTab.BILLS) {
                        "Select an outstanding bill to pay."
                    } else {
                        "Review your completed payments and open their receipts."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                BillingTabSelector(selected = selectedTab, onSelected = { selectedTab = it })
            }

            if (selectedTab == BillingTab.BILLS) {
                if (loading && bills.isEmpty()) {
                    item {
                        LoadingCard("Loading your bills…")
                    }
                } else if (bills.isEmpty()) {
                    item {
                        EmptyStateCard(
                            "No maintenance bill yet",
                            "Your bills will appear after they are prepared by the condominium office."
                        )
                    }
                } else {
                    item {
                        OutstandingSummary(outstanding)
                    }

                    item {
                        SectionTitle("Outstanding bills")
                    }

                    if (outstanding.isEmpty()) {
                        item {
                            EmptyStateCard(
                                "No outstanding bills",
                                "All maintenance fees for this unit are settled."
                            )
                        }
                    } else {
                        items(outstanding, key = { it.id }) { bill ->
                            OutstandingBillCard(
                                bill = bill,
                                selected = bill.id == selectedBillId,
                                overdue = bill.dueDate.isNotBlank() && bill.dueDate < today,
                                onClick = { onBillSelected(bill.id) }
                            )
                        }
                    }

                    if (paid.isNotEmpty()) {
                        item {
                            SectionTitle("Paid bills")
                        }
                        items(paid, key = { it.id }) { bill ->
                            PaidBillRow(bill)
                        }
                    }
                }
            } else {
                if (loading && payments.isEmpty()) {
                    item {
                        LoadingCard("Loading payment history…")
                    }
                } else if (payments.isEmpty()) {
                    item {
                        EmptyStateCard(
                            "No payments yet",
                            "Completed maintenance-fee payments will appear here."
                        )
                    }
                }
                items(payments, key = { it.id }) { payment ->
                    PaymentHistoryRow(payment = payment, onClick = { onOpenReceipt(payment) })
                }
            }
        }

        if (selectedTab == BillingTab.BILLS && bills.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    PrimaryActionButton(
                        text = selectedBill?.let { "Continue to payment · ${it.amount}" }
                            ?: "Continue to payment",
                        enabled = selectedBill != null,
                        onClick = { selectedBill?.let(onContinuePayment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OutstandingSummary(outstanding: List<MaintenanceFee>) {
    val total = outstanding.sumOf { parseAmount(it.amount) }
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
                "Outstanding fees",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB9C9EE)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                formatMoney(total),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "${outstanding.size} bill${if (outstanding.size == 1) "" else "s"} to pay",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD8E2F6)
            )
        }
    }
}

@Composable
private fun OutstandingBillCard(
    bill: MaintenanceFee,
    selected: Boolean,
    overdue: Boolean,
    onClick: () -> Unit
) {
    CondoCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioIndicator(selected)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(BillingRules.monthLabel(bill.month), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Due ${bill.dueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(bill.amount, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                StatusPill(
                    if (overdue) "Overdue" else "Due",
                    if (overdue) PillTone.CORAL else PillTone.GOLD
                )
            }
        }
    }
}

@Composable
private fun PaidBillRow(bill: MaintenanceFee) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(SuccessPale, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.CHECK, Success, modifier = Modifier.size(13.dp), strokeWidth = 2.4.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(BillingRules.monthLabel(bill.month), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Paid",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(bill.amount, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                StatusPill("Paid", PillTone.GREEN)
            }
        }
    }
}

@Composable
private fun RadioIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                2.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                CircleShape
            )
            .padding(4.dp)
    ) {
        if (selected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun BillingTabSelector(
    selected: BillingTab,
    onSelected: (BillingTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BillingTab.entries.forEach { tab ->
                val active = selected == tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelected(tab) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent
                ) {
                    Text(
                        tab.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) BlueDeep else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(payment: PaymentRecord, onClick: () -> Unit) {
    CondoCard(modifier = Modifier.clickable(onClick = onClick)) {
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
            Spacer(Modifier.width(4.dp))
            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseAmount(amount: String): Double =
    amount.replace("RM", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0

private fun formatMoney(amount: Double): String =
    "RM " + NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)

private fun displayDate(value: String): String =
    value.replace("T", " ").take(16).ifBlank { "—" }
