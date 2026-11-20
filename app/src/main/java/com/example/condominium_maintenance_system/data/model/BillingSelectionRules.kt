package com.example.condominium_maintenance_system.data.model

import com.example.condominium_maintenance_system.ui.MaintenanceFee

object BillingSelectionRules {
    fun outstandingBills(bills: List<MaintenanceFee>, today: String): List<MaintenanceFee> =
        bills
            .filter { !it.status.equals("PAID", ignoreCase = true) }
            .sortedWith(
                compareByDescending<MaintenanceFee> {
                    it.dueDate.isNotBlank() && it.dueDate < today
                }
                    .thenBy { it.dueDate }
                    .thenBy { it.month }
            )

    fun paidBills(bills: List<MaintenanceFee>): List<MaintenanceFee> =
        bills.filter { it.status.equals("PAID", ignoreCase = true) }

    fun initialSelection(outstanding: List<MaintenanceFee>): String? =
        outstanding.singleOrNull()?.id
}
