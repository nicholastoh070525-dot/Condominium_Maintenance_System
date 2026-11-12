package com.example.condominium_maintenance_system.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    @SerialName("unit_id") val unitId: String? = null,
    @SerialName("unit_code") val unitCode: String? = null,
    @SerialName("avatar_key") val avatarKey: String = "AM",
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class UnitRow(
    val id: String,
    val block: String,
    @SerialName("floor_number") val floorNumber: Int,
    @SerialName("room_number") val roomNumber: Int,
    @SerialName("unit_code") val unitCode: String
)

@Serializable
data class VisitorRow(
    val id: String,
    @SerialName("resident_id") val residentId: String,
    @SerialName("visitor_name") val visitorName: String,
    @SerialName("vehicle_color") val vehicleColor: String,
    @SerialName("number_plate") val numberPlate: String,
    @SerialName("visit_at") val visitAt: String,
    val note: String = "",
    val status: String,
    @SerialName("pass_reference") val passReference: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("resident_name") val residentName: String = "",
    @SerialName("unit_code") val unitCode: String = ""
)

@Serializable
data class BillRow(
    val id: String,
    @SerialName("unit_id") val unitId: String,
    @SerialName("billing_month") val billingMonth: String,
    val amount: Double,
    @SerialName("due_date") val dueDate: String,
    val status: String,
    @SerialName("paid_at") val paidAt: String? = null
)

@Serializable
data class PaymentRow(
    val id: String,
    @SerialName("bill_id") val billId: String,
    val amount: Double,
    val method: String,
    @SerialName("paid_at") val paidAt: String,
    @SerialName("receipt_number") val receiptNumber: String
)

@Serializable
data class NewProfilePayload(
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    @SerialName("unit_id") val unitId: String? = null,
    @SerialName("unit_code") val unitCode: String? = null,
    @SerialName("avatar_key") val avatarKey: String = "AM"
)

@Serializable
data class NewVisitorPayload(
    @SerialName("resident_id") val residentId: String,
    @SerialName("visitor_name") val visitorName: String,
    @SerialName("vehicle_color") val vehicleColor: String,
    @SerialName("number_plate") val numberPlate: String,
    @SerialName("visit_at") val visitAt: String,
    val note: String = "",
    @SerialName("resident_name") val residentName: String,
    @SerialName("unit_code") val unitCode: String
)

@Serializable
data class NewPaymentPayload(
    @SerialName("bill_id") val billId: String,
    @SerialName("unit_id") val unitId: String,
    val amount: Double,
    val method: String,
    @SerialName("receipt_number") val receiptNumber: String
)
