package com.example.condominium_maintenance_system.ui.screens.home

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
fun HomeScreen(
    role: UserRole,
    notificationsEnabled: Boolean = true,
    onNavigate: (AppDestination) -> Unit,
    onOpenNotifications: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                role = role,
                notificationsEnabled = notificationsEnabled,
                onOpenNotifications = onOpenNotifications
            )
        }
        item {
            if (role == UserRole.RESIDENT) {
                ResidentHero(onViewRequest = { onNavigate(AppDestination.MAINTENANCE) })
            } else {
                ManagementHero()
            }
        }
        item {
            SectionTitle(
                title = if (role == UserRole.RESIDENT) "Your services" else "Management modules",
                action = if (notificationsEnabled) "Notifications" else null,
                onAction = onOpenNotifications
            )
        }
        val modules = moduleItems(role)
        items(modules.chunked(2).size) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                modules.chunked(2)[rowIndex].forEach { item ->
                    ModuleCard(
                        code = item.code,
                        title = item.title,
                        detail = item.detail,
                        symbol = item.symbol,
                        symbolColor = item.symbolColor,
                        symbolBackground = item.symbolBackground,
                        onClick = { onNavigate(item.destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (modules.chunked(2)[rowIndex].size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    role: UserRole,
    notificationsEnabled: Boolean,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BrandMark()
            Column {
                Row {
                    Text("Condo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Track",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "Skyline Residences · Tower B",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (notificationsEnabled) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable(onClick = onOpenNotifications),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Glyph(GlyphType.BELL, MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ResidentHero(onViewRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(BlueDeep, MaterialTheme.colorScheme.primary)
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(BluePale, CircleShape)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "NEXT VISIT · SATURDAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC8D6FA)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Everything is\non track.",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Air-con servicing · 9:00–10:30 AM",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFDCE6FF)
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(4.dp, BlueBright, CircleShape)
                            .padding(9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("82%", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "Home care score\nUp 6% this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D6FA)
                    )
                }
                Button(
                    onClick = onViewRequest,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 17.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Ink
                    )
                ) {
                    Text("View request", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ManagementHero() {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        MetricCard("Open work", "12", Modifier.weight(1f), highlighted = true)
        MetricCard("Due today", "03", Modifier.weight(1f))
        MetricCard("Completed", "28", Modifier.weight(1f))
    }
}

private data class ModuleItem(
    val code: String,
    val title: String,
    val detail: String,
    val symbol: String,
    val symbolColor: Color,
    val symbolBackground: Color,
    val destination: AppDestination
)

private fun moduleItems(role: UserRole): List<ModuleItem> {
    val items = listOf(
        ModuleItem("MODULE 1", "Users & access", "Profile · visitors · roles", "◎", Purple, PurplePale, AppDestination.ACCESS),
        ModuleItem("MODULE 2", "Maintenance", "Requests · work · schedule", "✦", Color(0xFFA05D49), CoralPale, AppDestination.MAINTENANCE),
        ModuleItem("MODULE 3", "Facility booking", "Availability · approvals", "▣", BluePrimary, BluePale, AppDestination.BOOKINGS),
        ModuleItem("MODULE 4", "Lost & found", "Lost · found · claimed", "⌕", Color(0xFF8B6C32), GoldPale, AppDestination.LOST_FOUND),
        ModuleItem("MODULE 5", "Announcements", "Notices · notifications", "◖", BluePrimary, BluePale, AppDestination.ANNOUNCEMENTS),
        ModuleItem("MODULE 6", "Billing", "Maintenance fee · pay online", "▤", Color(0xFF3E5FBF), Color(0xFFE8EDFF), AppDestination.BILLING)
    )
    return if (role == UserRole.RESIDENT) items else items.dropLast(1)
}
