package com.example.condominium_maintenance_system.data.model

import com.example.condominium_maintenance_system.ui.BillableUnit
import com.example.condominium_maintenance_system.ui.BillingScope
import com.example.condominium_maintenance_system.ui.BillingScopeMode
import com.example.condominium_maintenance_system.ui.ManagementBill
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BillingRules {
    fun deletableBillsForMonth(
        bills: List<ManagementBill>,
        month: String
    ): List<ManagementBill> = bills.filter { bill ->
        bill.billingMonth == month && bill.status.equals("UNPAID", ignoreCase = true)
    }

    fun unitsForScope(units: List<BillableUnit>, scope: BillingScope): List<BillableUnit> =
        when (scope.mode) {
            BillingScopeMode.ALL -> units
            BillingScopeMode.BLOCK -> units.filter {
                it.block.equals(scope.block.orEmpty(), ignoreCase = true)
            }
            BillingScopeMode.FLOOR_RANGE -> {
                val start = scope.startFloor
                val end = scope.endFloor
                if (start == null || end == null) emptyList()
                else units.filter {
                    val blockMatches = scope.block.isNullOrBlank() ||
                        it.block.equals(scope.block, ignoreCase = true)
                    blockMatches && it.floor in minOf(start, end)..maxOf(start, end)
                }
            }
        }

    fun effectiveStatus(bill: ManagementBill, today: String): String {
        if (bill.status.equals("PAID", ignoreCase = true)) return "PAID"
        return if (bill.dueDate.isNotBlank() && bill.dueDate < today) "OVERDUE" else "DUE"
    }

    fun todayIso(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    fun parseDueDate(input: String): String? {
        val date = runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
                isLenient = false
            }.parse(input.trim())
        }.getOrNull() ?: return null
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    fun formatDueDateInput(input: String): String {
        val digits = input.filter(Char::isDigit).take(8)
        return buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }
    }

    fun monthDate(year: Int, month: Int): String =
        String.format(Locale.US, "%04d-%02d-01", year, month + 1)

    fun monthLabel(value: String): String {
        val calendar = Calendar.getInstance().apply {
            val parts = value.split("-")
            clear()
            set(
                parts.getOrNull(0)?.toIntOrNull() ?: 2000,
                (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1,
                1
            )
        }
        return SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time)
    }
}
