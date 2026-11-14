package com.example.condominium_maintenance_system.ui

import android.net.Uri
import com.example.condominium_maintenance_system.data.model.UnitRules

enum class UserRole(val displayName: String, val initials: String) {
    RESIDENT("Resident", "AM"),
    MANAGEMENT("Management", "NS");

    companion object {
        fun fromDatabase(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: error("Unsupported CondoTrack role: $value")
    }
}

enum class AppDestination {
    HOME,
    ACCESS,
    MAINTENANCE,
    BOOKINGS,
    LOST_FOUND,
    ANNOUNCEMENTS,
    BILLING,
    NEW_REQUEST,
    ADD_VISITOR,
    SETTINGS,
    EDIT_PROFILE
}

data class NavItem(
    val destination: AppDestination,
    val label: String
)

data class VisitorRequest(
    val name: String,
    val plate: String,
    val visitTime: String,
    val bay: String,
    val status: String,
    val id: String = "",
    val vehicleColor: String = "Silver",
    val note: String = "",
    val unitCode: String = "",
    val residentName: String = "",
    val approvedAt: String? = null,
    val completedAt: String? = null,
    val passReference: String? = null
)

data class MaintenanceRequest(
    val id: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val unitCode: String = "",
    val title: String,
    val category: String,
    val description: String,
    val urgency: String,
    val status: String,
    val photoUrls: List<String> = emptyList(),
    val technicianName: String? = null,
    val scheduledAt: String? = null,
    val report: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val reference: String
        get() = if (id.isBlank()) "MNT-NEW" else "MNT-${id.take(8).uppercase()}"

    val detail: String
        get() = "$category · $urgency"
}

data class MaintenanceRequestDraft(
    val title: String,
    val category: String,
    val description: String,
    val urgency: String,
    val photoUris: List<Uri> = emptyList()
)

data class MaintenanceRequestUpdate(
    val status: String,
    val technicianName: String?,
    val scheduledAt: String?,
    val report: String?
)

data class MaintenanceTask(
    val id: String = "",
    val title: String,
    val requestId: String? = null,
    val unit: String,
    val assigneeId: String,
    val assigneeName: String,
    val priority: String,
    val scheduledAt: String,
    val status: String,
    val notes: String? = null,
    val report: String? = null,
    val createdAt: String? = null
)

data class MaintenanceTaskDraft(
    val title: String,
    val unit: String,
    val priority: String,
    val scheduledAt: String,
    val notes: String? = null
)

data class FacilityBooking(
    val facility: String,
    val unit: String,
    val date: Int,
    val slot: String,
    val status: String,
    val id: String = "",
    val residentName: String = ""
) {
    val time: String
        get() = "${dayLabel(date)} · $slot"
}

fun dayLabel(day: Int): String {
    val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return "${names[(day - 1) % 7]}, $day Nov"
}

fun FacilityBooking.occupiesSlot(): Boolean =
    status == "Pending" || status == "Approved"

data class LostFoundItem(
    val title: String,
    val detail: String,
    val status: String,
    val kind: String
)

data class CommunityNotice(
    val title: String,
    val body: String,
    val time: String,
    val pinned: Boolean = false,
    val read: Boolean = false
)

data class MaintenanceFee(
    val id: String = "",
    val month: String,
    val amount: String,
    val dueDate: String,
    val paidDate: String? = null,
    val status: String = if (paidDate == null) "UNPAID" else "PAID"
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val unitId: String? = null,
    val unitCode: String? = null,
    val avatarKey: String = "AM",
    val avatarUrl: String? = null
)

data class PaymentRecord(
    val id: String,
    val amount: String,
    val method: String,
    val paidAt: String,
    val receiptNumber: String
)

data class RegistrationDraft(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: UserRole,
    val block: String? = null,
    val floor: Int? = null,
    val room: Int? = null
) {
    val unitCode: String?
        get() = if (block != null && floor != null && room != null) {
            if (UnitRules.isValid(block, floor, room)) UnitRules.code(block, floor, room) else null
        } else {
            null
        }
}

data class ProfileUpdateDraft(
    val fullName: String,
    val phone: String,
    val email: String,
    val avatarKey: String,
    val avatarUrl: String?,
    val block: String? = null,
    val floor: Int? = null,
    val room: Int? = null
)

data class UnitAvailability(
    val id: String,
    val block: String,
    val floor: Int,
    val room: Int,
    val unitCode: String
)
