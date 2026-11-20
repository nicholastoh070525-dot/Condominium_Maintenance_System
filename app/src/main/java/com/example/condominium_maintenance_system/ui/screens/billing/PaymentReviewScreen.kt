package com.example.condominium_maintenance_system.ui.screens.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep

@Composable
fun PaymentReviewScreen(
    bill: MaintenanceFee,
    unitCode: String,
    paymentMethod: String,
    processing: Boolean,
    onBack: () -> Unit,
    onProcessPayment: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Payment review", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(BlueDeep, MaterialTheme.colorScheme.primary)
                                ),
                                RoundedCornerShape(26.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            "Amount to pay",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB9C9EE)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            bill.amount,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "${BillingRules.monthLabel(bill.month)} maintenance fee",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD8E2F6)
                        )
                    }
                }
            }

            item {
                SectionTitle("Payment details")
            }

            item {
                CondoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow("Unit", unitCode.ifBlank { "—" })
                        DetailRow("Billing month", BillingRules.monthLabel(bill.month))
                        DetailRow("Due date", bill.dueDate)
                        DetailRow("Amount", bill.amount)
                    }
                }
            }

            item {
                SectionTitle("Payment method")
            }

            item {
                CondoCard(modifier = Modifier.clickable(onClick = onBack)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(paymentMethod, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Selected payment method",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    "This is a simulated payment. No banking transaction is processed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                    text = if (processing) "Processing payment…" else "Process Payment",
                    enabled = !processing,
                    onClick = onProcessPayment
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
