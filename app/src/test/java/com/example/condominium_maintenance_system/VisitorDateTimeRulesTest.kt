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
            timeText = "6:00 PM",
            nowMillis = now,
            timeZone = timeZone
        )

        assertNotNull(result.isoValue)
        assertEquals("15 Nov 2026 · 6:00 PM", result.displayValue)
    }

    @Test
    fun dateInputIsFormattedWhileTyping() {
        assertEquals("15/11/2026", VisitorDateTimeRules.formatDateInput("15112026"))
        assertEquals("15/11", VisitorDateTimeRules.formatDateInput("1511"))
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
