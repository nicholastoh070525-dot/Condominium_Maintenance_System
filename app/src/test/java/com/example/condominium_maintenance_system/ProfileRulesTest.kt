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
        assertNull(PhoneRules.validationMessage("108661002"))
        assertNull(PhoneRules.validationMessage("1123456789"))
        assertNull(PhoneRules.validationMessage("0123456789"))
        assertEquals("+60 10 866 1002", PhoneRules.normalize("010-866 1002"))
        assertEquals("+60 11 2345 6789", PhoneRules.normalize("1123456789"))
    }

    @Test
    fun malformedPhoneNumbersAreRejected() {
        listOf(
            "234567890",
            "12345678",
            "11234567890",
            "10a8661002"
        ).forEach { phone ->
            assertEquals(
                "Enter 9 or 10 digits after +60, starting with 1.",
                PhoneRules.validationMessage(phone)
            )
        }
    }
}
