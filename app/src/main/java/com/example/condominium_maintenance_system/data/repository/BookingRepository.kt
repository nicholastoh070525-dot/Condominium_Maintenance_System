package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.model.FacilityBookingRow
import com.example.condominium_maintenance_system.data.model.FacilityRow
import com.example.condominium_maintenance_system.data.model.NewFacilityBookingPayload
import com.example.condominium_maintenance_system.data.model.NewFacilityPayload
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.Facility
import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.overlaps
import io.github.jan.supabase.postgrest.from

private const val TABLE = "condotrack_facility_bookings"
private const val FACILITY_TABLE = "condotrack_facilities"

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

        // 用数据库里最新的数据检查,不只看手机上的副本
        val clash = client.from(TABLE)
            .select { filter { eq("facility", booking.facility) } }
            .decodeList<FacilityBookingRow>()
            .map { it.toDomain() }
            .any { it.overlaps(booking.facility, booking.date, booking.startMinute, booking.endMinute) }
        check(!clash) { "That time is already booked. Please choose another time." }

        client.from(TABLE).insert(
            NewFacilityBookingPayload(
                residentId = profile.id,
                residentName = profile.fullName,
                unitCode = unitCode,
                facility = booking.facility,
                bookingDay = booking.date,
                slot = booking.slot,
                startMinute = booking.startMinute,
                endMinute = booking.endMinute
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

    // ---------- Facilities ----------

    suspend fun getFacilities(): List<Facility> =
        client.from(FACILITY_TABLE)
            .select()
            .decodeList<FacilityRow>()
            .map { Facility(name = it.name, symbol = it.symbol, id = it.id) }
            .sortedBy { it.name.lowercase() }

    suspend fun addFacility(name: String, symbol: String) {
        client.from(FACILITY_TABLE).insert(
            NewFacilityPayload(name = name, symbol = symbol)
        )
    }

    suspend fun deleteFacility(facility: Facility) {
        // 还有 Pending 或 Approved 的预订时,不允许删除
        val active = client.from(TABLE)
            .select { filter { eq("facility", facility.name) } }
            .decodeList<FacilityBookingRow>()
            .filter { it.status == "Pending" || it.status == "Approved" }
        check(active.isEmpty()) {
            "This facility still has pending or approved bookings. Please handle them first."
        }

        client.from(FACILITY_TABLE).delete {
            filter { eq("id", facility.id) }
        }

        // 删除后再查一次,确认真的删掉了
        val remaining = client.from(FACILITY_TABLE)
            .select { filter { eq("id", facility.id) } }
            .decodeList<FacilityRow>()
        check(remaining.isEmpty()) { "Facility could not be deleted." }
    }

    private fun FacilityBookingRow.toDomain() = FacilityBooking(
        facility = facility,
        unit = unitCode,
        date = bookingDay,
        slot = slot,
        status = status,
        id = id,
        residentName = residentName,
        startMinute = startMinute,
        endMinute = endMinute
    )
}