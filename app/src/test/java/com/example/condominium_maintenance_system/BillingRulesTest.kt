package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.ui.BillableUnit
import com.example.condominium_maintenance_system.ui.BillingScope
import com.example.condominium_maintenance_system.ui.BillingScopeMode
import com.example.condominium_maintenance_system.ui.ManagementBill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingRulesTest {
    private val units = listOf(
        BillableUnit("a11", "A", 11, 1, "A-11-01", "Aina"),
        BillableUnit("a15", "A", 15, 1, "A-15-01", "Daniel"),
        BillableUnit("b20", "B", 20, 1, "B-20-01", "Mei")
    )

    @Test
    fun floorRangeIncludesOnlyMatchingFloors() {
        val selected = BillingRules.unitsForScope(
            units,
            BillingScope(
                mode = BillingScopeMode.FLOOR_RANGE,
                block = "A",
                startFloor = 15,
                endFloor = 20
            )
        )

        assertEquals(listOf("A-15-01"), selected.map { it.unitCode })

        val blockB = BillingRules.unitsForScope(
            units,
            BillingScope(
                mode = BillingScopeMode.FLOOR_RANGE,
                block = "B",
                startFloor = 15,
                endFloor = 20
            )
        )
        assertEquals(listOf("B-20-01"), blockB.map { it.unitCode })
    }

    @Test
    fun incompleteFloorRangeSelectsNothing() {
        val selected = BillingRules.unitsForScope(
            units,
            BillingScope(mode = BillingScopeMode.FLOOR_RANGE, startFloor = 11)
        )

        assertTrue(selected.isEmpty())
    }

    @Test
    fun unpaidPastDueBillIsOverdueWhilePaidBillStaysPaid() {
        val unpaid = ManagementBill(
            id = "1",
            unitId = "a11",
            unitCode = "A-11-01",
            residentName = "Aina",
            billingMonth = "2026-11-01",
            amount = 285.0,
            dueDate = "2026-11-15",
            status = "UNPAID"
        )
        val paid = unpaid.copy(id = "2", dueDate = "2020-01-01", status = "PAID")

        assertEquals("OVERDUE", BillingRules.effectiveStatus(unpaid, "2026-11-18"))
        assertEquals("PAID", BillingRules.effectiveStatus(paid, "2026-11-18"))
    }

    @Test
    fun dueDateInputFormatsDigitsAsTyped() {
        assertEquals("15/11", BillingRules.formatDueDateInput("1511"))
        assertEquals("15/11/2026", BillingRules.formatDueDateInput("15112026"))
        assertEquals("15/11/2026", BillingRules.formatDueDateInput("15/11/202699"))
    }

    @Test
    fun billingMonthIsStoredAsFirstDayOfSelectedMonth() {
        assertEquals("2027-02-01", BillingRules.monthDate(2027, 1))
        assertEquals("February 2027", BillingRules.monthLabel("2027-02-01"))
    }

    @Test
    fun monthDeletionIncludesOnlyUnpaidBillsFromSelectedMonth() {
        val novemberUnpaid = ManagementBill(
            id = "1",
            unitId = "a11",
            unitCode = "A-11-01",
            residentName = "Aina",
            billingMonth = "2026-11-01",
            amount = 285.0,
            dueDate = "2026-11-15",
            status = "UNPAID"
        )
        val bills = listOf(
            novemberUnpaid,
            novemberUnpaid.copy(id = "2", unitId = "a12", status = "PAID"),
            novemberUnpaid.copy(id = "3", unitId = "b11", billingMonth = "2026-12-01")
        )

        val deletable = BillingRules.deletableBillsForMonth(bills, "2026-11-01")

        assertEquals(listOf("1"), deletable.map { it.id })
    }
}
