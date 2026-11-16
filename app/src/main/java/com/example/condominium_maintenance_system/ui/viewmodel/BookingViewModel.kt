package com.example.condominium_maintenance_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.BookingRepository
import com.example.condominium_maintenance_system.ui.Facility
import com.example.condominium_maintenance_system.ui.FacilityBooking
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.occupiesSlot
import com.example.condominium_maintenance_system.ui.overlaps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingUiState(
    val bookings: List<FacilityBooking> = emptyList(),
    val facilities: List<Facility> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class BookingViewModel(
    private val repository: BookingRepository = BookingRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    fun load() {
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.getAll() to repository.getFacilities() }
                .onSuccess { (bookings, facilities) ->
                    _state.update {
                        it.copy(bookings = bookings, facilities = facilities, loading = false)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.userMessage("Unable to load facility bookings."))
                    }
                }
        }
    }

    fun add(profile: UserProfile, booking: FacilityBooking) {
        val taken = _state.value.bookings.any {
            it.overlaps(booking.facility, booking.date, booking.startMinute, booking.endMinute)
        }
        if (taken) {
            _state.update { it.copy(error = "That time is already booked. Please choose another time.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.create(profile, booking)
                repository.getAll()
            }
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            bookings = list,
                            loading = false,
                            message = "Facility booking request submitted."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.userMessage("Unable to submit booking. The time slot may have just been taken.")
                        )
                    }
                }
        }
    }

    fun updateStatus(booking: FacilityBooking, status: String) {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            runCatching {
                repository.updateStatus(booking.id, status)
                repository.getAll()
            }
                .onSuccess { list ->
                    _state.update { it.copy(bookings = list, message = "Booking $status.") }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.userMessage("Unable to update booking.")) }
                }
        }
    }

    fun addFacility(name: String, symbol: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _state.update { it.copy(error = "Facility name is required.") }
            return
        }
        if (_state.value.facilities.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _state.update { it.copy(error = "That facility already exists.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.addFacility(trimmed, symbol)
                repository.getFacilities()
            }
                .onSuccess { list ->
                    _state.update {
                        it.copy(facilities = list, loading = false, message = "Facility added.")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.userMessage("Unable to add facility."))
                    }
                }
        }
    }

    fun deleteFacility(facility: Facility) {
        val inUse = _state.value.bookings.any {
            it.facility == facility.name && it.occupiesSlot()
        }
        if (inUse) {
            _state.update {
                it.copy(error = "This facility still has pending or approved bookings. Please handle them first.")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.deleteFacility(facility)
                repository.getFacilities()
            }
                .onSuccess { list ->
                    _state.update {
                        it.copy(facilities = list, loading = false, message = "Facility deleted.")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.userMessage("Unable to delete facility."))
                    }
                }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }
}