package com.example.condominium_maintenance_system.ui.screens.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.PhoneRules
import com.example.condominium_maintenance_system.ui.RegistrationDraft
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.screens.common.BrandLockup
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.viewmodel.AuthUiState
import kotlinx.coroutines.delay

private enum class AuthPage {
    LOGIN,
    REGISTER,
    REGISTER_OTP,
    FORGOT_EMAIL,
    RECOVERY_OTP,
    NEW_PASSWORD
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (RegistrationDraft) -> Unit,
    onVerifyRegistration: (String) -> Unit,
    onResendRegistration: () -> Unit,
    onRequestPasswordReset: (String) -> Unit,
    onVerifyRecovery: (String) -> Unit,
    onResendRecovery: () -> Unit,
    onResetPassword: (String) -> Unit,
    onCancelFlow: () -> Unit,
    onClearFeedback: () -> Unit
) {
    var page by remember { mutableStateOf(AuthPage.LOGIN) }

    LaunchedEffect(state.pendingSignup?.email) {
        if (state.pendingSignup != null) page = AuthPage.REGISTER_OTP
    }
    LaunchedEffect(state.recoveryEmail, state.recoveryVerified) {
        page = when {
            state.recoveryEmail != null && state.recoveryVerified -> AuthPage.NEW_PASSWORD
            state.recoveryEmail != null -> AuthPage.RECOVERY_OTP
            else -> page
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BrandLockup() }
            item {
                Text(
                    when (page) {
                        AuthPage.LOGIN -> "Home life,\nhandled calmly."
                        AuthPage.REGISTER -> "Create your\nCondoTrack account."
                        AuthPage.REGISTER_OTP -> "Verify your\nemail address."
                        AuthPage.FORGOT_EMAIL -> "Recover your\nCondoTrack account."
                        AuthPage.RECOVERY_OTP -> "Enter your\nrecovery code."
                        AuthPage.NEW_PASSWORD -> "Create a new\npassword."
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    when (page) {
                        AuthPage.LOGIN -> "Maintenance, bookings, notices and fees in one place."
                        AuthPage.REGISTER -> "Residents and management can register with a verified email."
                        AuthPage.REGISTER_OTP -> "We sent a six-digit code to ${state.pendingSignup?.email.orEmpty()}."
                        AuthPage.FORGOT_EMAIL -> "Enter your account email to receive a six-digit recovery code."
                        AuthPage.RECOVERY_OTP -> "Enter the code sent to ${state.recoveryEmail.orEmpty()}."
                        AuthPage.NEW_PASSWORD -> "Your code is verified. Choose a strong new password."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FeedbackBanner(state.error, state.message)
            }

            item {
                when (page) {
                    AuthPage.LOGIN -> LoginPage(
                        busy = state.busy,
                        onSignIn = onSignIn,
                        onRegister = {
                            onClearFeedback()
                            page = AuthPage.REGISTER
                        },
                        onForgot = {
                            onClearFeedback()
                            page = AuthPage.FORGOT_EMAIL
                        }
                    )

                    AuthPage.REGISTER -> RegistrationPage(
                        busy = state.busy,
                        onBack = {
                            onClearFeedback()
                            page = AuthPage.LOGIN
                        },
                        onRegister = onRegister
                    )

                    AuthPage.REGISTER_OTP -> OtpPage(
                        email = state.pendingSignup?.email.orEmpty(),
                        busy = state.busy,
                        onVerify = onVerifyRegistration,
                        onResend = onResendRegistration,
                        onBack = {
                            onCancelFlow()
                            page = AuthPage.REGISTER
                        }
                    )

                    AuthPage.FORGOT_EMAIL -> ForgotEmailPage(
                        busy = state.busy,
                        onBack = {
                            onCancelFlow()
                            page = AuthPage.LOGIN
                        },
                        onSubmit = onRequestPasswordReset
                    )

                    AuthPage.RECOVERY_OTP -> OtpPage(
                        email = state.recoveryEmail.orEmpty(),
                        busy = state.busy,
                        onVerify = onVerifyRecovery,
                        onResend = onResendRecovery,
                        onBack = {
                            onCancelFlow()
                            page = AuthPage.FORGOT_EMAIL
                        }
                    )

                    AuthPage.NEW_PASSWORD -> NewPasswordPage(
                        busy = state.busy,
                        onSubmit = onResetPassword,
                        onBack = {
                            onCancelFlow()
                            page = AuthPage.LOGIN
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginPage(
    busy: Boolean,
    onSignIn: (String, String) -> Unit,
    onRegister: () -> Unit,
    onForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    CondoCard {
        Text("Sign in", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            "Your role and permissions are loaded securely from your profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        AuthField("Email", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        AuthField("Password", password, { password = it }, isPassword = true)
        Spacer(Modifier.height(16.dp))
        AuthPrimaryButton("Sign in", busy, email.isNotBlank() && password.isNotBlank()) {
            onSignIn(email, password)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onForgot) { Text("Forgot password?") }
            TextButton(onClick = onRegister) { Text("Create account") }
        }
    }
}

@Composable
private fun RegistrationPage(
    busy: Boolean,
    onBack: () -> Unit,
    onRegister: (RegistrationDraft) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.RESIDENT) }
    var block by remember { mutableStateOf("A") }
    var floor by remember { mutableIntStateOf(11) }
    var room by remember { mutableIntStateOf(1) }
    var validation by remember { mutableStateOf<String?>(null) }

    CondoCard {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            RoleChoice(UserRole.RESIDENT, role, { role = it }, Modifier.weight(1f))
            RoleChoice(UserRole.MANAGEMENT, role, { role = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        AuthField("Full name", name, { name = it })
        Spacer(Modifier.height(9.dp))
        AuthField("Email", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(9.dp))
        AuthField("Phone number", phone, { phone = it }, KeyboardType.Phone)
        if (role == UserRole.RESIDENT) {
            Spacer(Modifier.height(14.dp))
            Text("Residential unit", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectorField("Block", block, listOf("A", "B"), Modifier.weight(1f)) { block = it }
                SelectorField("Floor", floor.toString(), (11..40).map(Int::toString), Modifier.weight(1f)) {
                    floor = it.toInt()
                }
                SelectorField(
                    "Room",
                    room.toString().padStart(2, '0'),
                    (1..28).map { it.toString().padStart(2, '0') },
                    Modifier.weight(1f)
                ) { room = it.toInt() }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Unit ${block}-${floor.toString().padStart(2, '0')}-${room.toString().padStart(2, '0')} · Skyline Residences",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        AuthField("Password", password, { password = it }, isPassword = true)
        Spacer(Modifier.height(9.dp))
        AuthField("Confirm password", confirmPassword, { confirmPassword = it }, isPassword = true)
        validation?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        AuthPrimaryButton("Send verification code", busy) {
            validation = when {
                name.isBlank() -> "Enter your full name."
                email.isBlank() -> "Enter your email address."
                phone.isBlank() -> "Enter your phone number."
                PhoneRules.validationMessage(phone) != null -> PhoneRules.validationMessage(phone)
                password.length < 8 -> "Password must contain at least 8 characters."
                password != confirmPassword -> "Passwords do not match."
                else -> null
            }
            if (validation == null) {
                onRegister(
                    RegistrationDraft(
                        fullName = name,
                        email = email,
                        phone = phone,
                        password = password,
                        role = role,
                        block = block.takeIf { role == UserRole.RESIDENT },
                        floor = floor.takeIf { role == UserRole.RESIDENT },
                        room = room.takeIf { role == UserRole.RESIDENT }
                    )
                )
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to sign in")
        }
    }
}

@Composable
private fun OtpPage(
    email: String,
    busy: Boolean,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var seconds by remember { mutableIntStateOf(60) }

    LaunchedEffect(email, seconds) {
        if (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
    }

    CondoCard {
        Text("Six-digit verification", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "The code expires after 10 minutes. A resent code replaces the previous code.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6) },
            label = { Text("Verification code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        )
        Spacer(Modifier.height(16.dp))
        AuthPrimaryButton("Verify code", busy, code.length == 6) { onVerify(code) }
        TextButton(
            onClick = {
                if (seconds == 0) {
                    onResend()
                    seconds = 60
                }
            },
            enabled = seconds == 0 && !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (seconds > 0) "Resend code in ${seconds}s" else "Resend code")
        }
        Text(
            if (seconds > 0) {
                "Please wait for the countdown before requesting another code."
            } else {
                "You can request one new code at a time."
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun ForgotEmailPage(
    busy: Boolean,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    CondoCard {
        Text("Forgot password", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            "We will send a six-digit recovery code if the account exists.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        AuthField("Email", email, { email = it }, KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        AuthPrimaryButton("Send recovery code", busy, email.isNotBlank()) {
            onSubmit(email)
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to sign in")
        }
    }
}

@Composable
private fun NewPasswordPage(
    busy: Boolean,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    CondoCard {
        Text("Set new password", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            "Use at least eight characters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        AuthField("New password", password, { password = it }, isPassword = true)
        Spacer(Modifier.height(9.dp))
        AuthField("Confirm new password", confirmation, { confirmation = it }, isPassword = true)
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        AuthPrimaryButton("Update password", busy) {
            error = when {
                password.length < 8 -> "Password must contain at least 8 characters."
                password != confirmation -> "Passwords do not match."
                else -> null
            }
            if (error == null) onSubmit(password)
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to sign in")
        }
    }
}

@Composable
private fun FeedbackBanner(error: String?, message: String?) {
    if (error == null && message == null) return
    val isError = error != null
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = error ?: message.orEmpty(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp)
    )
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    busy: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoleChoice(
    role: UserRole,
    selected: UserRole,
    onSelect: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onSelect(role) },
        shape = RoundedCornerShape(14.dp),
        color = if (role == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            role.displayName,
            modifier = Modifier.padding(vertical = 12.dp),
            color = if (role == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SelectorField(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(13.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.labelLarge)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
