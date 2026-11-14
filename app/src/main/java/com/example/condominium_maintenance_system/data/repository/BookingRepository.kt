package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.model.FacilityBookingRow
import com.example.condominium_maintenance_system.data.model.NewFacilityBookingPayload
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.UserProfile
import io.github.jan.supabase.postgrest.from

private const val TABLE = "condotrack_facility_bookings"

class BookingRepository {
    private val client
        get() = SupabaseProvider.client

    suspend fun getAll(): List<FacilityBooking> =
        client.from(TABLE)
            .select()
            .decodeList<FacilityBookingRow>()
            .map { it.toDomain() }
            .sortedWith(compareBy({ it.date }, { it.slot }))

    suspend fun create(profile: UserProfile, booking: FacilityBooking) {
        val unitCode = profile.unitCode
        require(unitCode != null) { "Resident unit is required." }
        client.from(TABLE).insert(
            NewFacilityBookingPayload(
                residentId = profile.id,
                residentName = profile.fullName,
                unitCode = unitCode,
                facility = booking.facility,
                bookingDay = booking.date,
                slot = booking.slot
            )
        )
    }

    suspend fun updateStatus(bookingId: String, status: String) {
        client.from(TABLE).update(
            { set("status", status) }
        ) {
            filter { eq("id", bookingId) }
        }

        val updated = client.from(TABLE)
            .select { filter { eq("id", bookingId) } }
            .decodeList<FacilityBookingRow>()
            .firstOrNull()
            ?: error("Booking not found.")
        check(updated.status == status) { "Booking could not be updated." }
    }

    private fun FacilityBookingRow.toDomain() = FacilityBooking(
        facility = facility,
        unit = unitCode,
        date = bookingDay,
        slot = slot,
        status = status,
        id = id,
        residentName = residentName
    )
}