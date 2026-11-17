package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.VisitorDateTimeRules
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VisitorDateTimeRulesTest {
    private val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    private val now = Calendar.getInstance(timeZone).apply {
        set(2026, Calendar.NOVEMBER, 12, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun futureDateTimeIsAccepted() {
        val result = VisitorDateTimeRules.validate(
            dateText = "15/11/2026",
            timeText = "18:00",
            nowMillis = now,
            timeZone = timeZone
        )

        assertNotNull(result.isoValue)
        assertEquals("15 Nov 2026 · 18:00", result.displayValue)
    }

    @Test
    fun amPmTimeIsRejected() {
        val result = VisitorDateTimeRules.validate(
            dateText = "15/11/2026",
            timeText = "1:00 PM",
            nowMillis = now,
            timeZone = timeZone
        )

        assertNull(result.isoValue)
        assertEquals(
            "Use 24-hour time such as 13:00.",
            result.timeError
        )
    }

    @Test
    fun timeInputFormatsDigitsAs24HourTime() {
        assertEquals("13:00", VisitorDateTimeRules.formatTimeInput("1300"))
        assertEquals("18:30", VisitorDateTimeRules.formatTimeInput("18:30"))
    }

    @Test
    fun dateInputIsFormattedWhileTyping() {
        assertEquals("15/11/2026", VisitorDateTimeRules.formatDateInput("15112026"))
        assertEquals("15/11", VisitorDateTimeRules.formatDateInput("1511"))
    }

    @Test
    fun calendarPickerKeepsTheSelectedLocalDate() {
        val pickerMillis = VisitorDateTimeRules.datePickerMillis(
            dateText = "15/11/2026",
            nowMillis = now,
            timeZone = timeZone
        )
        val utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = pickerMillis
        }

        assertEquals(2026, utcDate.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, utcDate.get(Calendar.MONTH))
        assertEquals(15, utcDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(
            "15/11/2026",
            VisitorDateTimeRules.dateTextFromPickerMillis(pickerMillis)
        )
    }

    @Test
    fun pastDateIsRejected() {
        val result = VisitorDateTimeRules.validate(
            dateText = "11/11/2026",
            timeText = "6:00 PM",
            nowMillis = now,
            timeZone = timeZone
        )

        assertNull(result.isoValue)
        assertEquals("Visit date cannot be in the past.", result.dateError)
    }

    @Test
    fun invalidTimeIsRejected() {
        val result = VisitorDateTimeRules.validate(
            dateText = "15/11/2026",
            timeText = "25:99",
            nowMillis = now,
            timeZone = timeZone
        )

        assertNull(result.isoValue)
        assertNotNull(result.timeError)
    }
}
