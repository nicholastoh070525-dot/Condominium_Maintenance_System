package com.example.condominium_maintenance_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.VisitorRepository
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.VisitorRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisitorUiState(
    val requests: List<VisitorRequest> = emptyList(),
    val loading: Boolean = false,
    val processingId: String? = null,
    val activePass: VisitorRequest? = null,
    val error: String? = null,
    val message: String? = null
)

class VisitorViewModel(
    private val repository: VisitorRepository = VisitorRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(VisitorUiState())
    val state: StateFlow<VisitorUiState> = _state.asStateFlow()

    fun load(role: UserRole, userId: String) {
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                if (role == UserRole.MANAGEMENT) repository.getAllRequests()
                else repository.getMyRequests(userId)
            }.onSuccess { requests ->
                _state.update { it.copy(requests = requests, loading = false) }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.userMessage("Unable to load visitor requests."))
                }
            }
        }
    }

    fun add(profile: UserProfile, visitor: VisitorRequest) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.createRequest(profile, visitor) }
                .onSuccess {
                    _state.update { it.copy(loading = false, message = "Visitor request submitted.") }
                    load(UserRole.RESIDENT, profile.id)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.userMessage("Unable to submit request."))
                    }
                }
        }
    }

    fun review(request: VisitorRequest, status: String, role: UserRole, userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingId = request.id, error = null) }
            runCatching {
                repository.reviewRequest(request.id, status)
                if (role == UserRole.MANAGEMENT) repository.getAllRequests()
                else repository.getMyRequests(userId)
            }
                .onSuccess { refreshed ->
                    _state.update {
                        it.copy(
                            requests = refreshed,
                            processingId = null,
                            message = "Visitor request $status."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to update visitor request.")
                        )
                    }
                }
        }
    }

    fun openPass(request: VisitorRequest, residentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingId = request.id, error = null) }
            runCatching { repository.openPass(request.id, residentId) }
                .onSuccess { pass ->
                    _state.update {
                        it.copy(
                            activePass = pass,
                            processingId = null,
                            requests = it.requests.map { row ->
                                if (row.id == pass.id) pass else row
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to open the vehicle pass.")
                        )
                    }
                }
        }
    }

    fun closePass() {
        _state.update { it.copy(activePass = null) }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }
}
