package com.example.condominium_maintenance_system.ui.screens.billing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.ui.BillableUnit
import com.example.condominium_maintenance_system.ui.ManagementBill
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.LoadingCard
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.Coral
import com.example.condominium_maintenance_system.ui.theme.Success
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ManagementBillingScreen(
    month: String,
    bills: List<ManagementBill>,
    monthLabel: String,
    loading: Boolean,
    deletingBills: Boolean,
    onBack: () -> Unit,
    onCreateBills: () -> Unit,
    onDeleteMonth: (String, () -> Unit) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val today = BillingRules.todayIso()
    val paid = bills.filter { it.status.equals("PAID", true) }
    val deletableBills = BillingRules.deletableBillsForMonth(bills, month)
    val due = bills.filter { BillingRules.effectiveStatus(it, today) == "DUE" }
    val overdue = bills.filter { BillingRules.effectiveStatus(it, today) == "OVERDUE" }
    val collected = paid.sumOf { it.amount }
    val outstanding = (due + overdue).sumOf { it.amount }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Billing", onBack = onBack, trailing = GlyphType.CARD)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onPreviousMonth),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                    ) {
                        Text("‹", modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                    }
                    Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                    Surface(
                        modifier = Modifier.clickable(onClick = onNextMonth),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                    ) {
                        Text("›", modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Issue monthly fees and track resident payments.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF142846)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Issued this month",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB8C4D7)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            money(bills.sumOf { it.amount }),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            BillingMetric("${paid.size}", "Paid", Color(0xFFDCE5F7))
                            BillingMetric("${due.size}", "Due", Color(0xFFDCE5F7))
                            BillingMetric("${overdue.size}", "Overdue", Color(0xFFFFB7AA))
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            BillingMetric(money(collected), "Collected", Color.White)
                            BillingMetric(money(outstanding), "Outstanding", Color.White)
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Unit ledger",
                    action = if (deletableBills.isNotEmpty() && !loading && !deletingBills) {
                        "Delete bills"
                    } else {
                        null
                    },
                    onAction = if (deletableBills.isNotEmpty() && !loading && !deletingBills) {
                        { showDeleteDialog = true }
                    } else {
                        null
                    }
                )
            }

            if (loading && bills.isEmpty()) {
                item {
                    LoadingCard("Loading monthly bills…")
                }
            } else if (bills.isEmpty()) {
                item {
                    EmptyStateCard(
                        "No bills for this month",
                        "Create a billing run to issue maintenance fees to resident units."
                    )
                }
            } else {
                items(bills, key = { it.id }) { bill ->
                    ManagementBillRow(bill = bill, today = today)
                }
            }

            item {
                PrimaryActionButton("Create bills", enabled = !loading, onClick = onCreateBills)
            }
        }
    }

    if (showDeleteDialog) {
        DeleteMonthBillsDialog(
            monthLabel = monthLabel,
            unpaidCount = deletableBills.size,
            paidCount = paid.size,
            deleting = deletingBills,
            onDismiss = {
                if (!deletingBills) showDeleteDialog = false
            },
            onConfirm = { password ->
                onDeleteMonth(password) {
                    showDeleteDialog = false
                }
            }
        )
    }
}

