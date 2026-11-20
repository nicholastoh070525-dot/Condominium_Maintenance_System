package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.BillingSelectionRules
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BillingSelectionRulesTest {

    private fun fee(
        id: String,
        month: String = "2026-11-01",
        dueDate: String = "2026-11-15",
        status: String = "UNPAID"
    ) = MaintenanceFee(
        id = id,
        month = month,
        amount = "RM 285.00",
        dueDate = dueDate,
        status = status
    )

    @Test
    fun outstandingBillsExcludesPaidBills() {
        val bills = listOf(
            fee("1", status = "UNPAID"),
            fee("2", status = "PAID"),
            fee("3", status = "UNPAID")
        )

        assertEquals(
            listOf("1", "3"),
            BillingSelectionRules.outstandingBills(bills, "2026-11-10").map { it.id }
        )
    }

    @Test
    fun outstandingBillsOrdersOverdueFirstThenDueDateThenMonth() {
        val bills = listOf(
            fee("late", month = "2026-12-01", dueDate = "2026-12-05"),
            fee("oct-overdue", month = "2026-10-01", dueDate = "2026-10-05"),
            fee("sep-overdue", month = "2026-09-01", dueDate = "2026-09-05"),
            fee("early", month = "2026-11-01", dueDate = "2026-11-15"),
            fee("tie-older", month = "2026-10-01", dueDate = "2026-11-20"),
            fee("tie-newer", month = "2026-11-01", dueDate = "2026-11-20")
        )

        assertEquals(
            listOf("sep-overdue", "oct-overdue", "early", "tie-older", "tie-newer", "late"),
            BillingSelectionRules.outstandingBills(bills, "2026-11-10").map { it.id }
        )
    }

    @Test
    fun paidBillsReturnsOnlyPaidBills() {
        val bills = listOf(
            fee("1", status = "UNPAID"),
            fee("2", status = "PAID"),
            fee("3", status = "PAID")
        )

        assertEquals(
            listOf("2", "3"),
            BillingSelectionRules.paidBills(bills).map { it.id }
        )
    }

    @Test
    fun initialSelectionPicksOnlyWhenExactlyOneOutstanding() {
        assertNull(BillingSelectionRules.initialSelection(emptyList()))
        assertEquals("only", BillingSelectionRules.initialSelection(listOf(fee("only"))))
        assertNull(BillingSelectionRules.initialSelection(listOf(fee("a"), fee("b"))))
    }
}
