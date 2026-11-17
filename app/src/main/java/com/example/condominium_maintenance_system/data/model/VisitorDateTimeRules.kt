package com.example.condominium_maintenance_system.data.model

import java.text.SimpleDateFormat
import java.text.ParsePosition
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
    fun datePickerMillis(
        dateText: String?,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val localDate = dateText
            ?.takeIf { it.isNotBlank() }
            ?.let { text ->
                runCatching {
                    SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
                        isLenient = false
                        this.timeZone = timeZone
                    }.parse(text.trim())
                }.getOrNull()
            }
        val local = Calendar.getInstance(timeZone).apply {
            if (localDate == null) timeInMillis = nowMillis else time = localDate
        }
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return utc.timeInMillis
    }

    fun dateTextFromPickerMillis(pickerMillis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(pickerMillis))

    fun formatDateInput(input: String): String {
        val digits = input.filter(Char::isDigit).take(8)
        return buildString {
            digits.forEachIndexed { index, character ->
                if (index == 2 || index == 4) append('/')
                append(character)
            }
        }
    }

    fun formatTimeInput(input: String): String {
        val digits = input.filter(Char::isDigit).take(4)
        return if (digits.length <= 2) {
            digits
        } else {
            "${digits.take(2)}:${digits.drop(2)}"
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

        val timeInput = timeText.trim()
        val parsedTime = listOf("H:mm", "HH:mm")
            .firstNotNullOfOrNull { pattern ->
                val formatter = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    this.timeZone = timeZone
                }
                val position = ParsePosition(0)
                runCatching { formatter.parse(timeInput, position) }
                    .getOrNull()
                    ?.takeIf { position.index == timeInput.length }
            }
            ?: return VisitorDateTimeValidation(
                timeError = "Use 24-hour time such as 13:00."
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
        val displayValue = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US).apply {
            this.timeZone = timeZone
        }.format(selectedDate)
        return VisitorDateTimeValidation(
            isoValue = isoValue,
            displayValue = displayValue
        )
    }
}
