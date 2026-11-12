package com.example.condominium_maintenance_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.BillingRepository
import com.example.condominium_maintenance_system.ui.MaintenanceFee
import com.example.condominium_maintenance_system.ui.PaymentRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillingUiState(
    val bills: List<MaintenanceFee> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val loading: Boolean = false,
    val paying: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val currentBill: MaintenanceFee?
        get() = bills.firstOrNull { it.status.equals("UNPAID", ignoreCase = true) } ?: bills.firstOrNull()
}

class BillingViewModel(
    private val repository: BillingRepository = BillingRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    fun load(unitId: String) {
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.getBills(unitId) to repository.getPayments(unitId) }
                .onSuccess { (bills, payments) ->
                    _state.update { it.copy(bills = bills, payments = payments, loading = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.userMessage("Unable to load billing."))
                    }
                }
        }
    }

    fun pay(bill: MaintenanceFee, unitId: String, method: String) {
        viewModelScope.launch {
            _state.update { it.copy(paying = true, error = null) }
            runCatching { repository.payBill(bill, method) }
                .onSuccess { payment ->
                    _state.update {
                        it.copy(
                            paying = false,
                            payments = listOf(payment) + it.payments,
                            message = "$method payment completed. Receipt ${payment.receiptNumber}."
                        )
                    }
                    load(unitId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(paying = false, error = error.userMessage("Payment failed."))
                    }
                }
        }
    }
}
