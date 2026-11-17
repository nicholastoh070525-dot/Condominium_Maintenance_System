package com.example.condominium_maintenance_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.model.BillingRules
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.AuthRepository
import com.example.condominium_maintenance_system.data.repository.BillingRepository
import com.example.condominium_maintenance_system.ui.BillIssueResult
import com.example.condominium_maintenance_system.ui.BillableUnit
import com.example.condominium_maintenance_system.ui.BillingScope
import com.example.condominium_maintenance_system.ui.ManagementBill
import java.util.Calendar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagementBillingUiState(
    val month: String = currentBillingMonth(),
    val bills: List<ManagementBill> = emptyList(),
    val paidBills: List<ManagementBill> = emptyList(),
    val units: List<BillableUnit> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val deletingBills: Boolean = false,
    val lastIssueResult: BillIssueResult? = null,
    val error: String? = null,
    val message: String? = null
) {
    val monthLabel: String
        get() = BillingRules.monthLabel(month)
}

class ManagementBillingViewModel(
    private val repository: BillingRepository = BillingRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ManagementBillingUiState())
    val state: StateFlow<ManagementBillingUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun load() {
        if (!SupabaseProvider.isConfigured) return
        val month = _state.value.month
        loadJob?.cancel()
        _state.update { it.copy(loading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                val snapshot = repository.getManagementSnapshot(month)
                if (_state.value.month == month) {
                    _state.update {
                        it.copy(
                            bills = snapshot.bills,
                            paidBills = snapshot.paidBills,
                            units = snapshot.units,
                            loading = false
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (_state.value.month == month) {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.userMessage("Unable to load management billing.")
                        )
                    }
                }
            }
        }
    }

    fun selectMonth(month: String) {
        if (!month.matches(Regex("\\d{4}-(0[1-9]|1[0-2])-01"))) return
        if (month == _state.value.month) return
        _state.update {
            it.copy(
                month = month,
                bills = emptyList(),
                paidBills = emptyList(),
                lastIssueResult = null,
                message = null,
                error = null
            )
        }
        load()
    }

    fun changeMonth(offset: Int) {
        val calendar = Calendar.getInstance().apply {
            val parts = _state.value.month.split("-")
            clear()
            set(
                parts[0].toIntOrNull() ?: 2000,
                (parts[1].toIntOrNull() ?: 1) - 1,
                1
            )
            add(Calendar.MONTH, offset)
        }
        selectMonth(
            BillingRules.monthDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH)
            )
        )
    }

    fun issueBills(
        scope: BillingScope,
        amountText: String,
        dueDateText: String,
        onSuccess: (BillIssueResult) -> Unit = {}
    ) {
        val amount = amountText.trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = "Enter an amount greater than zero.") }
            return
        }
        val dueDate = BillingRules.parseDueDate(dueDateText)
        if (dueDate == null) {
            _state.update { it.copy(error = "Enter the due date as DD/MM/YYYY.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = null) }
            runCatching {
                repository.issueBills(
                    month = _state.value.month,
                    amount = amount,
                    dueDate = dueDate,
                    scope = scope
                )
            }.onSuccess { result ->
                _state.update {
                    it.copy(
                        saving = false,
                        lastIssueResult = result,
                        message = "Issued ${result.issued} bills. Skipped ${result.skipped} existing bills."
                    )
                }
                load()
                onSuccess(result)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        saving = false,
                        error = error.userMessage("Unable to create bills.")
                    )
                }
            }
        }
    }

    fun deleteMonthBills(
        password: String,
        onSuccess: () -> Unit = {}
    ) {
        val month = _state.value.month
        val deletableBills = BillingRules.deletableBillsForMonth(_state.value.bills, month)
        if (deletableBills.isEmpty()) {
            _state.update { it.copy(error = "There are no unpaid bills to delete for this month.") }
            return
        }
        if (password.isBlank()) {
            _state.update { it.copy(error = "Enter your account password.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(deletingBills = true, error = null, message = null) }
            if (authRepository.verifyCurrentAccountPassword(password).isFailure) {
                _state.update {
                    it.copy(
                        deletingBills = false,
                        error = "Password verification failed. No bills were deleted."
                    )
                }
                return@launch
            }

            runCatching { repository.deleteUnpaidBillsForMonth(month) }
                .onSuccess { deletedCount ->
                    if (deletedCount == 0) {
                        _state.update {
                            it.copy(
                                deletingBills = false,
                                error = "There are no unpaid bills to delete for this month."
                            )
                        }
                        return@onSuccess
                    }
                    _state.update {
                        it.copy(
                            deletingBills = false,
                            message = "Deleted $deletedCount unpaid bills for ${BillingRules.monthLabel(month)}."
                        )
                    }
                    load()
                    onSuccess()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            deletingBills = false,
                            error = error.userMessage("Unable to delete the unpaid bills.")
                        )
                    }
                }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }
}

private fun currentBillingMonth(): String {
    val calendar = Calendar.getInstance()
    return BillingRules.monthDate(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH)
    )
}
