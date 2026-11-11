package com.example.condominium_maintenance_system.ui.screens.booking

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
fun BookingScreen(
    role: UserRole,
    bookings: List<FacilityBooking>,
    onBack: () -> Unit,
    onAddBooking: (FacilityBooking) -> Unit,
    onBookingAction: (FacilityBooking, String) -> Unit,
    onAction: (String) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(6) }
    var showBookingDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M3",
            title = "Facility booking",
            onBack = onBack,
            trailing = GlyphType.CALENDAR,
            onTrailing = { onAction("Facility calendar opened.") }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (role == UserRole.RESIDENT) "Book your shared space" else "Facility operations",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (role == UserRole.RESIDENT) {
                        "Check availability, send a request and follow the approval status."
                    } else {
                        "Review resident bookings and manage facility availability."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                InfoBanner(
                    title = "November availability",
                    body = "Approvals usually take less than one working day.",
                    glyph = GlyphType.CALENDAR,
                    badge = "Open"
                )
            }
            item {
                SectionTitle("Choose a date")
                Spacer(Modifier.height(10.dp))
                WeekStrip()
            }
            if (role == UserRole.RESIDENT) {
                item {
                    SectionTitle("Available facilities")
                }
                items(4) { index ->
                    val facilities = listOf(
                        Pair("Swimming pool", "Available · 3 time slots"),
                        Pair("Function room", "Available · 4 hours"),
                        Pair("BBQ pit", "Limited · Ends 10 PM"),
                        Pair("Sports court", "Available · 2 hours")
                    )
                    FacilityRow(index, facilities[index].first, facilities[index].second)
                }
                item {
                    PrimaryActionButton("Book a facility") { showBookingDialog = true }
                }
            } else {
                item {
                    SectionTitle("Booking requests", action = "View all") { onAction("All bookings opened.") }
                }
                items(bookings.filter { it.status == "Pending" }) { booking ->
                    CondoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(booking.facility, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${booking.unit} · ${booking.time}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill("Pending", PillTone.GOLD)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onBookingAction(booking, "Approved") }) {
                                Text("Approve")
                            }
                            TextButton(
                                onClick = { onBookingAction(booking, "Rejected") },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
                if (bookings.none { it.status == "Pending" }) {
                    item { EmptyStateCard("All requests reviewed", "New facility bookings will appear here.") }
                }
                item {
                    SectionTitle("Approved bookings", action = "Calendar") { onAction("Approved facility calendar opened.") }
                }
                items(bookings.filter { it.status == "Approved" }) { booking ->
                    CondoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(booking.facility, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${booking.unit} · ${booking.time}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill("Approved", PillTone.BLUE)
                        }
                    }
                }
            }
        }
    }

    if (showBookingDialog) {
        BookingDialog(
            date = "Thu, 6 Nov",
            onDismiss = { showBookingDialog = false },
            onSubmit = {
                onAddBooking(it)
                showBookingDialog = false
            }
        )
    }
}

@Composable
private fun FacilityRow(index: Int, name: String, detail: String) {
    val palettes = listOf(
        BluePale to BluePrimary,
        PurplePale to Purple,
        GoldPale to Gold,
        Color(0xFFE8EDFF) to Color(0xFF3E5FBF)
    )
    val visual = listOf("≈", "✧", "♨", "⚑")
    val palette = palettes[index]
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(palette.first, RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(visual[index], color = palette.second, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusPill("Available", PillTone.BLUE)
        }
    }
}

@Composable
private fun BookingDialog(
    date: String,
    onDismiss: () -> Unit,
    onSubmit: (FacilityBooking) -> Unit
) {
    var facility by remember { mutableStateOf("Function room") }
    var time by remember { mutableStateOf("2:00 PM – 5:00 PM") }
    FormDialog(title = "Request a booking", onDismiss = onDismiss) {
        Text(
            "Select a facility for $date. Management will review the request.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        listOf("Function room", "Swimming pool", "Sports court", "BBQ pit").forEach { name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .clickable { facility = name }
                    .background(
                        if (facility == name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(
                            2.dp,
                            if (facility == name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .padding(4.dp)
                ) {
                    if (facility == name) {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton("Send booking request") {
            onSubmit(FacilityBooking(facility, "Unit 12-08", "$date · $time", "Pending"))
        }
    }
}
