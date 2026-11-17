package com.example.condominium_maintenance_system.ui.screens.requests

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.InfoBanner
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.Coral
import com.example.condominium_maintenance_system.ui.theme.Success
import com.example.condominium_maintenance_system.ui.theme.SuccessPale

private const val MAX_PHOTOS = 4

@Composable
fun NewRequestScreen(
    onBack: () -> Unit,
    submitting: Boolean = false,
    onSubmit: (String, String, String, String, List<Uri>) -> Unit
) {
    var category by remember { mutableStateOf("Plumbing") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Normal") }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val categories = listOf("Plumbing", "Electrical", "Air-con", "Lift", "Common area", "Other")
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selected ->
        if (selected != null) {
            photos = (photos + selected).distinct().take(MAX_PHOTOS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "New request", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "What needs fixing?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Add the details and photos. Management can then schedule a technician and keep the status updated.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
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
                    onValueChange = { if (it.length <= 80) title = it },
                    label = { Text("Short title") },
                    placeholder = { Text("Example: Kitchen sink is leaking") },
                    supportingText = { Text("${title.length}/80") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 800) description = it },
                    label = { Text("Describe the problem") },
                    placeholder = { Text("Where is it, when did it start, and what have you noticed?") },
                    minLines = 5,
                    supportingText = { Text("${description.length}/800") },
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
                                selectedContainerColor = if (option == "Urgent") Coral
                                else MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Photos", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${photos.size}/$MAX_PHOTOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Select one photo at a time. The picker closes after each selection.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    photos.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(RoundedCornerShape(18.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected maintenance photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(5.dp)
                                    .size(25.dp)
                                    .background(Color.Black.copy(alpha = 0.62f), CircleShape)
                                    .clickable { photos = photos - uri },
                                contentAlignment = Alignment.Center
                            ) {
                                Glyph(
                                    GlyphType.CLOSE,
                                    Color.White,
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 2.2.dp
                                )
                            }
                        }
                    }
                    if (photos.size < MAX_PHOTOS) {
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Glyph(
                                    GlyphType.ADD,
                                    MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Add photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            item {
                InfoBanner(
                    title = "Photos are optional but helpful",
                    body = "Take a clear photo of the affected area. Avoid including people, documents or private information.",
                    glyph = GlyphType.UPLOAD
                )
            }
            item {
                PrimaryActionButton(
                    text = if (submitting) "Uploading and submitting…" else "Submit maintenance request",
                    enabled = !submitting && title.isNotBlank() && description.isNotBlank()
                ) {
                    onSubmit(category, title.trim(), description.trim(), urgency, photos)
                }
            }
        }
    }
}

@Composable
fun RequestSubmittedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(SuccessPale, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Glyph(
                    GlyphType.CHECK,
                    Success,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.8.dp
                )
            }
        },
        title = {
            Text(
                "Complaint submitted",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                "Your maintenance request is now with the management team. You can follow its progress from the Maintenance page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Success,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("OK")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}
