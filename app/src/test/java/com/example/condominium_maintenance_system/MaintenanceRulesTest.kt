package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceRulesTest {
    @Test
    fun workflowHasOnlyPendingInProgressAndCompleted() {
        assertEquals(
            listOf("PENDING", "IN_PROGRESS", "COMPLETED"),
            MaintenanceRules.requestStatuses
        )
    }

    @Test
    fun malformedLegacyStatusValuesAreNormalized() {
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus("\"PENDING\"::text"))
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus("'PENDING'"))
        assertEquals("PENDING", MaintenanceRules.normalizeRequestStatus(" pending "))
    }

    @Test
    fun statusLabelsUseCanonicalUppercaseValues() {
        assertEquals("PENDING", MaintenanceRules.requestStatusLabel("\"PENDING\"::text"))
        assertEquals("IN_PROGRESS", MaintenanceRules.requestStatusLabel("in progress"))
        assertEquals("COMPLETED", MaintenanceRules.requestStatusLabel("Completed"))
    }

    @Test
    fun requestOnlyMovesForwardOneStage() {
        assertTrue(MaintenanceRules.canChangeRequestStatus("PENDING", "IN_PROGRESS"))
        assertTrue(MaintenanceRules.canChangeRequestStatus("IN_PROGRESS", "COMPLETED"))
        assertFalse(MaintenanceRules.canChangeRequestStatus("PENDING", "COMPLETED"))
        assertFalse(MaintenanceRules.canChangeRequestStatus("COMPLETED", "IN_PROGRESS"))
    }

    @Test
    fun repeatedSameStatusUpdateIsIdempotent() {
        assertTrue(MaintenanceRules.canApplyRequestStatus("PENDING", "PENDING"))
        assertTrue(MaintenanceRules.canApplyRequestStatus("IN_PROGRESS", "IN_PROGRESS"))
        assertTrue(MaintenanceRules.canApplyRequestStatus("COMPLETED", "COMPLETED"))
        assertFalse(MaintenanceRules.canApplyRequestStatus("COMPLETED", "IN_PROGRESS"))
    }

    @Test
    fun openRequestsExcludeCompleted() {
        assertTrue(MaintenanceRules.isOpen("PENDING"))
        assertTrue(MaintenanceRules.isOpen("\"IN_PROGRESS\"::text"))
        assertFalse(MaintenanceRules.isOpen("COMPLETED"))
    }

    @Test
    fun taskStatusesHaveReadableLabels() {
        assertEquals("Assigned", MaintenanceRules.taskStatusLabel("ASSIGNED"))
        assertEquals("In progress", MaintenanceRules.taskStatusLabel("IN_PROGRESS"))
        assertEquals("Completed", MaintenanceRules.taskStatusLabel("COMPLETED"))
    }

    @Test
    fun scheduledTimestampCanBeParsedAndFormatted() {
        val scheduled = MaintenanceRules.scheduleTimestamp(
            year = 2026,
            month = 10,
            day = 20,
            hour = 14,
            minute = 30
        )
        assertNotNull(MaintenanceRules.parseTimestamp(scheduled))
        assertTrue(MaintenanceRules.dateSummary(scheduled).contains("20 Nov 2026"))
        assertEquals("2:30 PM", MaintenanceRules.timeSummary(scheduled))
    }
}
