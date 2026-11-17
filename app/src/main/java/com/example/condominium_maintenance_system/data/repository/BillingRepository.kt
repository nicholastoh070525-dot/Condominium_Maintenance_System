package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.model.BillRow
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.data.model.NewBillPayload
import com.example.condominium_maintenance_system.data.model.NewPaymentPayload
import com.example.condominium_maintenance_system.data.model.PaymentRow
import com.example.condominium_maintenance_system.data.model.ProfileRow
import com.example.condominium_maintenance_system.data.model.UnitRow
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.BillIssueResult
import com.example.condominium_maintenance_system.ui.BillableUnit
import com.example.condominium_maintenance_system.ui.BillingScope
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.ManagementBill
import com.example.condominium_maintenance_system.ui.ManagementBillingSnapshot
import com.example.condominium_maintenance_system.ui.PaymentRecord
import io.github.jan.supabase.postgrest.from
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class BillingRepository {
    private val client
        get() = SupabaseProvider.client

    suspend fun getBills(unitId: String): List<MaintenanceFee> =
        client.from("condotrack_bills")
            .select {
                filter { eq("unit_id", unitId) }
            }
            .decodeList<BillRow>()
            .map { it.toDomain() }
            .sortedByDescending { it.month }

    suspend fun getPayments(unitId: String): List<PaymentRecord> =
        client.from("condotrack_payments")
            .select {
                filter { eq("unit_id", unitId) }
            }
            .decodeList<PaymentRow>()
            .map {
                PaymentRecord(
                    id = it.id,
                    amount = money(it.amount),
                    method = it.method,
                    paidAt = it.paidAt,
                    receiptNumber = it.receiptNumber
                )
            }

    suspend fun getManagementSnapshot(month: String): ManagementBillingSnapshot {
        val units = getBillableUnits()
        val allBills = client.from("condotrack_bills")
            .select()
            .decodeList<BillRow>()
        val mappedBills = toManagementBills(allBills, units.associateBy { it.id })
        return ManagementBillingSnapshot(
            bills = mappedBills.filter { it.billingMonth == month },
            units = units,
            paidBills = mappedBills.filter { it.status.equals("PAID", true) }
        )
    }

    private fun toManagementBills(
        bills: List<BillRow>,
        units: Map<String, BillableUnit>
    ): List<ManagementBill> =
        bills
            .map { bill ->
                val unit = units[bill.unitId]
                ManagementBill(
                    id = bill.id,
                    unitId = bill.unitId,
                    unitCode = unit?.unitCode ?: bill.unitId.take(8),
                    residentName = unit?.residentName.orEmpty(),
                    billingMonth = bill.billingMonth,
                    amount = bill.amount,
                    dueDate = bill.dueDate,
                    status = bill.status,
                    paidAt = bill.paidAt
                )
            }
            .sortedWith(compareByDescending<ManagementBill> { it.billingMonth }.thenBy { it.unitCode })

    suspend fun getBillableUnits(): List<BillableUnit> {
        val profiles = client.from("condotrack_profiles")
            .select()
            .decodeList<ProfileRow>()
            .filter { it.unitId != null }
            .associateBy { it.unitId }
        return client.from("condotrack_units")
            .select()
            .decodeList<UnitRow>()
            .map { unit ->
                BillableUnit(
                    id = unit.id,
                    block = unit.block,
                    floor = unit.floorNumber,
                    room = unit.roomNumber,
                    unitCode = unit.unitCode,
                    residentName = profiles[unit.id]?.fullName.orEmpty()
                )
            }
            .sortedWith(compareBy({ it.block }, { it.floor }, { it.room }))
    }

    suspend fun issueBills(
        month: String,
        amount: Double,
        dueDate: String,
        scope: BillingScope
    ): BillIssueResult {
        require(month.matches(Regex("\\d{4}-\\d{2}-01"))) { "Choose a valid billing month." }
        require(dueDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Choose a valid due date." }
        require(amount > 0) { "Amount must be greater than zero." }

        val eligibleUnits = BillingRules.unitsForScope(getBillableUnits(), scope)
        val existingUnitIds = client.from("condotrack_bills")
            .select { filter { eq("billing_month", month) } }
            .decodeList<BillRow>()
            .mapTo(mutableSetOf()) { it.unitId }
        val missingUnits = eligibleUnits.filterNot { it.id in existingUnitIds }

        if (missingUnits.isNotEmpty()) {
            client.from("condotrack_bills").insert(
                missingUnits.map { unit ->
                    NewBillPayload(
                        unitId = unit.id,
                        billingMonth = month,
                        amount = amount,
                        dueDate = dueDate,
                        status = "UNPAID"
                    )
                }
            )
        }

        return BillIssueResult(
            issued = missingUnits.size,
            skipped = eligibleUnits.size - missingUnits.size,
            totalAmount = missingUnits.size * amount
        )
    }

    suspend fun payBill(bill: MaintenanceFee, method: String): PaymentRecord {
        require(method == "DuitNow" || method == "FPX online banking")
        val storedBill = client.from("condotrack_bills")
            .select { filter { eq("id", bill.id) } }
            .decodeList<BillRow>()
            .firstOrNull()
            ?: error("Maintenance bill not found.")
        check(storedBill.status.equals("UNPAID", true)) { "This maintenance fee is already paid." }

        val receiptNumber = createReceiptNumber()
        val payload = NewPaymentPayload(
            billId = storedBill.id,
            unitId = storedBill.unitId,
            amount = storedBill.amount,
            method = method,
            receiptNumber = receiptNumber
        )

        client.from("condotrack_payments").insert(payload)
        try {
            client.from("condotrack_bills").update(
                {
                    set("status", "PAID")
                    set("paid_at", isoTimestamp())
                }
            ) {
                filter {
                    eq("id", storedBill.id)
                    eq("status", "UNPAID")
                }
            }
            val updated = client.from("condotrack_bills")
                .select { filter { eq("id", storedBill.id) } }
                .decodeList<BillRow>()
                .firstOrNull()
                ?: error("Updated bill could not be loaded.")
            check(updated.status.equals("PAID", true)) { "Bill status could not be updated." }
        } catch (error: Throwable) {
            runCatching {
                client.from("condotrack_payments").delete {
                    filter { eq("receipt_number", receiptNumber) }
                }
            }
            throw error
        }

        val payment = client.from("condotrack_payments")
            .select { filter { eq("receipt_number", receiptNumber) } }
            .decodeList<PaymentRow>()
            .firstOrNull()
            ?: error("Payment receipt could not be loaded.")

        return PaymentRecord(
            id = payment.id,
            amount = money(payment.amount),
            method = payment.method,
            paidAt = payment.paidAt,
            receiptNumber = payment.receiptNumber
        )
    }

    private fun BillRow.toDomain() = MaintenanceFee(
        id = id,
        month = billingMonth,
        amount = money(amount),
        dueDate = dueDate,
        paidDate = paidAt,
        status = status
    )

    private fun money(amount: Double): String =
        "RM " + NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)

    private fun isoTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

    private fun createReceiptNumber(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.US)
        return "CT-$date-$suffix"
    }
}
