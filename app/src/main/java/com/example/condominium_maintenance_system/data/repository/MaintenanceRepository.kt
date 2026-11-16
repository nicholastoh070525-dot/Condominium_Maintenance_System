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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private const val REQUESTS_TABLE = "condotrack_maintenance_requests"
private const val TASKS_TABLE = "condotrack_maintenance_tasks"
private const val PHOTO_BUCKET = "condotrack-maintenance-images"

class MaintenanceRepository {
    private val client
        get() = SupabaseProvider.client

    suspend fun getRequests(role: UserRole, userId: String): List<MaintenanceRequest> {
        val rows = if (role == UserRole.RESIDENT) {
            client.from(REQUESTS_TABLE)
                .select { filter { eq("resident_id", userId) } }
                .decodeList<MaintenanceRequestRow>()
        } else {
            client.from(REQUESTS_TABLE)
                .select()
                .decodeList<MaintenanceRequestRow>()
        }
        return rows.map { it.toDomain() }
            .sortedWith(compareByDescending<MaintenanceRequest> { it.createdAt ?: "" }.thenByDescending { it.id })
    }

    suspend fun getTasks(
        role: UserRole,
        unitCode: String?
    ): List<MaintenanceTask> {
        val rows = if (role == UserRole.RESIDENT) {
            val unit = unitCode?.takeIf { it.isNotBlank() } ?: return emptyList()
            client.from(TASKS_TABLE)
                .select { filter { eq("unit_code", unit) } }
                .decodeList<MaintenanceTaskRow>()
        } else {
            client.from(TASKS_TABLE)
                .select()
                .decodeList<MaintenanceTaskRow>()
        }
        return rows.map { it.toDomain() }.sortedBy { it.scheduledAt }
    }

    suspend fun createRequest(
        profile: UserProfile,
        draft: MaintenanceRequestDraft,
        resolver: ContentResolver
    ) {
        require(draft.title.trim().isNotBlank()) { "Enter a short issue title." }
        require(draft.description.trim().isNotBlank()) { "Describe the maintenance issue." }
        val unitCode = profile.unitCode?.takeIf { it.isNotBlank() }
            ?: error("A residential unit is required before submitting a request.")

        val photoUrls = draft.photoUris.mapIndexed { index, uri ->
            uploadPhoto(profile.id, uri, resolver, index)
        }

        client.from(REQUESTS_TABLE).insert(
            NewMaintenanceRequestPayload(
                residentId = profile.id,
                residentName = profile.fullName,
                unitCode = unitCode,
                title = draft.title.trim(),
                category = draft.category,
                description = draft.description.trim(),
                urgency = draft.urgency,
                status = "PENDING",
                photoUrls = photoUrls
            )
        )
    }

    suspend fun deleteRequest(requestId: String, residentId: String): Boolean {
        val currentRequest = client.from(REQUESTS_TABLE)
            .select {
                filter {
                    eq("id", requestId)
                    eq("resident_id", residentId)
                }
            }
            .decodeList<MaintenanceRequestRow>()
            .firstOrNull()
            ?: error("Complaint not found or it does not belong to this resident.")
        val photoPaths = currentRequest.photoUrls.mapNotNull(::photoStoragePath)

        client.from(REQUESTS_TABLE).delete {
            filter {
                eq("id", requestId)
                eq("resident_id", residentId)
            }
        }

        val stillExists = client.from(REQUESTS_TABLE)
            .select { filter { eq("id", requestId) } }
            .decodeList<MaintenanceRequestRow>()
            .isNotEmpty()
        check(!stillExists) {
            "The complaint could not be deleted. It may already be removed or not belong to this resident."
        }

        if (photoPaths.isEmpty()) return true
        return runCatching {
            client.storage.from(PHOTO_BUCKET).delete(photoPaths)
        }.isSuccess
    }

