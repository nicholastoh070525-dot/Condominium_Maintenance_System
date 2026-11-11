package com.example.condominium_maintenance_system.ui.screens.account

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
fun AccountScreen(
    role: UserRole,
    visitors: List<VisitorRequest>,
    onBack: () -> Unit,
    onAddVisitor: (VisitorRequest) -> Unit,
    onVisitorAction: (VisitorRequest, String) -> Unit,
    onMessage: (String) -> Unit
) {
    var showVisitorDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M1",
            title = "User & access",
            onBack = onBack,
            trailing = if (role == UserRole.MANAGEMENT) GlyphType.ADD else GlyphType.ACCOUNT,
            onTrailing = {
                if (role == UserRole.RESIDENT) showVisitorDialog = true else onMessage("New user form opened.")
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (role == UserRole.RESIDENT) "People and access" else "Accounts and visitor access",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (role == UserRole.RESIDENT) {
                        "Manage your profile and the people visiting your unit."
                    } else {
                        "Manage resident accounts and approve visitor parking."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                InfoBanner(
                    title = if (role == UserRole.RESIDENT) "Verified resident account" else "Management access",
                    body = if (role == UserRole.RESIDENT) {
                        "You can view your own profile and visitor requests."
                    } else {
                        "You can manage resident accounts and visitor parking approvals."
                    },
                    glyph = GlyphType.SHIELD,
                    badge = role.displayName
                )
            }
            if (role == UserRole.RESIDENT) {
                item {
                    SectionTitle("Your account", action = "Edit") { onMessage("Profile editing opened.") }
                }
                item {
                    CondoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar("AM")
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Aina Mahmud", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Unit 12-08 · +60 12 555 0182",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "Edit",
                                modifier = Modifier.clickable { onMessage("Profile editing opened.") },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    SectionTitle("Visitor parking", action = "Add visitor") { showVisitorDialog = true }
                }
                items(visitors) { visitor ->
                    VisitorCard(visitor)
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard("Residents", "126", Modifier.weight(1f), highlighted = true)
                        MetricCard("Management", "04", Modifier.weight(1f))
                        MetricCard("Pending", "04", Modifier.weight(1f))
                    }
                }
                item {
                    SectionTitle("User directory", action = "Add user") { onMessage("Create user form opened.") }
                }
                item {
                    CondoCard {
                        DirectoryRow("Aina Mahmud", "Unit 12-08 · Resident", "AM", GoldPale, Color(0xFF6A4B39))
                        Spacer(Modifier.height(14.dp))
                        DirectoryRow("Nadia Safiya", "Building administrator", "NS", PurplePale, Color(0xFF51446C))
                    }
                }
                item {
                    SectionTitle("Visitor approvals", action = "View all") { onMessage("All visitor requests opened.") }
                }
                items(visitors.filter { it.status == "Pending" }) { visitor ->
                    CondoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(visitor.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${visitor.plate} · ${visitor.visitTime}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { onVisitorAction(visitor, "Approved") }) {
                                Text("Approve")
                            }
                        }
                    }
                }
                if (visitors.none { it.status == "Pending" }) {
                    item { EmptyStateCard("No requests waiting", "New visitor parking requests will appear here.") }
                }
            }
        }
    }

    if (showVisitorDialog) {
        AddVisitorDialog(
            onDismiss = { showVisitorDialog = false },
            onSubmit = {
                onAddVisitor(it)
                showVisitorDialog = false
            }
        )
    }
}

@Composable
private fun VisitorCard(visitor: VisitorRequest) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.CARD, MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${visitor.name} · ${visitor.plate}", style = MaterialTheme.typography.labelLarge)
                Text(
                    "${visitor.visitTime} · ${visitor.bay}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                visitor.status,
                if (visitor.status == "Approved") PillTone.BLUE else PillTone.GOLD
            )
        }
    }
}

@Composable
private fun DirectoryRow(
    name: String,
    detail: String,
    initials: String,
    background: Color,
    foreground: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(initials, modifier = Modifier.size(42.dp), background = background, foreground = foreground)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.labelLarge)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StatusPill("Active", PillTone.BLUE)
    }
}

@Composable
private fun AddVisitorDialog(
    onDismiss: () -> Unit,
    onSubmit: (VisitorRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("Today, 6:00 PM") }
    FormDialog(title = "Add visitor", onDismiss = onDismiss) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Visitor name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = plate,
            onValueChange = { plate = it.uppercase() },
            label = { Text("Vehicle plate") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Visit time") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton(
            text = "Submit request",
            enabled = name.isNotBlank() && plate.isNotBlank()
        ) {
            onSubmit(VisitorRequest(name, plate, time, "Assigned on arrival", "Pending"))
        }
    }
}
