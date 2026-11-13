package com.example.condominium_maintenance_system.data.model

object PhoneRules {
    private val malaysianMobile = Regex("^\\+60 \\d{2} \\d{3} \\d{4}$")

    fun validationMessage(phone: String): String? =
        if (malaysianMobile.matches(phone.trim())) {
            null
        } else {
            "Use a Malaysian number in the format +60 12 345 6789."
        }
}
