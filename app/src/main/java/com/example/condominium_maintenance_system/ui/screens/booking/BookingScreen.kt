package com.example.condominium_maintenance_system.ui.screens.booking

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.condominium_maintenance_system.ui.*
import com.example.condominium_maintenance_system.ui.components.*
import com.example.condominium_maintenance_system.ui.screens.common.*
import com.example.condominium_maintenance_system.ui.theme.*

private const val TODAY = 12
private const val DAYS_IN_MONTH = 30
private const val FIRST_DAY_OFFSET = 6 // 1 Nov 2026 is a Sunday, the grid starts on Monday

private data class FacilityInfo(
    val name: String,
    val symbol: String,
    val background: Color,
    val foreground: Color
)

private val facilities = listOf(
    FacilityInfo("Swimming pool", "≈", BluePale, BluePrimary),
    FacilityInfo("Function room", "✧", PurplePale, Purple),
    FacilityInfo("BBQ pit", "♨", GoldPale, Gold),
    FacilityInfo("Sports court", "⚑", Color(0xFFE8EDFF), Color(0xFF3E5FBF))
)

private val timeSlots = listOf(
    "9:00 AM – 12:00 PM",
    "12:00 PM – 3:00 PM",
    "3:00 PM – 6:00 PM",
    "6:00 PM – 9:00 PM"
)

private fun findBooking(
    bookings: List<FacilityBooking>,
    facility: String,
    date: Int,
    slot: String
): FacilityBooking? = bookings.firstOrNull {
    it.facility == facility && it.date == date && it.slot == slot && it.occupiesSlot()
}

private fun statusTone(status: String): PillTone = when (status) {
    "Approved" -> PillTone.BLUE
    "Rejected" -> PillTone.CORAL
    "Cancelled" -> PillTone.PURPLE
    else -> PillTone.GOLD
}

