package com.example.condominium_maintenance_system.data.model

import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.ManagementBill
import com.example.condominium_maintenance_system.ui.VisitorRequest

object ManagementOverviewRules {
    fun pendingCount(
        visitors: List<VisitorRequest>,
        bookings: List<FacilityBooking>,
        maintenance: List<MaintenanceRequest>
    ): Int =
        visitors.count { it.status.equals("PENDING", true) } +
            bookings.count { it.status.equals("Pending", true) } +
            maintenance.count {
                MaintenanceRules.normalizeRequestStatus(it.status) == "PENDING"
            }

    fun completedCount(
        visitors: List<VisitorRequest>,
        bookings: List<FacilityBooking>,
        maintenance: List<MaintenanceRequest>,
        paidBills: List<ManagementBill>
    ): Int =
        visitors.count {
            it.status.equals("APPROVED", true) || it.status.equals("COMPLETE", true)
        } +
            bookings.count { it.status.equals("Approved", true) } +
            maintenance.count {
                MaintenanceRules.normalizeRequestStatus(it.status) == "COMPLETED"
            } +
            paidBills.size
}
