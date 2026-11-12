package com.example.condominium_maintenance_system.ui.screens.account

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.VisitorRequest
import com.example.condominium_maintenance_system.ui.components.Avatar
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.InfoBanner
import com.example.condominium_maintenance_system.ui.components.MetricCard
import com.example.condominium_maintenance_system.ui.components.PillTone
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.components.StatusPill
import com.example.condominium_maintenance_system.ui.screens.common.FormDialog
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.BluePale
import com.example.condominium_maintenance_system.ui.theme.Coral
import com.example.condominium_maintenance_system.ui.theme.GoldPale
import com.example.condominium_maintenance_system.ui.theme.Ink
import com.example.condominium_maintenance_system.ui.theme.PurplePale

private val ApproveGreen = Color(0xFF218B55)
private val ApprovePale = Color(0xFFE7F6EC)
private val RejectRed = Color(0xFFD63F32)
private val RejectPale = Color(0xFFFFEAE7)

private val avatarPalettes = listOf(
    "AM" to (Color(0xFFFFF0DD) to Color(0xFF3F3027)),
    "⌂" to (Color(0xFFDCE7FF) to Color(0xFF173B86)),
    "✦" to (Color(0xFFEFEAFF) to Color(0xFF574294)),
    "☀" to (Color(0xFFFFF4D7) to Color(0xFF73501B)),
    "≈" to (Color(0xFFDFF4FF) to Color(0xFF17617D)),
    "◇" to (Color(0xFFE5FBEF) to Color(0xFF296348))
)

