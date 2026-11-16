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

    private var currentRole: UserRole = UserRole.RESIDENT
    private var currentUserId: String = ""
    private var currentUnitCode: String? = null

    fun load(role: UserRole, userId: String, unitCode: String?) {
        currentRole = role
        currentUserId = userId
        currentUnitCode = unitCode
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { fetchAll() }
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
                            error = error.userMessage(
                                "Unable to load maintenance data. Check that the maintenance tables are set up in Supabase."
                            )
                        )
                    }
                }
        }
    }

    fun refresh() {
        if (currentUserId.isBlank()) return
        load(currentRole, currentUserId, currentUnitCode)
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
                repository.createRequest(profile, draft, resolver)
                fetchAll()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            saving = false,
                            message = null
                        )
                    }
                    onSubmitted()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            saving = false,
                            error = error.userMessage(
                                "Unable to submit the request. Check the photo storage bucket and try again."
                            )
                        )
                    }
                }
        }
    }

    fun updateRequest(
        request: MaintenanceRequest,
        update: MaintenanceRequestUpdate
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(processingId = request.id, error = null, message = null)
            }
            runCatching {
                repository.updateRequest(request.id, update)
                fetchAll()
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
                            error = error.userMessage("Unable to update the maintenance request.")
                        )
                    }
                }
        }
    }

    fun deleteRequest(request: MaintenanceRequest, residentId: String) {
        if (request.residentId != residentId) {
            _state.update { it.copy(error = "You can only delete your own complaints.") }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(processingId = request.id, error = null, message = null)
            }
            runCatching {
                repository.deleteRequest(request.id, residentId) to fetchAll()
            }
                .onSuccess { (photosRemoved, refreshed) ->
                    val (requests, tasks) = refreshed
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            processingId = null,
                            message = if (photosRemoved) {
                                "Complaint deleted."
                            } else {
                                "Complaint deleted. Some photos could not be removed from storage."
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to delete the complaint.")
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
                fetchAll()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            saving = false,
                            message = "Maintenance task scheduled and assigned to you."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            saving = false,
                            error = error.userMessage("Unable to create the maintenance task.")
                        )
                    }
                }
        }
    }

    fun updateTask(
        task: MaintenanceTask,
        managerId: String,
        status: String,
        report: String? = null
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(processingId = task.id, error = null, message = null)
            }
            runCatching {
                repository.updateTask(task.id, status, managerId, report)
                fetchAll()
            }
                .onSuccess { (requests, tasks) ->
                    _state.update {
                        it.copy(
                            requests = requests,
                            tasks = tasks,
                            processingId = null,
                            message = "Task status updated."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingId = null,
                            error = error.userMessage("Unable to update the maintenance task.")
                        )
                    }
                }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }

    private suspend fun fetchAll(): Pair<List<MaintenanceRequest>, List<MaintenanceTask>> =
        repository.getRequests(currentRole, currentUserId) to
                repository.getTasks(currentRole, currentUnitCode)
}
