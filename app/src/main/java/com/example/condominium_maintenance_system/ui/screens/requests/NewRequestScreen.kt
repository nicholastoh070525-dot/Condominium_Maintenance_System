package com.example.condominium_maintenance_system.ui.screens.requests

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
import com.example.condominium_maintenance_system.ui.components.*
import com.example.condominium_maintenance_system.ui.screens.common.*
import com.example.condominium_maintenance_system.ui.theme.*

@Composable
fun NewRequestScreen(
    onBack: () -> Unit,
    onSubmit: (MaintenanceRequest) -> Unit
) {
    var category by remember { mutableStateOf("Plumbing") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Normal") }
    val categories = listOf("Plumbing", "Electrical", "Air-con", "Other")

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(code = null, title = "New request", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("What needs fixing?", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Give us a few details and the management office will follow up.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Text("Choose a category", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Short title") },
                    placeholder = { Text("Example: Kitchen sink is leaking") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe the problem") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
            item {
                Text("How urgent is it?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Normal", "Urgent").forEach { option ->
                        FilterChip(
                            selected = urgency == option,
                            onClick = { urgency = option },
                            label = { Text(option) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (option == "Urgent") Coral else MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            item {
                InfoBanner(
                    title = "Photos can be added in the full app",
                    body = "This local interface keeps the request flow available without an external database.",
                    glyph = GlyphType.UPLOAD
                )
            }
            item {
                PrimaryActionButton(
                    text = "Submit maintenance request",
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    onSubmit(
                        MaintenanceRequest(
                            title = title,
                            reference = "#MNT-${(1044..1099).random()}",
                            detail = "$category · $urgency",
                            status = "Pending",
                            time = "Just now"
                        )
                    )
                }
            }
        }
    }
}
