package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceRulesTest {
    @Test
    fun statusesAreNormalizedAndCompletedRequestsAreClosed() {
        assertEquals("IN_PROGRESS", MaintenanceRules.normalizeRequestStatus("in progress"))
        assertEquals("COMPLETED", MaintenanceRules.normalizeRequestStatus("Completed"))
        assertTrue(MaintenanceRules.isOpen("PENDING"))
        assertFalse(MaintenanceRules.isOpen("COMPLETED"))
    }

    @Test
    fun scheduledTimestampCanBeParsedAndFormatted() {
        val scheduled = MaintenanceRules.scheduleTimestamp(
            year = 2026,
            month = 10,
            day = 20,
            hour = 10,
            minute = 30
        )

        assertNotNull(MaintenanceRules.parseTimestamp(scheduled))
        assertTrue(MaintenanceRules.formatTimestamp(scheduled).contains("2026"))
    }
}
