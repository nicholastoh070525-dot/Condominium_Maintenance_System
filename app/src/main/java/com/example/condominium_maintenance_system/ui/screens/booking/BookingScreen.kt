package com.example.condominium_maintenance_system.ui.screens.booking

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.condominium_maintenance_system.ui.*
import com.example.condominium_maintenance_system.ui.components.*
import com.example.condominium_maintenance_system.ui.screens.common.*
import com.example.condominium_maintenance_system.ui.theme.*
import java.util.Calendar

private const val DAYS_IN_MONTH = 30
private const val FIRST_DAY_OFFSET = 6 // 1 Nov 2026 is a Sunday, the grid starts on Monday
private const val CALENDAR_YEAR = 2026

// Facility opening hours and booking rules
private const val OPEN_MINUTE = 8 * 60            // 8:00 AM
private const val CLOSE_MINUTE = 22 * 60          // 10:00 PM
private const val STEP_MINUTES = 30               // times can be picked every 30 minutes
private const val MAX_BOOKING_MINUTES = 4 * 60    // one booking can be at most 4 hours

// Timetable sizes
private val CELL_WIDTH = 30.dp                    // width of one 30-minute cell
private val ROW_HEIGHT = 48.dp
private val HEADER_HEIGHT = 36.dp
private val NAME_COLUMN_WIDTH = 108.dp

// The calendar shows November 2026, so this returns today's day of that month.
// Before November 2026 -> 1 (no past dates), after November 2026 -> beyond the last day (all dates are past).
private fun todayInCalendar(): Int {
    val now = Calendar.getInstance()
    val year = now.get(Calendar.YEAR)
    val month = now.get(Calendar.MONTH)
    return when {
        year < CALENDAR_YEAR || (year == CALENDAR_YEAR && month < Calendar.NOVEMBER) -> 1
        year == CALENDAR_YEAR && month == Calendar.NOVEMBER -> now.get(Calendar.DAY_OF_MONTH)
        else -> DAYS_IN_MONTH + 1
    }
}

private val facilityPalettes = listOf(
    BluePale to BluePrimary,
    PurplePale to Purple,
    GoldPale to Gold,
    Color(0xFFE8EDFF) to Color(0xFF3E5FBF),
    SuccessPale to Success,
    CoralPale to Coral
)

private val symbolOptions = listOf("≈", "✧", "♨", "⚑", "✦", "★")

// The same facility name always gets the same colours
private fun paletteFor(name: String): Pair<Color, Color> =
    facilityPalettes[(name.hashCode() and Int.MAX_VALUE) % facilityPalettes.size]

private fun nowMinutes(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
}

private fun roundUpToStep(minutes: Int): Int =
    ((minutes + STEP_MINUTES - 1) / STEP_MINUTES) * STEP_MINUTES

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest min"
        rest == 0 -> "$hours h"
        else -> "$hours h $rest min"
    }
}

// How many minutes of the opening hours are still free for one facility on one day
private fun freeMinutesFor(bookings: List<FacilityBooking>, facility: String, day: Int): Int {
    val bookedCells = (OPEN_MINUTE until CLOSE_MINUTE step STEP_MINUTES).count { cell ->
        bookings.any { it.overlaps(facility, day, cell, cell + STEP_MINUTES) }
    }
    return (CLOSE_MINUTE - OPEN_MINUTE) - bookedCells * STEP_MINUTES
}

