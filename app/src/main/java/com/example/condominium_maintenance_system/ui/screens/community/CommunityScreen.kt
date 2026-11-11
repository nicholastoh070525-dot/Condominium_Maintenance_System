package com.example.condominium_maintenance_system.ui.screens.community

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
fun CommunityScreen(
    role: UserRole,
    items: List<LostFoundItem>,
    onBack: () -> Unit,
    onAddItem: (LostFoundItem) -> Unit,
    onItemAction: (LostFoundItem, String) -> Unit,
    onAction: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showItemDialog by remember { mutableStateOf(false) }
    val visibleItems = items.filter {
        search.isBlank() || it.title.contains(search, ignoreCase = true) || it.detail.contains(search, ignoreCase = true)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M4",
            title = "Lost & found",
            onBack = onBack,
            trailing = GlyphType.ADD,
            onTrailing = { showItemDialog = true }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Missing something?", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Search what has been found, or post an item.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search keys, wallet, phone…") },
                    leadingIcon = { Glyph(GlyphType.SEARCH, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                InfoBanner(
                    title = if (role == UserRole.RESIDENT) "Resident community board" else "Management moderation",
                    body = if (role == UserRole.RESIDENT) {
                        "Report lost items and browse everything posted by management."
                    } else {
                        "Review posts, update item status and help return items to their owners."
                    },
                    glyph = GlyphType.SEARCH,
                    badge = role.displayName
                )
            }
            item {
                SectionTitle("Recently posted", action = "View all") { onAction("All lost and found posts opened.") }
            }
            items(visibleItems) { item ->
                LostItemCard(
                    item = item,
                    role = role,
                    onAction = { onItemAction(item, it) }
                )
            }
            if (visibleItems.isEmpty()) {
                item { EmptyStateCard("Nothing found", "Try a different search or post a new item.") }
            }
            item {
                PrimaryActionButton(
                    if (role == UserRole.RESIDENT) "Report a lost item" else "Post a found item"
                ) { showItemDialog = true }
            }
        }
    }

    if (showItemDialog) {
        AddLostFoundDialog(
            role = role,
            onDismiss = { showItemDialog = false },
            onSubmit = {
                onAddItem(it)
                showItemDialog = false
            }
        )
    }
}

@Composable
private fun LostItemCard(item: LostFoundItem, role: UserRole, onAction: (String) -> Unit) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        if (item.kind == "Found") BluePale else GoldPale,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (item.kind == "Found") "⌕" else "◇",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (item.kind == "Found") BluePrimary else Color(0xFF8B6C32)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    item.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                item.status,
                when (item.status) {
                    "Unclaimed" -> PillTone.BLUE
                    "Claimed", "Returned" -> PillTone.PURPLE
                    else -> PillTone.GOLD
                }
            )
        }
        if (role == UserRole.MANAGEMENT && item.status != "Returned") {
            Text(
                "Mark returned",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onAction("Returned") }
                    .padding(top = 10.dp, start = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddLostFoundDialog(
    role: UserRole,
    onDismiss: () -> Unit,
    onSubmit: (LostFoundItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    FormDialog(
        title = if (role == UserRole.RESIDENT) "Report a lost item" else "Post a found item",
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Item name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = detail,
            onValueChange = { detail = it },
            label = { Text("Location and details") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton(
            text = "Post item",
            enabled = title.isNotBlank() && detail.isNotBlank()
        ) {
            onSubmit(
                LostFoundItem(
                    title = title,
                    detail = "$detail · Just now",
                    status = if (role == UserRole.RESIDENT) "Lost report" else "Unclaimed",
                    kind = if (role == UserRole.RESIDENT) "Lost" else "Found"
                )
            )
        }
    }
}
