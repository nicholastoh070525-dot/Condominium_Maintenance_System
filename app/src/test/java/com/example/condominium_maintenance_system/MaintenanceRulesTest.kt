package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceRulesTest {
    @Test
    fun requestWorkflowHasOnlyThreeStatuses() {
        assertEquals(
            listOf("PENDING", "IN_PROGRESS", "COMPLETED"),
            MaintenanceRules.requestStatuses
        )
    }

    @Test
    fun openStatusesDriveTheResidentDashboard() {
        assertTrue(MaintenanceRules.isOpen("PENDING"))
        assertTrue(MaintenanceRules.isOpen("IN_PROGRESS"))
        assertFalse(MaintenanceRules.isOpen("COMPLETED"))
    }

    @Test
    fun databaseStatusesHaveReadableLabels() {
        assertEquals("PENDING", MaintenanceRules.requestStatusLabel("PENDING"))
        assertEquals("IN_PROGRESS", MaintenanceRules.requestStatusLabel("IN_PROGRESS"))
        assertEquals("COMPLETED", MaintenanceRules.requestStatusLabel("COMPLETED"))
        assertEquals("Assigned", MaintenanceRules.taskStatusLabel("ASSIGNED"))
    }

    @Test
    fun malformedLegacyStatusValuesAreNormalized() {
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus("\"PENDING\"::text"))
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus("'PENDING'"))
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus(" pending "))
        assertTrue(MaintenanceRules.canChangeRequestStatus("\"PENDING\"::text", "IN_PROGRESS"))
    }

    @Test
    fun requestStatusOnlyMovesForwardOneStage() {
        assertTrue(MaintenanceRules.canChangeRequestStatus("PENDING", "IN_PROGRESS"))
        assertTrue(MaintenanceRules.canChangeRequestStatus("IN_PROGRESS", "COMPLETED"))
        assertFalse(MaintenanceRules.canChangeRequestStatus("PENDING", "COMPLETED"))
        assertFalse(MaintenanceRules.canChangeRequestStatus("COMPLETED", "IN_PROGRESS"))
        assertFalse(MaintenanceRules.canChangeRequestStatus("PENDING", "PENDING"))
    }

    @Test
    fun repeatedSameStatusUpdateIsIdempotent() {
        assertTrue(MaintenanceRules.canApplyRequestStatus("PENDING", "PENDING"))
        assertTrue(MaintenanceRules.canApplyRequestStatus("IN_PROGRESS", "IN_PROGRESS"))
        assertTrue(MaintenanceRules.canApplyRequestStatus("COMPLETED", "COMPLETED"))
        assertFalse(MaintenanceRules.canApplyRequestStatus("COMPLETED", "IN_PROGRESS"))
    }

    @Test
    fun isoDateTimeCanBeParsedForCalendarDisplay() {
        assertNotNull(MaintenanceRules.parseTimestamp("2026-11-14T10:30:00+08:00"))
        assertEquals(14, MaintenanceRules.dayOfMonth("2026-11-14T12:00:00+08:00"))
    }

    @Test
    fun generatedScheduleKeepsTheSelectedVisitTime() {
        val timestamp = MaintenanceRules.scheduleTimestamp(
            year = 2026,
            month = 10,
            day = 20,
            hour = 14,
            minute = 30
        )
        assertEquals("2:30 PM", MaintenanceRules.timeSummary(timestamp))
        assertEquals("20 Nov 2026", MaintenanceRules.dateSummary(timestamp))
    }
}
