package com.example.condominium_maintenance_system.ui.screens.common

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
import com.example.condominium_maintenance_system.ui.theme.*

@Composable
fun ScreenHeader(
    code: String?,
    title: String,
    onBack: () -> Unit,
    trailing: GlyphType = GlyphType.WORK,
    onTrailing: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onBack),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Glyph(GlyphType.BACK, MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            if (code != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        code,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (onTrailing != null) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onTrailing),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Glyph(trailing, MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Spacer(Modifier.width(42.dp))
        }
    }
}

@Composable
internal fun WeekStrip() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        days.forEachIndexed { index, day ->
            Surface(
                modifier = Modifier.width(48.dp),
                shape = RoundedCornerShape(15.dp),
                color = if (index == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (index == 3) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        day,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == 3) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${index + 3}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (index == 3) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
internal fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column(content = content)
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
internal fun BrandMark() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                Brush.linearGradient(listOf(BlueBright, BlueDeep)),
                RoundedCornerShape(15.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.condotrack_mark),
            contentDescription = null,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
internal fun BrandLockup() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        BrandMark()
        Column {
            Row {
                Text("Condo", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Track",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "Condominium maintenance tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
