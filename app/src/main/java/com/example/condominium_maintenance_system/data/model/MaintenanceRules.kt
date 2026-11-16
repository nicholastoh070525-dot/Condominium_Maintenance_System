package com.example.condominium_maintenance_system.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object MaintenanceRules {
    val requestStatuses = listOf(
        "PENDING",
        "IN_PROGRESS",
        "COMPLETED"
    )

    val taskStatuses = listOf(
        "ASSIGNED",
        "IN_PROGRESS",
        "COMPLETED",
        "CANCELLED"
    )

    private val timestampPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ssX",
        "yyyy-MM-dd"
    )

    fun normalizeRequestStatus(value: String): String {
        val cleaned = value.trim()
            .removeSuffix("::text")
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
            .uppercase(Locale.US)
            .replace('-', '_')
            .replace(' ', '_')
        return cleaned.ifBlank { "PENDING" }
    }

    fun requestStatusLabel(value: String): String = normalizeRequestStatus(value)

    fun canChangeRequestStatus(current: String, next: String): Boolean =
        when (normalizeRequestStatus(current)) {
            "PENDING" -> normalizeRequestStatus(next) == "IN_PROGRESS"
            "IN_PROGRESS" -> normalizeRequestStatus(next) == "COMPLETED"
            else -> false
        }

    fun canApplyRequestStatus(current: String, next: String): Boolean {
        val normalizedCurrent = normalizeRequestStatus(current)
        val normalizedNext = normalizeRequestStatus(next)
        return normalizedCurrent == normalizedNext ||
                canChangeRequestStatus(normalizedCurrent, normalizedNext)
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
        normalizeRequestStatus(value) != "COMPLETED"

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

    fun formatTimestamp(
        value: String?,
        pattern: String = "d MMM yyyy · h:mm a",
        fallback: String = "Not scheduled"
    ): String {
        val date = parseTimestamp(value.orEmpty()) ?: return fallback
        return SimpleDateFormat(pattern, Locale.US).format(date)
    }

    fun dayOfMonth(value: String?): Int? {
        val date = parseTimestamp(value.orEmpty()) ?: return null
        return Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
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
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(local.time)
    }
}
