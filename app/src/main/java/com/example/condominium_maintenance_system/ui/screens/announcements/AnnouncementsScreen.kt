package com.example.condominium_maintenance_system.ui.screens.announcements

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
fun AnnouncementsScreen(
    role: UserRole,
    notices: List<CommunityNotice>,
    onBack: () -> Unit,
    onAddNotice: (CommunityNotice) -> Unit,
    onMarkRead: () -> Unit,
    onAction: (String) -> Unit
) {
    var showPostDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M5",
            title = "Announcements",
            onBack = onBack,
            trailing = if (role == UserRole.MANAGEMENT) GlyphType.ADD else GlyphType.CHECK,
            onTrailing = {
                if (role == UserRole.MANAGEMENT) showPostDialog = true else onMarkRead()
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Stay in the loop", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Important building notices and personal maintenance updates.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(notices) { notice ->
                NoticeCard(notice, role, onAction)
            }
            if (role == UserRole.MANAGEMENT) {
                item {
                    PrimaryActionButton("Post an announcement") { showPostDialog = true }
                }
            }
        }
    }

    if (showPostDialog) {
        AddNoticeDialog(
            onDismiss = { showPostDialog = false },
            onSubmit = {
                onAddNotice(it)
                showPostDialog = false
            }
        )
    }
}

@Composable
private fun NoticeCard(notice: CommunityNotice, role: UserRole, onAction: (String) -> Unit) {
    val pinned = notice.pinned
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        color = if (pinned) BlueDeep else MaterialTheme.colorScheme.surface,
        border = if (pinned) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (pinned) "PINNED · MANAGEMENT" else notice.time.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (pinned) Color(0xFFB9C9EE) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusPill(if (notice.read) "Read" else "Unread", if (notice.read) PillTone.PURPLE else PillTone.BLUE)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                notice.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (pinned) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(5.dp))
            Text(
                notice.body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (pinned) Color(0xFFD8E2F6) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (role == UserRole.MANAGEMENT) {
                Text(
                    "Manage notice",
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { onAction("Announcement actions opened.") },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (pinned) Color.White else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddNoticeDialog(
    onDismiss: () -> Unit,
    onSubmit: (CommunityNotice) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    FormDialog(title = "Post announcement", onDismiss = onDismiss) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton("Publish notice", enabled = title.isNotBlank() && body.isNotBlank()) {
            onSubmit(CommunityNotice(title, body, "Just now"))
        }
    }
}
