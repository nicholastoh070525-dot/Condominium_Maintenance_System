package com.example.condominium_maintenance_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.AppDestination
import com.example.condominium_maintenance_system.ui.CondoTrackApp
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.screens.billing.BillingScreen
import com.example.condominium_maintenance_system.ui.screens.home.HomeScreen
import com.example.condominium_maintenance_system.ui.theme.Condominium_Maintenance_SystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Condominium_Maintenance_SystemTheme {
                CondoTrackApp()
            }
        }
    }
}

@Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun CondoTrackPreview() {
    Condominium_Maintenance_SystemTheme {
        CondoTrackApp()
    }
}

@Preview(name = "Resident dashboard", showBackground = true, widthDp = 390, heightDp = 844)
@androidx.compose.runtime.Composable
fun ResidentDashboardPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        CondoTrackApp(initialRole = UserRole.RESIDENT)
    }
}

@Preview(name = "Management dashboard", showBackground = true, widthDp = 390, heightDp = 844)
@androidx.compose.runtime.Composable
fun ManagementDashboardPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        CondoTrackApp(initialRole = UserRole.MANAGEMENT)
    }
}

@Preview(name = "Maintenance fee payment", showBackground = true, widthDp = 390, heightDp = 844)
@androidx.compose.runtime.Composable
fun BillingPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        CondoTrackApp(
            initialRole = UserRole.RESIDENT,
            initialDestination = AppDestination.BILLING
        )
    }
}

@Preview(name = "Resident home direct", showBackground = true, widthDp = 390, heightDp = 844)
@androidx.compose.runtime.Composable
fun ResidentHomeDirectPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                role = UserRole.RESIDENT,
                onNavigate = {},
                onOpenNotifications = {}
            )
        }
    }
}

@Preview(name = "Billing direct", showBackground = true, widthDp = 390, heightDp = 844)
@androidx.compose.runtime.Composable
fun BillingDirectPreview() {
    Condominium_Maintenance_SystemTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            BillingScreen(
                fee = MaintenanceFee(
                    month = "November 2026",
                    amount = "RM 285.00",
                    dueDate = "15 Nov 2026"
                ),
                feePaid = false,
                onBack = {},
                onPay = {}
            )
        }
    }
}
