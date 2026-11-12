package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.model.NewVisitorPayload
import com.example.condominium_maintenance_system.data.model.VisitorRow
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.VisitorRequest
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class VisitorRepository {
    private val client = SupabaseProvider.client

    suspend fun getMyRequests(residentId: String): List<VisitorRequest> =
        client.from("condotrack_visitor_requests")
            .select {
                filter { eq("resident_id", residentId) }
            }
            .decodeList<VisitorRow>()
            .map { it.toDomain() }
            .sortedByDescending { it.visitTime }

    suspend fun getAllRequests(): List<VisitorRequest> =
        client.from("condotrack_visitor_requests")
            .select()
            .decodeList<VisitorRow>()
            .map { it.toDomain() }
            .sortedByDescending { it.visitTime }

    suspend fun createRequest(profile: UserProfile, request: VisitorRequest) {
        require(profile.unitCode != null) { "Resident unit is required." }
        client.from("condotrack_visitor_requests").insert(
            NewVisitorPayload(
                residentId = profile.id,
                visitorName = request.name.trim(),
                vehicleColor = request.vehicleColor.trim(),
                numberPlate = request.plate.trim().uppercase(),
                visitAt = request.visitTime,
                note = request.note.trim(),
                residentName = profile.fullName,
                unitCode = profile.unitCode
            )
        )
    }

    suspend fun reviewRequest(requestId: String, status: String) {
        require(status == "APPROVED" || status == "REJECTED")
        val now = isoTimestamp()
        val passReference = if (status == "APPROVED") createPassReference() else null

        client.from("condotrack_visitor_requests").update(
            {
                set("status", status)
                set("approved_at", if (status == "APPROVED") now else null)
                set("pass_reference", passReference)
            }
        ) {
            filter {
                eq("id", requestId)
                eq("status", "PENDING")
            }
        }

        val updated = getRequest(requestId)
        check(updated.status == status) { "Visitor request could not be updated." }
    }

    suspend fun openPass(requestId: String, residentId: String): VisitorRequest {
        client.from("condotrack_visitor_requests").update(
            {
                set("status", "COMPLETE")
                set("completed_at", isoTimestamp())
            }
        ) {
            filter {
                eq("id", requestId)
                eq("resident_id", residentId)
                eq("status", "APPROVED")
            }
        }
        val pass = getRequest(requestId)
        check(pass.status == "COMPLETE") { "Approved vehicle pass not found." }
        return pass
    }

    private suspend fun getRequest(requestId: String): VisitorRequest {
        val row = client.from("condotrack_visitor_requests")
            .select { filter { eq("id", requestId) } }
            .decodeList<VisitorRow>()
            .firstOrNull()
            ?: error("Visitor request not found.")
        return row.toDomain()
    }

    private fun isoTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

    private fun createPassReference(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.US)
        return "CT-$date-$suffix"
    }

    private fun VisitorRow.toDomain() = VisitorRequest(
        name = visitorName,
        plate = numberPlate,
        visitTime = visitAt,
        bay = "",
        status = status,
        id = id,
        vehicleColor = vehicleColor,
        note = note,
        unitCode = unitCode,
        residentName = residentName,
        approvedAt = approvedAt,
        completedAt = completedAt,
        passReference = passReference
    )
}
