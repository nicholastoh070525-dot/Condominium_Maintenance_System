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

    fun normalizeRequestStatus(status: String): String {
        val cleaned = status.trim()
            .removeSuffix("::text")
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
            .uppercase()
        return cleaned.ifBlank { "PENDING" }
    }

    fun requestStatusLabel(status: String): String = normalizeRequestStatus(status)

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

    fun taskStatusLabel(status: String): String = when (status.uppercase()) {
        "ASSIGNED" -> "Assigned"
        "IN_PROGRESS" -> "In progress"
        "COMPLETED" -> "Completed"
        "CANCELLED" -> "Cancelled"
        else -> requestStatusLabel(status)
    }

    fun isOpen(status: String): Boolean =
        normalizeRequestStatus(status) != "COMPLETED"

    fun isClosed(status: String): Boolean = !isOpen(status)

    fun formatTimestamp(
        value: String?,
        pattern: String,
        fallback: String = "Not scheduled"
    ): String {
        if (value.isNullOrBlank()) return fallback
        val parsed = parseTimestamp(value) ?: return value
        return SimpleDateFormat(pattern, Locale.US).format(parsed)
    }

    fun dayOfMonth(value: String?): Int? {
        val parsed = parseTimestamp(value ?: return null) ?: return null
        return Calendar.getInstance().apply { time = parsed }.get(Calendar.DAY_OF_MONTH)
    }

    fun scheduleTimestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(calendar.time)
    }

    fun dateTimeSummary(value: String?): String =
        formatTimestamp(value, "d MMM yyyy · h:mm a")

    fun dateSummary(value: String?): String =
        formatTimestamp(value, "d MMM yyyy")

    fun timeSummary(value: String?): String =
        formatTimestamp(value, "h:mm a")

    fun parseTimestamp(value: String): Date? {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    if (pattern.endsWith("'Z'")) {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    isLenient = false
                }.parse(value)
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }
}