// Block colour and text colour in the timetable, by booking status
private fun blockColors(status: String): Pair<Color, Color> = when (status) {
    "Pending" -> Gold to Color(0xFF5A3B0C)
    else -> BluePrimary to Color.White
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
    residentUnit: String,
    bookings: List<FacilityBooking>,
    facilities: List<Facility>,
    onBack: () -> Unit,
    onAddBooking: (FacilityBooking) -> Unit,
    onBookingAction: (FacilityBooking, String) -> Unit,
    onAddFacility: (String, String) -> Unit,
    onDeleteFacility: (Facility) -> Unit,
    onAction: (String) -> Unit
) {
    val today = todayInCalendar()
    var selectedDay by remember { mutableIntStateOf(today.coerceIn(1, DAYS_IN_MONTH)) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<FacilityBooking?>(null) }
    var dialogFacility by remember { mutableStateOf("") }
    var showAddFacility by remember { mutableStateOf(false) }
    var facilityToDelete by remember { mutableStateOf<Facility?>(null) }
    var showTimetable by remember { mutableStateOf(false) }
    val myBookings = bookings.filter { it.unit == residentUnit }.sortedBy { it.date }

    fun openBooking(facilityName: String) {
        if (selectedDay < today) {
            onAction("That date has already passed. Please choose another date.")
        } else {
            dialogFacility = facilityName
            showBookingDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Facility booking",
            onBack = onBack,
            trailing = GlyphType.CALENDAR,
            onTrailing = { selectedDay = today.coerceIn(1, DAYS_IN_MONTH) }
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
                SectionTitle("Choose a date")
                Spacer(Modifier.height(10.dp))
                MonthCalendar(
                    selectedDay = selectedDay,
                    today = today,
                    bookedCount = { day -> bookings.count { it.date == day && it.occupiesSlot() } },
                    onSelect = { selectedDay = it }
                )
            }
            item {
                TimetableEntryCard(onClick = { showTimetable = true })
            }
            if (role == UserRole.RESIDENT) {
                item {
                    SectionTitle("Facilities on ${dayLabel(selectedDay)}")
                }
                items(facilities) { info ->
                    val freeMinutes = freeMinutesFor(bookings, info.name, selectedDay)
                    FacilityRow(info, freeMinutes) { openBooking(info.name) }
                }
                if (facilities.isEmpty()) {
                    item { EmptyStateCard("No facilities yet", "Management has not added any facilities.") }
                }
                item {
                    PrimaryActionButton("Book a facility", enabled = facilities.isNotEmpty()) {
                        openBooking(facilities.firstOrNull()?.name ?: "")
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
                        if (booking.occupiesSlot() && booking.date >= today) {
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
                    SectionTitle("Manage facilities", action = "Add facility") { showAddFacility = true }
                }
                items(facilities) { facility ->
                    ManageFacilityRow(facility) { facilityToDelete = facility }
                }
                if (facilities.isEmpty()) {
                    item { EmptyStateCard("No facilities yet", "Tap Add facility to create the first one.") }
                }
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
            facilities = facilities,
            residentUnit = residentUnit,
            minStart = if (selectedDay == today) roundUpToStep(nowMinutes()) else 0,
            onDismiss = { showBookingDialog = false },
            onSubmit = {
                onAddBooking(it)
                showBookingDialog = false
            }
        )
    }
    if (showTimetable) {
        TimetableDialog(
            day = selectedDay,
            today = today,
            facilities = facilities,
            bookings = bookings,
            role = role,
            residentUnit = residentUnit,
            onDayChange = { selectedDay = it.coerceIn(1, DAYS_IN_MONTH) },
            onDismiss = { showTimetable = false }
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
    if (showAddFacility) {
        AddFacilityDialog(
            onDismiss = { showAddFacility = false },
            onSubmit = { name, symbol ->
                onAddFacility(name, symbol)
                showAddFacility = false
            }
        )
    }
    facilityToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { facilityToDelete = null },
            title = { Text("Delete this facility?") },
            text = {
                Text("${target.name} will be removed for all residents. A facility with pending or approved bookings cannot be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFacility(target)
                        facilityToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { facilityToDelete = null }) { Text("Keep facility") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun MonthCalendar(
    selectedDay: Int,
    today: Int,
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
                            past = day < today,
                            isToday = day == today,
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
    isToday: Boolean,
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
                if (isToday && !selected) {
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
private fun FacilityRow(info: Facility, freeMinutes: Int, onClick: () -> Unit) {
    val total = CLOSE_MINUTE - OPEN_MINUTE
    val (background, foreground) = paletteFor(info.name)
    val (label, tone) = when {
        freeMinutes <= 0 -> "Full" to PillTone.CORAL
        freeMinutes < total -> "Limited" to PillTone.GOLD
        else -> "Available" to PillTone.BLUE
    }
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = freeMinutes > 0, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(background, RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(info.symbol, color = foreground, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(info.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (freeMinutes <= 0) "Fully booked on this day" else "${formatDuration(freeMinutes)} free of ${formatDuration(total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(label, tone)
        }
    }
}

@Composable
private fun ManageFacilityRow(facility: Facility, onDelete: () -> Unit) {
    val (background, foreground) = paletteFor(facility.name)
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(background, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(facility.symbol, color = foreground, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                facility.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun AddFacilityDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf(symbolOptions.first()) }
    FormDialog(title = "Add a facility", onDismiss = onDismiss) {
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 30) name = it },
            label = { Text("Facility name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Icon", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            symbolOptions.forEach { option ->
                val selected = option == symbol
                val shape = RoundedCornerShape(12.dp)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(shape)
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape
                        )
                        .clickable { symbol = option },
                    contentAlignment = Alignment.Center
                ) {
                    Text(option, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryActionButton(text = "Add facility", enabled = name.isNotBlank()) {
            onSubmit(name.trim(), symbol)
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
private fun TimetableEntryCard(onClick: () -> Unit) {
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.CALENDAR, MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Booking timetable", style = MaterialTheme.typography.titleMedium)
                Text(
                    "See which facilities are booked, and when, on the selected date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TimetableDialog(
    day: Int,
    today: Int,
    facilities: List<Facility>,
    bookings: List<FacilityBooking>,
    role: UserRole,
    residentUnit: String,
    onDayChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val totalCells = (CLOSE_MINUTE - OPEN_MINUTE) / STEP_MINUTES
    val gridWidth = CELL_WIDTH * totalCells
    val hours = (OPEN_MINUTE / 60) until (CLOSE_MINUTE / 60)
    val horizontalState = rememberScrollState()
    val verticalState = rememberScrollState()
    val surface = MaterialTheme.colorScheme.surface
    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Booking timetable", style = MaterialTheme.typography.headlineMedium)
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable(onClick = onDismiss),
                        shape = RoundedCornerShape(15.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Glyph(GlyphType.CLOSE, MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayArrow("‹", enabled = day > 1) { onDayChange(day - 1) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayLabel(day), style = MaterialTheme.typography.titleMedium)
                        if (day == today) {
                            Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    DayArrow("›", enabled = day < DAYS_IN_MONTH) { onDayChange(day + 1) }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(BluePrimary, "Approved")
                    LegendItem(Gold, "Pending")
                    LegendItem(Color.Transparent, "Free", outlined = true)
                }
                if (role == UserRole.RESIDENT) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your own bookings have a dark outline.",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (facilities.isEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        EmptyStateCard("No facilities yet", "Facilities added by management will appear here.")
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).verticalScroll(verticalState)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.width(NAME_COLUMN_WIDTH)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(HEADER_HEIGHT)
                                        .background(surface)
                                        .padding(start = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        "Facility",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                facilities.forEach { facility ->
                                    val symbolColor = paletteFor(facility.name).second
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ROW_HEIGHT)
                                            .background(surface)
                                            .drawBehind {
                                                drawLine(
                                                    lineColor,
                                                    Offset(0f, size.height),
                                                    Offset(size.width, size.height),
                                                    strokeWidth = 1f
                                                )
                                            }
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(facility.symbol, color = symbolColor, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            facility.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f).horizontalScroll(horizontalState)) {
                                Row(
                                    modifier = Modifier
                                        .width(gridWidth)
                                        .height(HEADER_HEIGHT)
                                        .background(surface)
                                ) {
                                    hours.forEach { hour ->
                                        Box(
                                            modifier = Modifier
                                                .width(CELL_WIDTH * 2)
                                                .fillMaxHeight()
                                                .drawBehind {
                                                    drawLine(lineColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1f)
                                                },
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                hour.toString().padStart(2, '0') + ":00",
                                                modifier = Modifier.padding(start = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                facilities.forEach { facility ->
                                    TimetableRow(
                                        bookings = bookings.filter {
                                            it.facility == facility.name && it.date == day && it.occupiesSlot()
                                        },
                                        role = role,
                                        residentUnit = residentUnit,
                                        gridWidth = gridWidth,
                                        totalCells = totalCells
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableRow(
    bookings: List<FacilityBooking>,
    role: UserRole,
    residentUnit: String,
    gridWidth: Dp,
    totalCells: Int
) {
    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val hourLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .width(gridWidth)
            .height(ROW_HEIGHT)
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                val cell = CELL_WIDTH.toPx()
                for (i in 0..totalCells) {
                    drawLine(
                        if (i % 2 == 0) hourLineColor else lineColor,
                        Offset(i * cell, 0f),
                        Offset(i * cell, size.height),
                        strokeWidth = 1f
                    )
                }
                drawLine(hourLineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)
            }
    ) {
        bookings.forEach { booking ->
            val start = booking.startMinute.coerceAtLeast(OPEN_MINUTE)
            val end = booking.endMinute.coerceAtMost(CLOSE_MINUTE)
            if (end > start) {
                val (blockColor, textColor) = blockColors(booking.status)
                val mine = booking.unit == residentUnit
                val shape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .offset(x = CELL_WIDTH * ((start - OPEN_MINUTE) / STEP_MINUTES.toFloat()))
                        .width(CELL_WIDTH * ((end - start) / STEP_MINUTES.toFloat()))
                        .fillMaxHeight()
                        .padding(2.dp)
                        .background(blockColor, shape)
                        .then(
                            if (mine && role == UserRole.RESIDENT) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, shape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        when {
                            role == UserRole.MANAGEMENT -> booking.unit
                            mine -> "You"
                            else -> "Booked"
                        },
                        modifier = Modifier.padding(horizontal = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DayArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                symbol,
                style = MaterialTheme.typography.titleLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, outlined: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(4.dp))
                .then(
                    if (outlined) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    } else {
                        Modifier
                    }
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimeDropdown(
    label: String,
    selected: Int?,
    options: List<Int>,
    enabled: Boolean = true,
    isAvailable: (Int) -> Boolean,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        selected?.let { formatMinutes(it) } ?: "Select",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
                Text("▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                val available = isAvailable(option)
                DropdownMenuItem(
                    text = { Text(formatMinutes(option) + if (available) "" else "  ·  Booked") },
                    enabled = available,
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BookingDialog(
    date: Int,
    bookings: List<FacilityBooking>,
    facilities: List<Facility>,
    initialFacility: String,
    residentUnit: String,
    minStart: Int,
    onDismiss: () -> Unit,
    onSubmit: (FacilityBooking) -> Unit
) {
    var facility by remember { mutableStateOf(initialFacility) }
    var start by remember { mutableStateOf<Int?>(null) }
    var end by remember { mutableStateOf<Int?>(null) }

    fun isTaken(from: Int, to: Int): Boolean =
        bookings.any { it.overlaps(facility, date, from, to) }

    val startOptions = (OPEN_MINUTE until CLOSE_MINUTE step STEP_MINUTES).filter { it >= minStart }
    val endOptions = start?.let { s ->
        ((s + STEP_MINUTES)..minOf(s + MAX_BOOKING_MINUTES, CLOSE_MINUTE) step STEP_MINUTES).toList()
    } ?: emptyList()

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
                    start = null
                    end = null
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Time", style = MaterialTheme.typography.labelLarge)
            Text(
                "Open ${formatMinutes(OPEN_MINUTE)} – ${formatMinutes(CLOSE_MINUTE)}, up to ${MAX_BOOKING_MINUTES / 60} hours per booking.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            if (startOptions.isEmpty()) {
                Text(
                    "There is no time left on this date. Please choose another date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                TimeDropdown(
                    label = "Start time",
                    selected = start,
                    options = startOptions,
                    isAvailable = { !isTaken(it, it + STEP_MINUTES) }
                ) { chosen ->
                    start = chosen
                    end = null
                }
                Spacer(Modifier.height(8.dp))
                TimeDropdown(
                    label = "End time",
                    selected = end,
                    options = endOptions,
                    enabled = start != null,
                    isAvailable = { option -> start?.let { s -> !isTaken(s, option) } ?: false }
                ) { chosen ->
                    end = chosen
                }
            }
            val startValue = start
            val endValue = end
            if (startValue != null && endValue != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "${timeRangeLabel(startValue, endValue)} · ${formatDuration(endValue - startValue)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryActionButton(
                text = "Send booking request",
                enabled = startValue != null && endValue != null
            ) {
                if (startValue != null && endValue != null) {
                    onSubmit(
                        FacilityBooking(
                            facility = facility,
                            unit = residentUnit,
                            date = date,
                            slot = timeRangeLabel(startValue, endValue),
                            status = "Pending",
                            startMinute = startValue,
                            endMinute = endValue
                        )
                    )
                }
            }
        }
    }
}
