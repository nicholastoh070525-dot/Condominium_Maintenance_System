package com.example.condominium_maintenance_system.ui.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.MaintenanceRepository
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.MaintenanceRequestDraft
import com.example.condominium_maintenance_system.ui.MaintenanceRequestUpdate
import com.example.condominium_maintenance_system.ui.MaintenanceTask
import com.example.condominium_maintenance_system.ui.MaintenanceTaskDraft
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceUiState(
    val requests: List<MaintenanceRequest> = emptyList(),
    val tasks: List<MaintenanceTask> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val processingId: String? = null,
    val error: String? = null,
    val message: String? = null
)

class MaintenanceViewModel(
    private val repository: MaintenanceRepository = MaintenanceRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(MaintenanceUiState())
    val state: StateFlow<MaintenanceUiState> = _state.asStateFlow()

    private var role = UserRole.RESIDENT
    private var userId = ""
    private var unitCode: String? = null

    fun load(role: UserRole, userId: String, unitCode: String?) {
        this.role = role
        this.userId = userId
        this.unitCode = unitCode
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { loadData() }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            loading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.userMessage("Unable to load maintenance data.")
                        )
                    }
                }
        }
    }

    fun refresh() {
        if (userId.isBlank()) return
        load(role, userId, unitCode)
    }

    fun submitRequest(
        profile: UserProfile,
        draft: MaintenanceRequestDraft,
        resolver: ContentResolver,
        onSubmitted: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = null) }
            runCatching {
                repository.submitRequest(profile, draft, resolver)
                loadData()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            saving = false,
                            message = "Maintenance request submitted."
                        )
                    }
                    onSubmitted()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            saving = false,
                            error = error.userMessage("Unable to submit maintenance request.")
                        )
                    }
                }
        }
    }

    fun updateRequest(request: MaintenanceRequest, update: MaintenanceRequestUpdate) {
        viewModelScope.launch {
            _state.update { it.copy(processingId = request.id, error = null, message = null) }
            runCatching {
                repository.updateRequest(request.id, update)
                loadData()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            processingId = null,
                            message = "Maintenance request updated."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to update maintenance request.")
                        )
                    }
                }
        }
    }

    fun deleteRequest(request: MaintenanceRequest, residentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingId = request.id, error = null, message = null) }
            runCatching {
                repository.deleteRequest(request.id, residentId)
                loadData()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            processingId = null,
                            message = "Maintenance request deleted."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to delete maintenance request.")
                        )
                    }
                }
        }
    }

    fun createTask(profile: UserProfile, draft: MaintenanceTaskDraft) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = null) }
            runCatching {
                repository.createTask(profile, draft)
                loadData()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            saving = false,
                            message = "Maintenance task scheduled."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            saving = false,
                            error = error.userMessage("Unable to schedule maintenance task.")
                        )
                    }
                }
        }
    }

    fun updateTask(
        task: MaintenanceTask,
        managerId: String,
        status: String,
        report: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(processingId = task.id, error = null, message = null) }
            runCatching {
                repository.updateTask(task.id, managerId, status, report)
                loadData()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            processingId = null,
                            message = if (status == "COMPLETED") {
                                "Maintenance task completed."
                            } else {
                                "Maintenance task started."
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to update maintenance task.")
                        )
                    }
                }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }

    private suspend fun loadData(): Pair<List<MaintenanceRequest>, List<MaintenanceTask>> =
        repository.getRequests(role, userId) to repository.getTasks(role, unitCode)
}
