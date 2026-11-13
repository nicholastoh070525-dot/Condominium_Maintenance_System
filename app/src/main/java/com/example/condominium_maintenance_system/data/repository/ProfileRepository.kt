package com.example.condominium_maintenance_system.data.repository

import android.net.Uri
import com.example.condominium_maintenance_system.data.model.NewProfilePayload
import com.example.condominium_maintenance_system.data.model.PhoneRules
import com.example.condominium_maintenance_system.data.model.ProfileRow
import com.example.condominium_maintenance_system.data.model.UnitRow
import com.example.condominium_maintenance_system.data.model.UnitRules
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.RegistrationDraft
import com.example.condominium_maintenance_system.ui.UnitAvailability
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class ProfileRepository {
    private val client
        get() = SupabaseProvider.client

    suspend fun getUnit(block: String, floor: Int, room: Int): UnitAvailability? {
        val row = client.from("condotrack_units")
            .select {
                filter {
                    eq("block", block)
                    eq("floor_number", floor)
                    eq("room_number", room)
                }
                limit(1)
            }
            .decodeList<UnitRow>()
            .firstOrNull()
            ?: return null
        return UnitAvailability(row.id, row.block, row.floorNumber, row.roomNumber, row.unitCode)
    }

    suspend fun createProfile(userId: String, draft: RegistrationDraft): UserProfile {
        val normalizedPhone = PhoneRules.normalize(draft.phone)
        PhoneRules.validationMessage(normalizedPhone)?.let(::error)
        val unit = if (draft.role == UserRole.RESIDENT) {
            draft.block?.let { block ->
                draft.floor?.let { floor ->
                    draft.room?.let { room -> getUnit(block, floor, room) }
                }
            } ?: error("Select a valid residential unit.")
        } else {
            null
        }
        val initials = draft.fullName
            .trim()
            .split(Regex("\\s+"))
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "CT" }

        client.from("condotrack_profiles").insert(
            NewProfilePayload(
                userId = userId,
                fullName = draft.fullName.trim(),
                email = draft.email.trim().lowercase(),
                phone = normalizedPhone,
                role = draft.role.name,
                unitId = unit?.id,
                unitCode = unit?.unitCode,
                avatarKey = initials
            )
        )
        return getProfile(userId) ?: error("Profile could not be loaded.")
    }

    suspend fun getProfileByEmail(email: String): UserProfile? =
        client.from("condotrack_profiles")
            .select { filter { eq("email", email.trim().lowercase()) } }
            .decodeList<ProfileRow>()
            .firstOrNull()
            ?.toDomain()

    suspend fun getProfile(userId: String): UserProfile? {
        val row = client.from("condotrack_profiles")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ProfileRow>()
            .firstOrNull()
            ?: return null
        return row.toDomain()
    }

    suspend fun getAllProfiles(): List<UserProfile> =
        client.from("condotrack_profiles")
            .select()
            .decodeList<ProfileRow>()
            .map { it.toDomain() }
            .sortedBy { it.fullName }

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        avatarKey: String,
        avatarUrl: String?,
        unitBlock: String? = null,
        unitFloor: Int? = null,
        unitRoom: Int? = null,
        enforcePhoneValidation: Boolean = true
    ): UserProfile {
        val normalizedPhone = PhoneRules.normalize(phone)
        if (enforcePhoneValidation) {
            PhoneRules.validationMessage(normalizedPhone)?.let(::error)
        }
        val current = getProfile(userId) ?: error("Profile not found.")
        val selectedUnit = if (unitBlock != null || unitFloor != null || unitRoom != null) {
            if (current.role != UserRole.RESIDENT) {
                error("Only resident profiles can select a residential unit.")
            }
            UnitRules.validationMessage(unitBlock, unitFloor, unitRoom)?.let(::error)
            val block = requireNotNull(unitBlock)
            val floor = requireNotNull(unitFloor)
            val room = requireNotNull(unitRoom)
            getUnit(
                block = block,
                floor = floor,
                room = room
            ) ?: error("Residential unit ${UnitRules.code(block, floor, room)} was not found.")
        } else {
            null
        }

        client.from("condotrack_profiles").update(
            {
                set("full_name", fullName.trim())
                set("phone", normalizedPhone)
                set("avatar_key", avatarKey)
                set("avatar_url", avatarUrl)
                selectedUnit?.let { unit ->
                    set("unit_id", unit.id)
                    set("unit_code", unit.unitCode)
                }
            }
        ) {
            filter { eq("user_id", userId) }
        }
        return getProfile(userId) ?: error("Profile update could not be loaded.")
    }

    suspend fun syncManagementEmail(userId: String, email: String): UserProfile? {
        client.from("condotrack_profiles").update(
            { set("email", email.trim().lowercase()) }
        ) {
            filter {
                eq("user_id", userId)
                eq("role", UserRole.MANAGEMENT.name)
            }
        }
        return getProfile(userId)
    }

    suspend fun uploadAvatar(userId: String, uri: Uri, resolver: android.content.ContentResolver): String {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read the selected image.")
        val extension = resolver.getType(uri)?.substringAfterLast('/') ?: "jpg"
        val path = "$userId/avatar.$extension"
        val bucket = client.storage.from("condotrack-avatars")
        bucket.upload(path, bytes) { upsert = true }
        return bucket.publicUrl(path)
    }

    private fun ProfileRow.toDomain() = UserProfile(
        id = userId,
        fullName = fullName,
        email = email,
        phone = phone,
        role = UserRole.fromDatabase(role),
        unitId = unitId,
        unitCode = unitCode,
        avatarKey = avatarKey,
        avatarUrl = avatarUrl
    )
}
