package com.example.condominium_maintenance_system.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.AuthRepository
import com.example.condominium_maintenance_system.data.repository.ProfileRepository
import com.example.condominium_maintenance_system.ui.ProfileUpdateDraft
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val profiles: List<UserProfile> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load(userId: String, role: UserRole) {
        if (!SupabaseProvider.isConfigured) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val profile = repository.getProfile(userId) ?: error("Profile not found.")
                val profiles = if (role == UserRole.MANAGEMENT) repository.getAllProfiles() else emptyList()
                profile to profiles
            }.onSuccess { (profile, profiles) ->
                _state.update {
                    it.copy(
                        profile = profile,
                        profiles = profiles,
                        loading = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.userMessage("Unable to load profiles."))
                }
            }
        }
    }

    fun save(
        userId: String,
        update: ProfileUpdateDraft,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = null) }
            runCatching {
                val current = _state.value.profile ?: error("Profile not loaded.")
                var emailMessage: String? = null
                if (current.role == UserRole.MANAGEMENT &&
                    update.email.trim().lowercase() != current.email.lowercase()
                ) {
                    authRepository.requestEmailChange(update.email).getOrThrow()
                    emailMessage = "Check the new email address to confirm the change."
                }
                repository.updateProfile(
                    userId = userId,
                    fullName = update.fullName,
                    phone = update.phone,
                    avatarKey = update.avatarKey,
                    avatarUrl = update.avatarUrl,
                    unitBlock = update.block,
                    unitFloor = update.floor,
                    unitRoom = update.room
                ) to emailMessage
            }.onSuccess { (profile, emailMessage) ->
                _state.update {
                    it.copy(
                        profile = profile,
                        saving = false,
                        message = emailMessage ?: "Profile updated."
                    )
                }
                onComplete(true)
            }.onFailure { error ->
                _state.update {
                    it.copy(saving = false, error = error.userMessage("Unable to update profile."))
                }
                onComplete(false)
            }
        }
    }

    fun uploadAvatar(userId: String, uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = "Uploading avatar…") }
            runCatching {
                val url = repository.uploadAvatar(userId, uri, resolver)
                val profile = _state.value.profile ?: error("Profile not loaded.")
                repository.updateProfile(
                    userId = userId,
                    fullName = profile.fullName,
                    phone = profile.phone,
                    avatarKey = profile.avatarKey,
                    avatarUrl = url,
                    enforcePhoneValidation = false
                )
            }.onSuccess { profile ->
                _state.update {
                    it.copy(profile = profile, saving = false, message = "Avatar updated.")
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(saving = false, error = error.userMessage("Unable to upload avatar."))
                }
            }
        }
    }
}
