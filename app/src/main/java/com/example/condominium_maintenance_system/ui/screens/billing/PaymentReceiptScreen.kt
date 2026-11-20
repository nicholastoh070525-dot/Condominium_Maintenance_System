package com.example.condominium_maintenance_system.ui.screens.billing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.theme.Success
import com.example.condominium_maintenance_system.ui.theme.SuccessPale

@Composable
fun PaymentReceiptScreen(
    payment: PaymentRecord,
    bill: MaintenanceFee,
    unitCode: String,
    onBackToBills: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedSuccessBadge()
            Spacer(Modifier.height(20.dp))
            Text("Payment Successful", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Your maintenance fee has been paid.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(28.dp))

        CondoCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Receipt number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    payment.receiptNumber,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        CondoCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                DetailRow("Unit", unitCode.ifBlank { "—" })
                DetailRow("Billing month", BillingRules.monthLabel(bill.month))
                DetailRow("Amount", payment.amount)
                DetailRow("Method", payment.method)
                DetailRow("Paid at", displayPaidAt(payment.paidAt))
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            PrimaryActionButton("Back to bills", onClick = onBackToBills)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text("Back to home", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AnimatedSuccessBadge() {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.72f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .size(88.dp * scale.value)
            .background(SuccessPale, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(42.dp)) {
            val firstStart = Offset(size.width * 0.18f, size.height * 0.52f)
            val middle = Offset(size.width * 0.42f, size.height * 0.74f)
            val end = Offset(size.width * 0.82f, size.height * 0.28f)
            val firstProgress = (progress.value / 0.45f).coerceIn(0f, 1f)
            val secondProgress = ((progress.value - 0.45f) / 0.55f).coerceIn(0f, 1f)

            if (firstProgress > 0f) {
                drawLine(
                    color = Success,
                    start = firstStart,
                    end = Offset(
                        x = firstStart.x + (middle.x - firstStart.x) * firstProgress,
                        y = firstStart.y + (middle.y - firstStart.y) * firstProgress
                    ),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            if (secondProgress > 0f) {
                drawLine(
                    color = Success,
                    start = middle,
                    end = Offset(
                        x = middle.x + (end.x - middle.x) * secondProgress,
                        y = middle.y + (end.y - middle.y) * secondProgress
                    ),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
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

private fun displayPaidAt(value: String): String =
    value.replace("T", " ").take(16).ifBlank { "—" }
