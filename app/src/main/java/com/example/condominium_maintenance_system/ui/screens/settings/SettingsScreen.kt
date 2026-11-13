package com.example.condominium_maintenance_system.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader

@Composable
fun SettingsScreen(
    profile: UserProfile?,
    notificationsEnabled: Boolean,
    darkMode: Boolean,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = null,
            title = "Settings",
            onBack = onBack
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Your preferences", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Manage profile details, notifications and the CondoTrack appearance.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                CondoCard {
                    SettingsSwitchRow(
                        title = "Notifications",
                        subtitle = "Show in-app announcement and request alerts.",
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChanged
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                    SettingsSwitchRow(
                        title = "Dark mode",
                        subtitle = "Use the dark CondoTrack interface.",
                        checked = darkMode,
                        onCheckedChange = onDarkModeChanged
                    )
                }
            }
            item {
                Text("Account", style = MaterialTheme.typography.titleMedium)
            }
            item {
                CondoCard {
                    SettingsActionRow(
                        title = "Edit Profile",
                        subtitle = listOfNotNull(
                            profile?.fullName,
                            profile?.unitCode,
                            profile?.role?.displayName
                        ).joinToString(" · "),
                        onClick = onEditProfile
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Glyph(
                GlyphType.ACCOUNT,
                MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                subtitle.ifBlank { "Update your profile information" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