@Composable
fun BookingScreen(
    role: UserRole,
    residentUnit: String,                       // ← 新增
    bookings: List<FacilityBooking>,
    onBack: () -> Unit,
    onAddBooking: (FacilityBooking) -> Unit,
    onBookingAction: (FacilityBooking, String) -> Unit,
    onAction: (String) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(TODAY) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<FacilityBooking?>(null) }
    var dialogFacility by remember { mutableStateOf(facilities[0].name) }
    val myBookings = bookings.filter { it.unit == residentUnit }.sortedBy { it.date }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M3",
            title = "Facility booking",
            onBack = onBack,
            trailing = GlyphType.CALENDAR,
            onTrailing = { selectedDay = TODAY }
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
                    body = "Dots mark days that already have bookings. Approvals usually take less than one working day.",
                    glyph = GlyphType.CALENDAR,
                    badge = "Open"
                )
            }
            item {
                SectionTitle("Choose a date")
                Spacer(Modifier.height(10.dp))
                MonthCalendar(
                    selectedDay = selectedDay,
                    bookedCount = { day -> bookings.count { it.date == day && it.occupiesSlot() } },
                    onSelect = { selectedDay = it }
                )
            }
            if (role == UserRole.RESIDENT) {
                item {
                    SectionTitle("Facilities on ${dayLabel(selectedDay)}")
                }
                items(facilities) { info ->
                    val freeSlots = timeSlots.count { findBooking(bookings, info.name, selectedDay, it) == null }
                    FacilityRow(info, freeSlots) {
                        dialogFacility = info.name
                        showBookingDialog = true
                    }
                }
                item {
                    PrimaryActionButton("Book a facility") {
                        dialogFacility = facilities[0].name
                        showBookingDialog = true
                    }
                }
                item {
                    SectionTitle("My bookings")
                }
                items(myBookings) { booking ->
                    CondoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(booking.facility, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    booking.time,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(booking.status, statusTone(booking.status))
                        }
                        if (booking.occupiesSlot() && booking.date >= TODAY) {
                            TextButton(
                                onClick = { bookingToCancel = booking },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Cancel booking")
                            }
                        }
                    }
                }
                if (myBookings.isEmpty()) {
                    item { EmptyStateCard("No bookings yet", "Your facility requests will appear here.") }
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
            date = selectedDay,
            bookings = bookings,
            initialFacility = dialogFacility,
            residentUnit = residentUnit,
            onDismiss = { showBookingDialog = false },
            onSubmit = {
                onAddBooking(it)
                showBookingDialog = false
            }
        )
    }
    bookingToCancel?.let { target ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancel this booking?") },
            text = {
                Text("${target.facility}\n${target.time}\n\nThe time slot will be released for other residents.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBookingAction(target, "Cancelled")
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) { Text("Keep booking") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun MonthCalendar(
    selectedDay: Int,
    bookedCount: (Int) -> Int,
    onSelect: (Int) -> Unit
) {
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val cells: List<Int?> = List<Int?>(FIRST_DAY_OFFSET) { null } + (1..DAYS_IN_MONTH).toList()
    val weeks = cells.chunked(7)
    CondoCard {
        Text("November 2026", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { name ->
                Text(
                    name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (index in 0 until 7) {
                    val day = week.getOrNull(index)
                    if (day == null) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        CalendarCell(
                            day = day,
                            selected = day == selectedDay,
                            past = day < TODAY,
                            hasBookings = bookedCount(day) > 0,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(day) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(Gold, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                "Has bookings · Today is outlined · Past dates are unavailable",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarCell(
    day: Int,
    selected: Boolean,
    past: Boolean,
    hasBookings: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        past -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = when {
        !hasBookings -> Color.Transparent
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> Gold
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .then(
                if (day == TODAY && !selected) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = !past, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.toString(), style = MaterialTheme.typography.labelLarge, color = textColor)
            Spacer(Modifier.height(2.dp))
            Box(Modifier.size(5.dp).background(dotColor, CircleShape))
        }
    }
}

@Composable
private fun FacilityRow(info: FacilityInfo, freeSlots: Int, onClick: () -> Unit) {
    val (label, tone) = when {
        freeSlots == 0 -> "Full" to PillTone.CORAL
        freeSlots < timeSlots.size -> "Limited" to PillTone.GOLD
        else -> "Available" to PillTone.BLUE
    }
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = freeSlots > 0, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(info.background, RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(info.symbol, color = info.foreground, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(info.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (freeSlots == 0) "No slots left on this day" else "$freeSlots of ${timeSlots.size} slots free",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(label, tone)
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    note: String? = null,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
        if (note != null) {
            Text(
                note,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    2.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
                .padding(4.dp)
        ) {
            if (selected) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
            }
        }
    }
}

@Composable
private fun BookingDialog(
    date: Int,
    bookings: List<FacilityBooking>,
    initialFacility: String,
    residentUnit: String,
    onDismiss: () -> Unit,
    onSubmit: (FacilityBooking) -> Unit
) {
    var facility by remember { mutableStateOf(initialFacility) }
    var slot by remember { mutableStateOf<String?>(null) }
    FormDialog(title = "Request a booking", onDismiss = onDismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                "Booking for ${dayLabel(date)}. Management will review the request.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text("Facility", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            facilities.forEach { info ->
                OptionRow(label = info.name, selected = facility == info.name) {
                    facility = info.name
                    slot = null
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Time slot", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            timeSlots.forEach { option ->
                val holder = findBooking(bookings, facility, date, option)
                OptionRow(
                    label = option,
                    selected = slot == option,
                    enabled = holder == null,
                    note = when (holder?.status) {
                        null -> null
                        "Approved" -> "Booked"
                        else -> "Requested"
                    }
                ) {
                    slot = option
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryActionButton(text = "Send booking request", enabled = slot != null) {
                slot?.let { onSubmit(FacilityBooking(facility, residentUnit, date, it, "Pending")) }
            }
        }
    }
}