@Composable
fun AccountScreen(
    role: UserRole,
    profile: UserProfile?,
    profiles: List<UserProfile>,
    visitors: List<VisitorRequest>,
    loading: Boolean,
    processingId: String?,
    activePass: VisitorRequest?,
    onBack: () -> Unit,
    onSaveProfile: (String, String, String, String, String?) -> Unit,
    onUploadAvatar: (Uri, ContentResolver) -> Unit,
    onAddVisitor: (VisitorRequest) -> Unit,
    onReviewVisitor: (VisitorRequest, String) -> Unit,
    onOpenPass: (VisitorRequest) -> Unit,
    onClosePass: () -> Unit,
    onSignOut: () -> Unit
) {
    var showVisitorDialog by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }
    var visitorAction by remember { mutableStateOf<Pair<VisitorRequest, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M1",
            title = "User & access",
            onBack = onBack,
            trailing = if (role == UserRole.MANAGEMENT) GlyphType.PEOPLE else GlyphType.ADD,
            onTrailing = {
                if (role == UserRole.RESIDENT) showVisitorDialog = true
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    if (role == UserRole.RESIDENT) "People and access" else "Accounts and visitor access",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    if (role == UserRole.RESIDENT) {
                        "Manage your profile and the people visiting your unit."
                    } else {
                        "Review resident accounts and manage visitor approvals."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ProfileHero(
                    profile = profile,
                    onEdit = { showProfileEditor = true }
                )
            }

            if (loading && visitors.isEmpty() && profiles.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (role == UserRole.RESIDENT) {
                item {
                    SectionTitle("Visitor access", action = "Add visitor") { showVisitorDialog = true }
                }
                if (visitors.isEmpty() && !loading) {
                    item {
                        EmptyStateCard(
                            "No visitor requests",
                            "Add a visitor to create a request for management review."
                        )
                    }
                }
                items(visitors, key = { it.id.ifBlank { "${it.name}-${it.plate}" } }) { visitor ->
                    ResidentVisitorCard(
                        visitor = visitor,
                        processing = processingId == visitor.id,
                        onOpenPass = { onOpenPass(visitor) }
                    )
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard("Users", profiles.size.toString(), Modifier.weight(1f), highlighted = true)
                        MetricCard("Pending", visitors.count { it.status == "PENDING" }.toString(), Modifier.weight(1f))
                        MetricCard("Approved", visitors.count { it.status == "APPROVED" }.toString(), Modifier.weight(1f))
                    }
                }
                item {
                    SectionTitle("Visitor approvals")
                }
                val pending = visitors.filter { it.status.equals("PENDING", ignoreCase = true) }
                if (pending.isEmpty() && !loading) {
                    item {
                        EmptyStateCard("No pending requests", "New visitor requests will appear here.")
                    }
                }
                items(pending, key = { it.id }) { visitor ->
                    PendingVisitorCard(
                        visitor = visitor,
                        processing = processingId == visitor.id,
                        onApprove = { visitorAction = visitor to "APPROVED" },
                        onReject = { visitorAction = visitor to "REJECTED" }
                    )
                }
                item {
                    SectionTitle("Approved and completed", action = "Requests")
                }
                items(
                    visitors.filter {
                        it.status.equals("APPROVED", true) || it.status.equals("COMPLETE", true) ||
                            it.status.equals("REJECTED", true)
                    },
                    key = { it.id }
                ) { visitor ->
                    ManagementVisitorRow(visitor)
                }
                item {
                    SectionTitle("User directory", action = "Always active")
                }
                items(profiles, key = { it.id }) { user ->
                    DirectoryUserCard(user)
                }
            }

            item {
                SectionTitle("Account")
            }
            item {
                CondoCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSignOut),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RejectPale, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Glyph(GlyphType.CLOSE, RejectRed, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text("Sign out", style = MaterialTheme.typography.labelLarge, color = RejectRed)
                            Text(
                                "End this CondoTrack session",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showVisitorDialog && profile != null) {
        AddVisitorDialog(
            onDismiss = { showVisitorDialog = false },
            onSubmit = {
                onAddVisitor(it)
                showVisitorDialog = false
            }
        )
    }

    if (showProfileEditor && profile != null) {
        ProfileEditorDialog(
            profile = profile,
            onDismiss = { showProfileEditor = false },
            onUploadAvatar = { uri, resolver ->
                onUploadAvatar(uri, resolver)
                showProfileEditor = false
            },
            onSave = { name, phone, email, avatarKey, avatarUrl ->
                onSaveProfile(name, phone, email, avatarKey, avatarUrl)
                showProfileEditor = false
            }
        )
    }

    visitorAction?.let { (visitor, status) ->
        VisitorDecisionDialog(
            visitor = visitor,
            status = status,
            processing = processingId == visitor.id,
            onDismiss = { visitorAction = null },
            onConfirm = {
                onReviewVisitor(visitor, status)
            }
        )
        val latestStatus = visitors.firstOrNull { it.id == visitor.id }?.status
        LaunchedEffect(latestStatus, processingId) {
            if (processingId == null && latestStatus != null &&
                !latestStatus.equals("PENDING", ignoreCase = true)
            ) {
                visitorAction = null
            }
        }
    }

    activePass?.let {
        VehiclePassDialog(pass = it, onDismiss = onClosePass)
    }
}

@Composable
private fun ProfileHero(profile: UserProfile?, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(BlueDeep, MaterialTheme.colorScheme.primary)),
                    RoundedCornerShape(26.dp)
                )
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            ProfileAvatar(profile = profile, size = 72.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile?.fullName ?: "Loading profile…",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(
                        profile?.unitCode,
                        profile?.phone,
                        profile?.email
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD6E1F9),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(7.dp))
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                    Text(
                        "${profile?.role?.displayName ?: "User"} · Active",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            Button(
                onClick = onEdit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BlueDeep
                )
            ) {
                Text("Edit", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ResidentVisitorCard(
    visitor: VisitorRequest,
    processing: Boolean,
    onOpenPass: () -> Unit
) {
    val approved = visitor.status.equals("APPROVED", true)
    CondoCard(
        modifier = Modifier.clickable(enabled = approved && !processing, onClick = onOpenPass)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .background(BluePale, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.CARD, MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${visitor.name} · ${visitor.plate}",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${visitor.vehicleColor} vehicle · ${displayVisitTime(visitor.visitTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (approved) {
                    Text(
                        "Tap to open vehicle pass",
                        style = MaterialTheme.typography.labelSmall,
                        color = ApproveGreen
                    )
                }
            }
            VisitorStatusPill(visitor.status)
        }
    }
}

@Composable
private fun PendingVisitorCard(
    visitor: VisitorRequest,
    processing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    CondoCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(visitor.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${visitor.unitCode.ifBlank { "Unit" }} · ${visitor.residentName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "${visitor.vehicleColor} vehicle · ${visitor.plate}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    displayVisitTime(visitor.visitTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (processing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DecisionIconButton(
                        background = ApprovePale,
                        foreground = ApproveGreen,
                        glyph = GlyphType.CHECK,
                        label = "Approve visitor",
                        onClick = onApprove
                    )
                    DecisionIconButton(
                        background = RejectPale,
                        foreground = RejectRed,
                        glyph = GlyphType.CLOSE,
                        label = "Reject visitor",
                        onClick = onReject
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionIconButton(
    background: Color,
    foreground: Color,
    glyph: GlyphType,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Glyph(glyph, foreground, modifier = Modifier.size(21.dp), strokeWidth = 2.5.dp)
        }
    }
}

@Composable
private fun ManagementVisitorRow(visitor: VisitorRequest) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${visitor.name} · ${visitor.plate}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "${visitor.unitCode} · ${displayVisitTime(visitor.visitTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VisitorStatusPill(visitor.status)
        }
    }
}

