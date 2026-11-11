package com.example.condominium_maintenance_system.ui.screens.auth

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
fun LoginScreen(
    onSignIn: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENT) }
    var email by remember { mutableStateOf("resident@condocare.my") }
    var password by remember { mutableStateOf("condocare123") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                BrandLockup()
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Home life,\nhandled calmly.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Maintenance, bookings, community notices and monthly fees in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                CondoCard {
                    Text("Sign in", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Choose your account type to preview the correct interface.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LoginRoleChip(
                            role = UserRole.RESIDENT,
                            selected = selectedRole == UserRole.RESIDENT,
                            onClick = {
                                selectedRole = UserRole.RESIDENT
                                email = "resident@condocare.my"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LoginRoleChip(
                            role = UserRole.MANAGEMENT,
                            selected = selectedRole == UserRole.MANAGEMENT,
                            onClick = {
                                selectedRole = UserRole.MANAGEMENT
                                email = "management@condocare.my"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryActionButton(
                        text = "Continue as ${selectedRole.displayName}",
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        onSignIn(selectedRole)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Local demo account · No external database",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FeatureLabel("Requests", GlyphType.LIST)
                    FeatureLabel("Bookings", GlyphType.CALENDAR)
                    FeatureLabel("Fees", GlyphType.CARD)
                }
            }
        }
    }
}

@Composable
private fun LoginRoleChip(
    role: UserRole,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    ) {
        Text(
            text = role.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeatureLabel(label: String, glyph: GlyphType) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Glyph(glyph, MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
