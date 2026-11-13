package com.example.condominium_maintenance_system

import com.example.condominium_maintenance_system.data.model.UnitRules
import com.example.condominium_maintenance_system.data.model.PhoneRules
import com.example.condominium_maintenance_system.ui.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileRulesTest {
    @Test
    fun residentUnitCanBeParsedAndRebuilt() {
        val unit = UnitRules.parse("B-12-13")

        assertEquals("B", unit?.block)
        assertEquals(12, unit?.floor)
        assertEquals(13, unit?.room)
        assertEquals("B-12-13", unit?.code)
    }

    @Test
    fun invalidResidentUnitCannotBeParsed() {
        assertNull(UnitRules.parse("B-10-13"))
        assertNull(UnitRules.parse("C-12-13"))
        assertNull(UnitRules.parse("B-12-29"))
    }

    @Test
    fun databaseRoleIsCaseInsensitive() {
        assertEquals(UserRole.MANAGEMENT, UserRole.fromDatabase("MANAGEMENT"))
        assertEquals(UserRole.MANAGEMENT, UserRole.fromDatabase("management"))
        assertEquals(UserRole.RESIDENT, UserRole.fromDatabase("resident"))
    }

    @Test(expected = IllegalStateException::class)
    fun invalidDatabaseRoleIsRejected() {
        UserRole.fromDatabase("ADMIN")
    }

    @Test
    fun malaysianPhoneNumberIsAccepted() {
        assertNull(PhoneRules.validationMessage("+60 12 345 6789"))
    }

    @Test
    fun malformedPhoneNumbersAreRejected() {
        assertNull(PhoneRules.validationMessage("+60 12 345 6789"))
        listOf(
            "0123456789",
            "+60 123456789",
            "+60 1 2345 6789",
            "+61 12 345 6789",
            "+60 12 345 678a"
        ).forEach { phone ->
            assertEquals(
                "Use a Malaysian number in the format +60 12 345 6789.",
                PhoneRules.validationMessage(phone)
            )
        }
    }
}