@Composable
private fun DeleteMonthBillsDialog(
    monthLabel: String,
    unpaidCount: Int,
    paidCount: Int,
    deleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $monthLabel bills?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This permanently deletes all $unpaidCount unpaid bills for $monthLabel. " +
                        "It cannot be undone."
                )
                if (paidCount > 0) {
                    Text(
                        "$paidCount paid bills and their payment history will be kept.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Management account password") },
                    supportingText = { Text("Required to confirm this deletion.") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !deleting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = password.isNotBlank() && !deleting,
                onClick = { onConfirm(password) }
            ) {
                Text(if (deleting) "Deleting…" else "Delete bills", color = Coral)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun ManagementBillRow(bill: ManagementBill, today: String) {
    val status = BillingRules.effectiveStatus(bill, today)
    val statusColor = when (status) {
        "PAID" -> Success
        "OVERDUE" -> Coral
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    bill.unitCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    bill.residentName.ifBlank { "Resident not linked" },
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Due ${bill.dueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(money(bill.amount), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    status.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun BillingMetric(
    value: String,
    label: String,
    color: Color,
    labelColor: Color = Color(0xFFB8C4D7)
) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
    }
}

@Composable
fun CreateBillsScreen(
    month: String,
    monthLabel: String,
    units: List<BillableUnit>,
    existingBills: List<ManagementBill>,
    loading: Boolean,
    saving: Boolean,
    onBack: () -> Unit,
    onMonthSelected: (String) -> Unit,
    onIssue: (String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var dueDateDigits by remember { mutableStateOf("") }
    var showReview by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val selectedUnits = units
    val existingUnitIds = existingBills.mapTo(mutableSetOf()) { it.unitId }
    val newUnits = selectedUnits.filterNot { it.id in existingUnitIds }
    val skipped = selectedUnits.size - newUnits.size
    val parsedAmount = amount.toDoubleOrNull()
    val dueDate = BillingRules.formatDueDateInput(dueDateDigits)
    val canReview = newUnits.isNotEmpty() &&
        parsedAmount != null && parsedAmount > 0 &&
        BillingRules.parseDueDate(dueDate) != null &&
        !loading

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Create bills", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Billing month", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(7.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !loading && !saving) {
                            showMonthPicker = true
                        },
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = .5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Maintenance fee period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            if (loading) "Loading…" else "Change",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item {
                Text("Prepare $monthLabel fees", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Bills are sent to all registered units across Block A and Block B. Enter the amount and due date, then review before issuing.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount per unit") },
                    prefix = { Text("RM ") },
                    supportingText = { Text("Enter an amount greater than zero") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = dueDateDigits,
                    onValueChange = { dueDateDigits = it.filter(Char::isDigit).take(8) },
                    label = { Text("Due date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    visualTransformation = BillingDateVisualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Example: 15/11/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
            item {
                CondoCard {
                    Text("Preview", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        BillingMetric(
                            "${newUnits.size}",
                            "New bills",
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BillingMetric(
                            "$skipped",
                            "Already issued",
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BillingMetric(
                            if (parsedAmount == null) "RM —"
                            else money(newUnits.size * parsedAmount),
                            "Total",
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                PrimaryActionButton(
                    text = if (saving) "Issuing bills…" else "Review and issue",
                    enabled = canReview && !saving,
                    onClick = { showReview = true }
                )
            }
        }
    }

    if (showReview) {
        AlertDialog(
            onDismissRequest = { if (!saving) showReview = false },
            title = { Text("Issue ${newUnits.size} bills?") },
            text = {
                Text(
                    "$skipped units will be skipped because they already have a $monthLabel bill. " +
                        "The new total is ${money(newUnits.size * (parsedAmount ?: 0.0))}."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        showReview = false
                        onIssue(amount, BillingRules.formatDueDateInput(dueDateDigits))
                    }
                ) {
                    Text(if (saving) "Issuing…" else "Issue bills")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReview = false }, enabled = !saving) {
                    Text("Back")
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }

    if (showMonthPicker) {
        BillingMonthPickerDialog(
            selectedMonth = month,
            onDismiss = { showMonthPicker = false },
            onSelect = { selectedMonth ->
                showMonthPicker = false
                onMonthSelected(selectedMonth)
            }
        )
    }
}

@Composable
private fun BillingMonthPickerDialog(
    selectedMonth: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val selectedParts = remember(selectedMonth) { selectedMonth.split("-") }
    val selectedYear = selectedParts.getOrNull(0)?.toIntOrNull() ?: 2000
    val selectedMonthIndex = ((selectedParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1)
        .coerceIn(0, 11)
    var pickerYear by remember(selectedMonth) { mutableIntStateOf(selectedYear) }
    val monthNames = remember {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose billing month") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { pickerYear = (pickerYear - 1).coerceAtLeast(2000) }
                    ) {
                        Text("‹")
                    }
                    Text("$pickerYear", style = MaterialTheme.typography.titleMedium)
                    TextButton(
                        onClick = { pickerYear = (pickerYear + 1).coerceAtMost(2100) }
                    ) {
                        Text("›")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthNames.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowMonths.forEachIndexed { columnIndex, monthName ->
                                val monthIndex = rowIndex * 3 + columnIndex
                                FilterChip(
                                    selected = pickerYear == selectedYear &&
                                        monthIndex == selectedMonthIndex,
                                    onClick = {
                                        onSelect(BillingRules.monthDate(pickerYear, monthIndex))
                                    },
                                    label = { Text(monthName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}

private fun money(amount: Double): String =
    "RM " + NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)

private object BillingDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = BillingRules.formatDueDateInput(text.text)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 4 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(transformed.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                else -> offset - 2
            }.coerceIn(0, text.length)
        }
        return TransformedText(AnnotatedString(transformed), mapping)
    }
}
