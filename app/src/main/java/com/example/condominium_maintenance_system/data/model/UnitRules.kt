package com.example.condominium_maintenance_system.data.model

object UnitRules {
    val blocks = listOf("A", "B")
    val residentialFloors = (11..40).toList()
    val roomNumbers = (1..28).toList()

    fun validationMessage(block: String?, floor: Int?, room: Int?): String? = when {
        block == null || block !in blocks -> "Select Block A or Block B."
        floor == null || floor !in residentialFloors ->
            "Residential floors are 11 to 40 only. Floors 1–9 are parking and floor 10 is facilities."
        room == null || room !in roomNumbers ->
            "Room number must be between 01 and 28."
        else -> null
    }

    fun isValid(block: String?, floor: Int?, room: Int?): Boolean =
        validationMessage(block, floor, room) == null

    fun code(block: String, floor: Int, room: Int): String =
        "$block-${floor.toString().padStart(2, '0')}-${room.toString().padStart(2, '0')}"
}
