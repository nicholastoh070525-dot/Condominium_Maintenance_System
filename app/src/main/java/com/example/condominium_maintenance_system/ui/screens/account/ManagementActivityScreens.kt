package com.example.condominium_maintenance_system.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.ManagementBill
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.VisitorRequest
import com.example.condominium_maintenance_system.ui.components.Avatar
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.Success
import java.text.NumberFormat
import java.util.Locale

@Composable
fun UserDirectoryScreen(
    profiles: List<UserProfile>,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Users", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("All CondoTrack users", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.size(5.dp))
                Text(
                    "Profiles and residential units across management and resident accounts.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (profiles.isEmpty()) {
                item { EmptyStateCard("No users found", "Registered profiles will appear here.") }
            } else {
                items(profiles, key = { it.id }) { profile ->
                    UserDirectoryRow(profile)
                }
            }
        }
    }
}

@Composable
fun PendingActionsScreen(
    visitors: List<VisitorRequest>,
    bookings: List<FacilityBooking>,
    maintenanceRequests: List<MaintenanceRequest>,
    visitorProcessingId: String?,
    maintenanceProcessingId: String?,
    bookingProcessingId: String?,
    onBack: () -> Unit,
    onVisitorDecision: (VisitorRequest, String) -> Unit,
    onBookingDecision: (FacilityBooking, String) -> Unit,
    onReviewMaintenance: (MaintenanceRequest) -> Unit
) {
    val pendingVisitors = visitors.filter { it.status.equals("PENDING", true) }
    val pendingBookings = bookings.filter { it.status.equals("Pending", true) }
    val pendingMaintenance = maintenanceRequests.filter {
        MaintenanceRules.normalizeRequestStatus(it.status) == "PENDING"
    }
    val total = pendingVisitors.size + pendingBookings.size + pendingMaintenance.size

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Pending", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Actions for management", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.size(5.dp))
                Text(
                    "$total items need a decision or assignment across visitors, bookings and maintenance.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (total == 0) {
                item {
                    EmptyStateCard("Nothing pending", "New approvals and unassigned work will appear here.")
                }
            }
            if (pendingVisitors.isNotEmpty()) {
                item { SectionTitle("Visitor approvals") }
                items(pendingVisitors, key = { "pending-visitor-${it.id}" }) { visitor ->
                    PendingVisitorRow(
                        visitor = visitor,
                        processing = visitorProcessingId == visitor.id,
                        onApprove = { onVisitorDecision(visitor, "APPROVED") },
                        onReject = { onVisitorDecision(visitor, "REJECTED") }
                    )
                }
            }
            if (pendingBookings.isNotEmpty()) {
                item { SectionTitle("Booking approvals") }
                items(pendingBookings, key = { "pending-booking-${it.id}" }) { booking ->
                    PendingBookingRow(
                        booking = booking,
                        processing = bookingProcessingId == booking.id,
                        onApprove = { onBookingDecision(booking, "Approved") },
                        onReject = { onBookingDecision(booking, "Rejected") }
                    )
                }
            }
            if (pendingMaintenance.isNotEmpty()) {
                item { SectionTitle("Unassigned maintenance") }
                items(pendingMaintenance, key = { "pending-work-${it.id}" }) { request ->
                    PendingMaintenanceRow(
                        request = request,
                        processing = maintenanceProcessingId == request.id,
                        onReview = { onReviewMaintenance(request) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovedOutcomesScreen(
    visitors: List<VisitorRequest>,
    bookings: List<FacilityBooking>,
    maintenanceRequests: List<MaintenanceRequest>,
    bills: List<ManagementBill>,
    onBack: () -> Unit
) {
    val completedVisitors = visitors.filter {
        it.status.equals("APPROVED", true) || it.status.equals("COMPLETE", true)
    }
    val approvedBookings = bookings.filter { it.status.equals("Approved", true) }
    val completedMaintenance = maintenanceRequests.filter {
        MaintenanceRules.normalizeRequestStatus(it.status) == "COMPLETED"
    }
    val paidBills = bills.filter { it.status.equals("PAID", true) }
    val total = completedVisitors.size + approvedBookings.size + completedMaintenance.size + paidBills.size

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Approved", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Completed outcomes", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.size(5.dp))
                Text(
                    "$total approved, completed or paid outcomes across the condominium.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (total == 0) {
                item {
                    EmptyStateCard("No completed outcomes yet", "Completed management work will appear here.")
                }
            }
            if (completedVisitors.isNotEmpty()) {
                item { SectionTitle("Visitors") }
                items(completedVisitors, key = { "done-visitor-${it.id}" }) { visitor ->
                    OutcomeRow(
                        title = visitor.name,
                        detail = "${visitor.unitCode} · ${visitor.visitorTimeLabel()}",
                        value = if (visitor.status == "COMPLETE") "Completed" else "Approved",
                        highlight = true
                    )
                }
            }
            if (approvedBookings.isNotEmpty()) {
                item { SectionTitle("Bookings") }
                items(approvedBookings, key = { "done-booking-${it.id}" }) { booking ->
                    OutcomeRow(
                        title = booking.facility,
                        detail = "${booking.unit} · ${booking.time}",
                        value = "Approved",
                        highlight = true
                    )
                }
            }
            if (completedMaintenance.isNotEmpty()) {
                item { SectionTitle("Maintenance") }
                items(completedMaintenance, key = { "done-work-${it.id}" }) { request ->
                    OutcomeRow(
                        title = request.title,
                        detail = "${request.unitCode} · ${request.technicianName.orEmpty()}",
                        value = "Completed",
                        highlight = true
                    )
                }
            }
            if (paidBills.isNotEmpty()) {
                item { SectionTitle("Bills") }
                items(paidBills, key = { "paid-bill-${it.id}" }) { bill ->
                    OutcomeRow(
                        title = bill.unitCode,
                        detail = bill.residentName.ifBlank { "Resident not linked" },
                        value = money(bill.amount),
                        highlight = false
                    )
                }
            }
        }
    }
}

@Composable
private fun UserDirectoryRow(profile: UserProfile) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initials = profile.avatarKey)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.fullName, style = MaterialTheme.typography.labelLarge)
                Text(
                    profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(profile.role.displayName, style = MaterialTheme.typography.labelMedium)
                Text(
                    profile.unitCode ?: "No unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PendingVisitorRow(
    visitor: VisitorRequest,
    processing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    CondoCard {
        Text(visitor.name, style = MaterialTheme.typography.labelLarge)
        Text(
            "${visitor.unitCode} · ${visitor.visitorTimeLabel()} · ${visitor.plate}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onReject, enabled = !processing) { Text("Reject") }
            TextButton(onClick = onApprove, enabled = !processing) { Text("Approve") }
        }
    }
}

@Composable
private fun PendingBookingRow(
    booking: FacilityBooking,
    processing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    CondoCard {
        Text(booking.facility, style = MaterialTheme.typography.labelLarge)
        Text(
            "${booking.residentName.ifBlank { booking.unit }} · ${booking.time}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onReject, enabled = !processing) {
                Text(if (processing) "Updating…" else "Reject")
            }
            TextButton(onClick = onApprove, enabled = !processing) { Text("Approve") }
        }
    }
}

@Composable
private fun PendingMaintenanceRow(
    request: MaintenanceRequest,
    processing: Boolean,
    onReview: () -> Unit
) {
    CondoCard {
        Text(request.title, style = MaterialTheme.typography.labelLarge)
        Text(
            "${request.unitCode} · ${request.category} · ${request.urgency}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onReview, enabled = !processing) {
                Text(if (processing) "Loading…" else "Review work")
            }
        }
    }
}

@Composable
private fun OutcomeRow(
    title: String,
    detail: String,
    value: String,
    highlight: Boolean
) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(10.dp)
                    )
            ) {}
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlight) Success else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun VisitorRequest.visitorTimeLabel(): String =
    visitTime.replace("T", " ").take(16)

private fun money(amount: Double): String =
    "RM " + NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
