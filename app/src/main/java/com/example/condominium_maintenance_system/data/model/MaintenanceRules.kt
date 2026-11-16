package com.example.condominium_maintenance_system.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object MaintenanceRules {
    private val timestampPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ssX"
    )

    fun normalizeRequestStatus(value: String): String =
        value.trim()
            .uppercase(Locale.US)
            .replace('-', '_')
            .replace(' ', '_')
            .ifBlank { "PENDING" }

    fun requestStatusLabel(value: String): String = when (normalizeRequestStatus(value)) {
        "IN_PROGRESS" -> "In progress"
        "COMPLETED" -> "Completed"
        "CANCELLED" -> "Cancelled"
        else -> "Pending"
    }

    fun taskStatusLabel(value: String): String = when (
        value.trim().uppercase(Locale.US).replace('-', '_').replace(' ', '_')
    ) {
        "IN_PROGRESS" -> "In progress"
        "COMPLETED" -> "Completed"
        "CANCELLED" -> "Cancelled"
        else -> "Assigned"
    }

    fun isOpen(value: String): Boolean =
        normalizeRequestStatus(value) !in setOf("COMPLETED", "CANCELLED")

    fun parseTimestamp(value: String): Date? {
        if (value.isBlank()) return null
        return timestampPatterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(value)
            }.getOrNull()
        }
    }

    fun formatTimestamp(value: String?): String {
        val date = parseTimestamp(value.orEmpty()) ?: return "Not scheduled"
        return SimpleDateFormat("d MMM yyyy · h:mm a", Locale.US).format(date)
    }

    fun dateSummary(value: String?): String {
        val date = parseTimestamp(value.orEmpty()) ?: return "Choose a date"
        return SimpleDateFormat("EEE, d MMM yyyy", Locale.US).format(date)
    }

    fun timeSummary(value: String?): String {
        val date = parseTimestamp(value.orEmpty()) ?: return "Time not set"
        return SimpleDateFormat("h:mm a", Locale.US).format(date)
    }

    fun dateTimeSummary(value: String?): String =
        "${dateSummary(value)} · ${timeSummary(value)}"

    fun scheduleTimestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): String {
        val local = Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(local.time)
    }
}