    suspend fun updateRequest(
        requestId: String,
        update: MaintenanceRequestUpdate
    ) {
        require(update.status in MaintenanceRules.requestStatuses) {
            "Choose a valid maintenance status."
        }
        val currentRow = client.from(REQUESTS_TABLE)
            .select { filter { eq("id", requestId) } }
            .decodeList<MaintenanceRequestRow>()
            .firstOrNull()
            ?: error("Maintenance request not found.")
        val currentStatus = MaintenanceRules.normalizeRequestStatus(currentRow.status)
        val nextStatus = MaintenanceRules.normalizeRequestStatus(update.status)
        check(MaintenanceRules.canApplyRequestStatus(currentStatus, nextStatus)) {
            "This request is currently $currentStatus and cannot change to $nextStatus. Refresh the complaint list and continue from its current stage."
        }
        if (update.status == "IN_PROGRESS") {
            require(!update.technicianName.isNullOrBlank()) {
                "Assign a technician or vendor before starting the request."
            }
            require(!update.scheduledAt.isNullOrBlank()) {
                "Choose a maintenance visit date and time."
            }
        }
        if (update.status == "COMPLETED") {
            require(!update.report.isNullOrBlank()) {
                "Add a maintenance report before completing this request."
            }
        }

        client.from(REQUESTS_TABLE).update(
            {
                set("status", nextStatus)
                if (update.status == "IN_PROGRESS") {
                    set("technician_name", update.technicianName?.trim())
                    set("scheduled_at", update.scheduledAt)
                }
                if (update.status == "COMPLETED") {
                    set("report", update.report?.trim())
                }
                set("updated_at", nowTimestamp())
            }
        ) {
            filter {
                eq("id", requestId)
                eq("status", currentRow.status)
            }
        }

        val updated = client.from(REQUESTS_TABLE)
            .select { filter { eq("id", requestId) } }
            .decodeList<MaintenanceRequestRow>()
            .firstOrNull()
            ?: error("Maintenance request not found.")
        check(updated.status == update.status) {
            "The maintenance request could not be updated."
        }
    }

    suspend fun createTask(profile: UserProfile, draft: MaintenanceTaskDraft) {
        require(draft.title.trim().isNotBlank()) { "Enter a task title." }
        require(draft.scheduledAt.isNotBlank()) { "Choose a date and time." }

        client.from(TASKS_TABLE).insert(
            NewMaintenanceTaskPayload(
                title = draft.title.trim(),
                unitCode = draft.unit.trim().ifBlank { "Common area" },
                assigneeId = profile.id,
                assigneeName = profile.fullName,
                priority = draft.priority,
                scheduledAt = draft.scheduledAt,
                notes = draft.notes?.trim()?.takeIf { it.isNotBlank() }
            )
        )
    }

    suspend fun updateTask(
        taskId: String,
        status: String,
        managerId: String,
        report: String?
    ) {
        require(status in MaintenanceRules.taskStatuses) { "Choose a valid task status." }
        if (status == "COMPLETED") {
            require(!report.isNullOrBlank()) { "Add a short task report before completing it." }
        }

        client.from(TASKS_TABLE).update(
            {
                set("status", status)
                if (!report.isNullOrBlank()) set("report", report.trim())
            }
        ) {
            filter {
                eq("id", taskId)
                eq("assignee_id", managerId)
            }
        }

        val updated = client.from(TASKS_TABLE)
            .select { filter { eq("id", taskId) } }
            .decodeList<MaintenanceTaskRow>()
            .firstOrNull()
            ?: error("Maintenance task not found.")
        check(updated.status == status) {
            "Only the manager assigned to this task can update its status."
        }
    }

    private suspend fun uploadPhoto(
        userId: String,
        uri: Uri,
        resolver: ContentResolver,
        index: Int
    ): String {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read one of the selected photos.")
        val extension = resolver.getType(uri)
            ?.substringAfterLast('/')
            ?.substringBefore('+')
            ?.takeIf { it in setOf("jpg", "jpeg", "png", "webp") }
            ?: "jpg"
        val path = "$userId/${System.currentTimeMillis()}-$index.${extension}"
        val bucket = client.storage.from(PHOTO_BUCKET)
        bucket.upload(path, bytes)
        return bucket.publicUrl(path)
    }

    private fun nowTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

    private fun photoStoragePath(publicUrl: String): String? {
        val marker = "/object/public/$PHOTO_BUCKET/"
        val path = publicUrl.substringAfter(marker, "")
            .substringBefore('?')
            .takeIf { it.isNotBlank() }
            ?: return null
        return Uri.decode(path)
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
        status = status,
        notes = notes,
        report = report,
        createdAt = createdAt
    )
}
