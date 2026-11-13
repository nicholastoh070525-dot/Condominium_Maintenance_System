package com.example.condominium_maintenance_system.ui.screens.account

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.VisitorDateTimeRules
import com.example.condominium_maintenance_system.ui.VisitorRequest
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.InfoBanner
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep

@Composable
fun AddVisitorScreen(
    submitting: Boolean,
    onBack: () -> Unit,
    onSubmit: (VisitorRequest) -> Unit
) {
    var visitorName by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M1",
            title = "Visitor parking",
            onBack = onBack,
            trailing = GlyphType.CALENDAR
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Text("Add a visitor", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Enter the vehicle details and schedule. Management will review the request.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(listOf(BlueDeep, MaterialTheme.colorScheme.primary)),
                                RoundedCornerShape(25.dp)
                            )
                            .padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚘", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Vehicle pass request",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                "Pending → Approved → Complete",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD6E1F9)
                            )
                        }
                    }
                }
            }
            item {
                CondoCard {
                    Text("Visitor details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    VisitorOutlinedField(
                        label = "Visitor name",
                        value = visitorName,
                        onValueChange = {
                            visitorName = it
                            validationMessage = null
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VisitorOutlinedField(
                            label = "Vehicle color",
                            value = vehicleColor,
                            onValueChange = {
                                vehicleColor = it
                                validationMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        VisitorOutlinedField(
                            label = "Number plate",
                            value = plate,
                            onValueChange = {
                                plate = it.uppercase()
                                validationMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                CondoCard {
                    Text("Visit schedule", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Date and time are entered in separate fields.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            VisitorOutlinedField(
                                label = "Date",
                                value = date,
                                placeholder = "DD/MM/YYYY",
                                keyboardType = KeyboardType.Number,
                                isError = dateError != null,
                                onValueChange = {
                                    date = VisitorDateTimeRules.formatDateInput(it)
                                    dateError = null
                                    validationMessage = null
                                }
                            )
                            FieldError(dateError)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            VisitorOutlinedField(
                                label = "Time",
                                value = time,
                                placeholder = "6:00 PM",
                                isError = timeError != null,
                                onValueChange = {
                                    time = it
                                    timeError = null
                                    validationMessage = null
                                }
                            )
                            FieldError(timeError)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    VisitorOutlinedField(
                        label = "Note (optional)",
                        value = note,
                        onValueChange = { note = it },
                        singleLine = false
                    )
                }
            }
            item {
                InfoBanner(
                    title = "What happens next?",
                    body = "The request becomes Pending. After management approves it, the resident can open the vehicle pass.",
                    glyph = GlyphType.CHECK,
                    badge = "Review"
                )
            }
            validationMessage?.let { message ->
                item {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                PrimaryActionButton(
                    text = if (submitting) "Submitting request…" else "Send for approval",
                    enabled = !submitting,
                    onClick = {
                        val dateTime = VisitorDateTimeRules.validate(date, time)
                        dateError = dateTime.dateError
                        timeError = dateTime.timeError
                        validationMessage = when {
                            visitorName.isBlank() -> "Enter the visitor name."
                            vehicleColor.isBlank() -> "Enter the vehicle color."
                            plate.isBlank() -> "Enter the vehicle number plate."
                            !dateTime.isValid -> "Correct the date and time before continuing."
                            else -> null
                        }
                        if (validationMessage == null && dateTime.isoValue != null) {
                            onSubmit(
                                VisitorRequest(
                                    name = visitorName.trim(),
                                    plate = plate.trim().uppercase(),
                                    visitTime = dateTime.isoValue,
                                    bay = "",
                                    status = "PENDING",
                                    vehicleColor = vehicleColor.trim(),
                                    note = note.trim()
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VisitorOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun FieldError(message: String?) {
    if (message != null) {
        Text(
            message,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
