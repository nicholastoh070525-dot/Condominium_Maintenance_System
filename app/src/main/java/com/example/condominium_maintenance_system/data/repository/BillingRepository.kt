package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.model.BillRow
import com.example.condominium_maintenance_system.data.model.NewPaymentPayload
import com.example.condominium_maintenance_system.data.model.PaymentRow
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import io.github.jan.supabase.postgrest.from
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class BillingRepository {
    private val client = SupabaseProvider.client

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