@Composable
private fun DirectoryUserCard(user: UserProfile) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(profile = user, size = 44.dp)
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.labelLarge)
                Text(
                    listOfNotNull(user.role.displayName, user.unitCode, user.email).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusPill("Active", PillTone.GREEN)
        }
    }
}

@Composable
private fun VisitorStatusPill(status: String) {
    val normalized = status.uppercase()
    when (normalized) {
        "APPROVED" -> StatusPill("Approved", PillTone.GREEN)
        "REJECTED" -> StatusPill("Rejected", PillTone.CORAL)
        "COMPLETE" -> StatusPill("Complete", PillTone.BLUE)
        else -> StatusPill("Pending", PillTone.GOLD)
    }
}

@Composable
private fun ProfileEditorDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onUploadAvatar: (Uri, ContentResolver) -> Unit,
    onSave: (String, String, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember(profile) { mutableStateOf(profile.fullName) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var avatarKey by remember(profile) { mutableStateOf(profile.avatarKey) }
    var avatarUrl by remember(profile) { mutableStateOf(profile.avatarUrl) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUploadAvatar(uri, context.contentResolver)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileAvatar(
                        profile = profile.copy(avatarKey = avatarKey, avatarUrl = avatarUrl),
                        size = 68.dp
                    )
                    Column {
                        Text("Profile avatar", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Choose a preset or upload a photo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Text("Upload photo")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    avatarPalettes.forEach { (key, colors) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    avatarKey = key
                                    avatarUrl = null
                                },
                            shape = RoundedCornerShape(13.dp),
                            color = colors.first,
                            border = if (avatarKey == key && avatarUrl == null) {
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(key, color = colors.second, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (profile.role == UserRole.MANAGEMENT) email = it },
                    label = { Text("Email address") },
                    singleLine = true,
                    readOnly = profile.role == UserRole.RESIDENT,
                    enabled = profile.role == UserRole.MANAGEMENT,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                profile.unitCode?.let {
                    OutlinedTextField(
                        value = "$it · Skyline Residences",
                        onValueChange = {},
                        label = { Text("Residential unit") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email, avatarKey, avatarUrl) },
                enabled = name.isNotBlank() && phone.isNotBlank() && email.isNotBlank()
            ) {
                Text("Save changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ProfileAvatar(profile: UserProfile?, size: Dp) {
    val palette = avatarPalettes.firstOrNull { it.first == profile?.avatarKey }?.second
        ?: (GoldPale to Ink)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(palette.first)
            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(size * 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        if (!profile?.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = profile?.avatarUrl,
                contentDescription = "Profile avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                profile?.avatarKey ?: "CT",
                color = palette.second,
                style = if (size >= 60.dp) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.labelLarge
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddVisitorDialog(
    onDismiss: () -> Unit,
    onSubmit: (VisitorRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var visitTime by remember { mutableStateOf("") }

    FormDialog(title = "Add visitor", onDismiss = onDismiss) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Visitor name", color = MaterialTheme.colorScheme.onSurface) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = visitorFieldColors()
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = vehicleColor,
            onValueChange = { vehicleColor = it },
            label = { Text("Vehicle color", color = MaterialTheme.colorScheme.onSurface) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = visitorFieldColors()
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = plate,
            onValueChange = { plate = it.uppercase() },
            label = { Text("Number plate", color = MaterialTheme.colorScheme.onSurface) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = visitorFieldColors()
        )
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(
            value = visitTime,
            onValueChange = { visitTime = it },
            label = { Text("Visit date and time", color = MaterialTheme.colorScheme.onSurface) },
            placeholder = { Text("Example: 15/11/2026 6:00 PM") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = visitorFieldColors()
        )
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton(
            text = "Send for approval",
            enabled = name.isNotBlank() && vehicleColor.isNotBlank() &&
                plate.isNotBlank() && visitTime.isNotBlank()
        ) {
            onSubmit(
                VisitorRequest(
                    name = name,
                    plate = plate,
                    visitTime = visitTime,
                    bay = "",
                    status = "PENDING",
                    vehicleColor = vehicleColor,
                    note = ""
                )
            )
        }
    }
}

@Composable
private fun visitorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun VisitorDecisionDialog(
    visitor: VisitorRequest,
    status: String,
    processing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val approve = status == "APPROVED"
    val accent = if (approve) ApproveGreen else RejectRed
    val pale = if (approve) ApprovePale else RejectPale

    Dialog(
        onDismissRequest = { if (!processing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 20.dp
        ) {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Text(
                        "Visitor Approval",
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(pale, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Glyph(
                            if (approve) GlyphType.CHECK else GlyphType.CLOSE,
                            accent,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        if (approve) "Approve this visitor pass?" else "Reject this visitor request?",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "Verify the visitor information before confirming this action.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        DetailRow("Type", "Visitor request")
                        DetailRow("Visitor", visitor.name)
                        DetailRow("Requested by", "${visitor.residentName} · ${visitor.unitCode}")
                        DetailRow("Vehicle", "${visitor.vehicleColor} · ${visitor.plate}")
                        DetailRow("Visit time", displayVisitTime(visitor.visitTime))
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !processing,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (processing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (approve) "Yes, approve" else "Yes, reject")
                        }
                    }
                    TextButton(onClick = onDismiss, enabled = !processing, modifier = Modifier.fillMaxWidth()) {
                        Text("No, go back")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun VehiclePassDialog(pass: VisitorRequest, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(ApprovePale, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Glyph(GlyphType.CHECK, ApproveGreen, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                }
                Spacer(Modifier.height(10.dp))
                Text("Vehicle pass", style = MaterialTheme.typography.headlineMedium)
                Text(
                    pass.passReference ?: "CONDOTRACK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Visitor", pass.name)
                DetailRow("Unit", pass.unitCode)
                DetailRow("Vehicle color", pass.vehicleColor)
                DetailRow("Number plate", pass.plate)
                DetailRow("Visit time", displayVisitTime(pass.visitTime))
                DetailRow("Approved at", displayVisitTime(pass.approvedAt.orEmpty()))
                DetailRow("Status", if (pass.status == "COMPLETE") "Complete" else "Approved")
                InfoBanner(
                    title = "Show this pass at the guardhouse",
                    body = "Opening the approved pass has completed this visit record.",
                    glyph = GlyphType.CHECK,
                    badge = "Complete"
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close pass")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

private fun displayVisitTime(value: String): String =
    value.replace("T", " ").replace("+08:00", "").take(16).ifBlank { "—" }
