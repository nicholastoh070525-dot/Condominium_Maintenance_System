package com.example.condominium_maintenance_system.data.model

object PhoneRules {
    private val malaysianMobile = Regex("^1\\d{8,9}$")

    private fun nationalDigits(phone: String): String =
        phone.filter(Char::isDigit)
            .removePrefix("60")
            .removePrefix("0")

    fun inputDigits(phone: String): String = nationalDigits(phone).take(10)

    fun normalize(phone: String): String {
        val national = nationalDigits(phone)
        return when {
            national.length == 9 ->
                "+60 ${national.take(2)} ${national.drop(2).take(3)} ${national.drop(5)}"
            national.length == 10 ->
                "+60 ${national.take(2)} ${national.drop(2).take(4)} ${national.drop(6)}"
            else -> national
        }
    }

    fun validationMessage(phone: String): String? =
        if (malaysianMobile.matches(nationalDigits(phone))) {
            null
        } else {
            "Enter 9 or 10 digits after +60, starting with 1."
        }
}
