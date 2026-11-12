package com.example.condominium_maintenance_system.ui

import com.example.condominium_maintenance_system.data.model.UnitRules

enum class UserRole(val displayName: String, val initials: String) {
    RESIDENT("Resident", "AM"),
    MANAGEMENT("Management", "NS");

    companion object {
        fun fromDatabase(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RESIDENT
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
    NEW_REQUEST
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
    val title: String,
    val reference: String,
    val detail: String,
    val status: String,
    val time: String
)

data class WorkOrder(
    val reference: String,
    val title: String,
    val unit: String,
    val technician: String,
    val due: String,
    val priority: String
)

data class FacilityBooking(
    val facility: String,
    val unit: String,
    val date: Int,
    val slot: String,
    val status: String
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

data class UnitAvailability(
    val id: String,
    val block: String,
    val floor: Int,
    val room: Int,
    val unitCode: String
)
