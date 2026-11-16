package com.example.condominium_maintenance_system.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.condominium_maintenance_system.data.model.MaintenanceRequestRow
import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import com.example.condominium_maintenance_system.data.model.MaintenanceTaskRow
import com.example.condominium_maintenance_system.data.model.NewMaintenanceRequestPayload
import com.example.condominium_maintenance_system.data.model.NewMaintenanceTaskPayload
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.MaintenanceRequestDraft
import com.example.condominium_maintenance_system.ui.MaintenanceRequestUpdate
import com.example.condominium_maintenance_system.ui.MaintenanceTask
import com.example.condominium_maintenance_system.ui.MaintenanceTaskDraft
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

private const val REQUESTS_TABLE = "condotrack_maintenance_requests"
private const val TASKS_TABLE = "condotrack_maintenance_tasks"
private const val IMAGE_BUCKET = "condotrack-maintenance-images"

class MaintenanceRepository {
    private val client
        get() = SupabaseProvider.client

    suspend fun getRequests(role: UserRole, userId: String): List<MaintenanceRequest> {
        val query = if (role == UserRole.MANAGEMENT) {
            client.from(REQUESTS_TABLE).select()
        } else {
            client.from(REQUESTS_TABLE).select {
                filter { eq("resident_id", userId) }
            }
        }
        return query.decodeList<MaintenanceRequestRow>()
            .map { it.toDomain() }
            .sortedByDescending { it.createdAt.orEmpty() }
    }

    suspend fun getTasks(role: UserRole, unitCode: String?): List<MaintenanceTask> {
        val query = if (role == UserRole.MANAGEMENT || unitCode.isNullOrBlank()) {
            client.from(TASKS_TABLE).select()
        } else {
            client.from(TASKS_TABLE).select {
                filter { eq("unit_code", unitCode) }
            }
        }
        return query.decodeList<MaintenanceTaskRow>()
            .map { it.toDomain() }
            .sortedBy { it.scheduledAt }
    }

    suspend fun submitRequest(
        profile: UserProfile,
        draft: MaintenanceRequestDraft,
        resolver: ContentResolver
    ) {
        val photoUrls = draft.photoUris.mapIndexed { index, uri ->
            uploadPhoto(profile.id, uri, index, resolver)
        }
        client.from(REQUESTS_TABLE).insert(
            NewMaintenanceRequestPayload(
                residentId = profile.id,
                residentName = profile.fullName,
                unitCode = profile.unitCode.orEmpty(),
                title = draft.title.trim(),
                category = draft.category,
                description = draft.description.trim(),
                urgency = draft.urgency,
                status = "PENDING",
                photoUrls = photoUrls
            )
        )
    }

    suspend fun updateRequest(requestId: String, update: MaintenanceRequestUpdate) {
        client.from(REQUESTS_TABLE).update(
            {
                set("status", MaintenanceRules.normalizeRequestStatus(update.status))
                set("technician_name", update.technicianName)
                set("scheduled_at", update.scheduledAt)
                set("report", update.report)
            }
        ) {
            filter { eq("id", requestId) }
        }
    }

    suspend fun deleteRequest(requestId: String, residentId: String) {
        client.from(REQUESTS_TABLE).delete {
            filter {
                eq("id", requestId)
                eq("resident_id", residentId)
            }
        }
    }

    suspend fun createTask(manager: UserProfile, draft: MaintenanceTaskDraft) {
        client.from(TASKS_TABLE).insert(
            NewMaintenanceTaskPayload(
                title = draft.title.trim(),
                unitCode = draft.unit.trim(),
                assigneeId = manager.id,
                assigneeName = manager.fullName,
                priority = draft.priority,
                scheduledAt = draft.scheduledAt,
                notes = draft.notes?.trim()?.takeIf(String::isNotBlank)
            )
        )
    }

    suspend fun updateTask(
        taskId: String,
        managerId: String,
        status: String,
        report: String?
    ) {
        client.from(TASKS_TABLE).update(
            {
                set("status", status.trim().uppercase().replace(' ', '_'))
                set("report", report?.trim()?.takeIf(String::isNotBlank))
            }
        ) {
            filter {
                eq("id", taskId)
                eq("assignee_id", managerId)
            }
        }
    }

    private suspend fun uploadPhoto(
        userId: String,
        uri: Uri,
        index: Int,
        resolver: ContentResolver
    ): String {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read the selected photo.")
        val extension = resolver.getType(uri)
            ?.substringAfterLast('/')
            ?.substringBefore(';')
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        val path = "$userId/${System.currentTimeMillis()}-$index.$extension"
        val bucket = client.storage.from(IMAGE_BUCKET)
        bucket.upload(path, bytes) { upsert = true }
        return bucket.publicUrl(path)
    }

    private fun MaintenanceRequestRow.toDomain() = MaintenanceRequest(
        id = id,
        residentId = residentId,
        residentName = residentName,
        unitCode = unitCode,
        title = title,
        category = category,
        description = description,
        urgency = urgency,
        status = MaintenanceRules.normalizeRequestStatus(status),
        photoUrls = photoUrls,
        technicianName = technicianName,
        scheduledAt = scheduledAt,
        report = report,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MaintenanceTaskRow.toDomain() = MaintenanceTask(
        id = id,
        title = title,
        requestId = requestId,
        unit = unitCode,
        assigneeId = assigneeId,
        assigneeName = assigneeName,
        priority = priority,
        scheduledAt = scheduledAt,
        status = status.trim().uppercase().replace(' ', '_'),
        notes = notes,
        report = report,
        createdAt = createdAt
    )
}
