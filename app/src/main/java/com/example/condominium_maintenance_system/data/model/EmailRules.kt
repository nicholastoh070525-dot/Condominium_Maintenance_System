package com.example.condominium_maintenance_system.data.model

object EmailRules {
    private val emailPattern =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValid(email: String): Boolean = emailPattern.matches(email.trim())

    fun validationMessage(email: String): String? {
        val value = email.trim()
        return when {
            value.isBlank() -> "Enter your email address."
            !isValid(value) -> "Enter a valid email address, e.g. name@gmail.com."
            else -> null
        }
    }
}
