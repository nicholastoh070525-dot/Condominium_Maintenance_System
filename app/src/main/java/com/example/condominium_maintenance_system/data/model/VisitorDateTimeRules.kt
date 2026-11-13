package com.example.condominium_maintenance_system.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class VisitorDateTimeValidation(
    val isoValue: String? = null,
    val displayValue: String? = null,
    val dateError: String? = null,
    val timeError: String? = null
) {
    val isValid: Boolean
        get() = isoValue != null && dateError == null && timeError == null
}

object VisitorDateTimeRules {
    fun formatDateInput(input: String): String {
        val digits = input.filter(Char::isDigit).take(8)
        return buildString {
            digits.forEachIndexed { index, character ->
                if (index == 2 || index == 4) append('/')
                append(character)
            }
        }
    }

    fun validate(
        dateText: String,
        timeText: String,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): VisitorDateTimeValidation {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            isLenient = false
            this.timeZone = timeZone
        }
        val date = runCatching { dateFormat.parse(dateText.trim()) }.getOrNull()
            ?: return VisitorDateTimeValidation(
                dateError = "Use a valid date in DD/MM/YYYY format."
            )

        val calendar = Calendar.getInstance(timeZone).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.before(today)) {
            return VisitorDateTimeValidation(
                dateError = "Visit date cannot be in the past."
            )
        }

        val parsedTime = listOf("h:mm a", "hh:mm a", "H:mm", "HH:mm")
            .firstNotNullOfOrNull { pattern ->
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    this.timeZone = timeZone
                }.let { formatter -> runCatching { formatter.parse(timeText.trim()) }.getOrNull() }
            }
            ?: return VisitorDateTimeValidation(
                timeError = "Use a valid time such as 6:00 PM or 18:00."
            )

        val timeCalendar = Calendar.getInstance(timeZone).apply { time = parsedTime }
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= nowMillis) {
            return VisitorDateTimeValidation(
                isoValue = null,
                dateError = null,
                timeError = "Visit date and time must be in the future."
            )
        }

        val selectedDate: Date = calendar.time
        val isoValue = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
            this.timeZone = timeZone
        }.format(selectedDate)
        val displayValue = SimpleDateFormat("dd MMM yyyy · h:mm a", Locale.US).apply {
            this.timeZone = timeZone
        }.format(selectedDate)
        return VisitorDateTimeValidation(
            isoValue = isoValue,
            displayValue = displayValue
        )
    }
}
