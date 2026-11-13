package com.example.condominium_maintenance_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.CondoTrackApp
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.VisitorRequest
import com.example.condominium_maintenance_system.ui.screens.account.AccountScreen
import com.example.condominium_maintenance_system.ui.screens.auth.AuthScreen
import com.example.condominium_maintenance_system.ui.screens.billing.BillingScreen
import com.example.condominium_maintenance_system.ui.screens.home.HomeScreen
import com.example.condominium_maintenance_system.ui.settings.AppSettingsStore
import com.example.condominium_maintenance_system.ui.theme.Condominium_Maintenance_SystemTheme
import com.example.condominium_maintenance_system.ui.viewmodel.AuthUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsStore = remember { AppSettingsStore(context) }
            val systemDarkMode = isSystemInDarkTheme()
            var darkMode by remember {
                mutableStateOf(settingsStore.darkMode ?: systemDarkMode)
            }
            var notificationsEnabled by remember {
                mutableStateOf(settingsStore.notificationsEnabled)
            }

            Condominium_Maintenance_SystemTheme(darkTheme = darkMode) {
                CondoTrackApp(
                    notificationsEnabled = notificationsEnabled,
                    darkMode = darkMode,
                    onNotificationsChanged = {
                        settingsStore.setNotificationsEnabled(it)
                        notificationsEnabled = it
                    },
                    onDarkModeChanged = {
                        settingsStore.setDarkMode(it)
                        darkMode = it
                    }
                )
            }
        }
    }
}

@Preview(name = "Authentication", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AuthPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        AuthScreen(
            state = AuthUiState(loading = false),
            onSignIn = { _, _ -> },
            onRegister = {},
            onVerifyRegistration = {},
            onResendRegistration = {},
            onRequestPasswordReset = {},
            onVerifyRecovery = {},
            onResendRecovery = {},
            onResetPassword = {},
            onCancelFlow = {},
            onClearFeedback = {}
        )
    }
}

@Preview(name = "Resident dashboard", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ResidentDashboardPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                role = UserRole.RESIDENT,
                notificationsEnabled = true,
                onNavigate = {},
                onOpenNotifications = {}
            )
        }
    }
}

@Preview(name = "Management dashboard", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ManagementDashboardPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                role = UserRole.MANAGEMENT,
                notificationsEnabled = true,
                onNavigate = {},
                onOpenNotifications = {}
            )
        }
    }
}

@Preview(name = "Maintenance fee payment", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun BillingPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            BillingScreen(
                bills = listOf(
                    MaintenanceFee(
                        id = "preview",
                        month = "November 2026",
                        amount = "RM 285.00",
                        dueDate = "15 Nov 2026",
                        status = "UNPAID"
                    )
                ),
                payments = listOf(
                    PaymentRecord(
                        id = "preview-payment",
                        amount = "RM 285.00",
                        method = "DuitNow",
                        paidAt = "2026-10-12T10:30:00",
                        receiptNumber = "CT-20261012-001"
                    )
                ),
                loading = false,
                paying = false,
                onBack = {},
                onPay = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Resident profile", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ResidentProfilePreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AccountScreen(
                role = UserRole.RESIDENT,
                profile = previewResident(),
                profiles = emptyList(),
                visitors = listOf(
                    VisitorRequest(
                        name = "Daniel Tan",
                        plate = "WXY 8821",
                        visitTime = "2026-11-15T18:00:00+08:00",
                        bay = "",
                        status = "APPROVED",
                        vehicleColor = "Silver",
                        unitCode = "A-15-12",
                        residentName = "Aina Mahmud",
                        approvedAt = "2026-11-12T15:30:00+08:00",
                        passReference = "CT-20261112-A1B2C3"
                    )
                ),
                loading = false,
                processingId = null,
                activePass = null,
                onBack = {},
                onOpenVisitorForm = {},
                onReviewVisitor = { _, _ -> },
                onOpenPass = {},
                onClosePass = {},
                onSignOut = {}
            )
        }
    }
}

@Preview(name = "Management access", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ManagementAccessPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AccountScreen(
                role = UserRole.MANAGEMENT,
                profile = UserProfile(
                    id = "manager",
                    fullName = "Nadia Safiya",
                    email = "management@condotrack.my",
                    phone = "+60 12 777 0198",
                    role = UserRole.MANAGEMENT,
                    avatarKey = "NS"
                ),
                profiles = listOf(previewResident()),
                visitors = listOf(
                    VisitorRequest(
                        id = "pending",
                        name = "Mei Lin",
                        plate = "VCT 2048",
                        visitTime = "2026-11-15T10:00:00+08:00",
                        bay = "",
                        status = "PENDING",
                        vehicleColor = "White",
                        unitCode = "A-15-12",
                        residentName = "Aina Mahmud"
                    )
                ),
                loading = false,
                processingId = null,
                activePass = null,
                onBack = {},
                onOpenVisitorForm = {},
                onReviewVisitor = { _, _ -> },
                onOpenPass = {},
                onClosePass = {},
                onSignOut = {}
            )
        }
    }
}

private fun previewResident() = UserProfile(
    id = "resident",
    fullName = "Aina Mahmud",
    email = "aina.mahmud@gmail.com",
    phone = "+60 12 555 0182",
    role = UserRole.RESIDENT,
    unitId = "unit-a1512",
    unitCode = "A-15-12",
    avatarKey = "AM"
)
