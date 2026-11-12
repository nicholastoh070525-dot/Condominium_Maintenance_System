package com.example.condominium_maintenance_system.ui

enum class UserRole(val displayName: String, val initials: String) {
    RESIDENT("Resident", "AM"),
    MANAGEMENT("Management", "NS")
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
    val status: String
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
    val month: String,
    val amount: String,
    val dueDate: String,
    val paidDate: String? = null
)
