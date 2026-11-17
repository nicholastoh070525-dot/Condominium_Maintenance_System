package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.ManagementOverviewRules
import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.ManagementBill
import com.example.condominium_maintenance_system.ui.VisitorRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagementOverviewRulesTest {
    @Test
    fun pendingIncludesActionsFromAllThreeModules() {
        val count = ManagementOverviewRules.pendingCount(
            visitors = listOf(
                visitor("PENDING"),
                visitor("APPROVED")
            ),
            bookings = listOf(
                booking("Pending"),
                booking("Approved")
            ),
            maintenance = listOf(
                request("PENDING"),
                request("IN_PROGRESS"),
                request("COMPLETED")
            )
        )

        assertEquals(3, count)
    }

    @Test
    fun completedUsesCompletedOutcomesAndAllPaidBills() {
        val count = ManagementOverviewRules.completedCount(
            visitors = listOf(
                visitor("APPROVED"),
                visitor("COMPLETE"),
                visitor("REJECTED")
            ),
            bookings = listOf(booking("Approved"), booking("Pending")),
            maintenance = listOf(request("COMPLETED"), request("PENDING")),
            paidBills = listOf(paidBill("1"), paidBill("2"))
        )

        assertEquals(6, count)
    }

    private fun visitor(status: String) = VisitorRequest(
        name = "Visitor",
        plate = "ABC 123",
        visitTime = "2026-11-18T10:00:00Z",
        bay = "",
        status = status,
        id = status
    )

    private fun booking(status: String) = FacilityBooking(
        facility = "Function room",
        unit = "A-12-01",
        date = 18,
        slot = "10:00 AM",
        status = status,
        id = status
    )

    private fun request(status: String) = MaintenanceRequest(
        id = status,
        title = "Leak",
        category = "Plumbing",
        description = "Leak below sink",
        urgency = "Normal",
        status = status
    )

    private fun paidBill(id: String) = ManagementBill(
        id = id,
        unitId = "unit-$id",
        unitCode = "A-12-01",
        residentName = "Resident",
        billingMonth = "2026-11-01",
        amount = 285.0,
        dueDate = "2026-11-15",
        status = "PAID"
    )
}
