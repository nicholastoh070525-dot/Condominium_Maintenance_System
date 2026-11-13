package com.example.condominium_maintenance_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import com.example.condominium_maintenance_system.data.repository.AuthRepository
import com.example.condominium_maintenance_system.data.repository.ProfileRepository
import com.example.condominium_maintenance_system.ui.RegistrationDraft
import com.example.condominium_maintenance_system.ui.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class AuthUiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val profile: UserProfile? = null,
    val pendingSignup: RegistrationDraft? = null,
    val recoveryEmail: String? = null,
    val recoveryVerified: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        restoreSession()
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }

    fun signIn(email: String, password: String) {
        execute("Signing in…") {
            authRepository.signIn(email, password).getOrThrow()
            val userId = authRepository.currentUserId() ?: error("Account could not be loaded.")
            val profile = profileRepository.getProfile(userId)
                ?: error("Your CondoTrack profile is missing.")
            _state.update {
                it.copy(profile = profile, loading = false, busy = false, message = "Welcome back.")
            }
        }
    }

    fun beginRegistration(draft: RegistrationDraft) {
        execute("Creating your account…") {
            authRepository.startRegistration(draft.email, draft.password).getOrThrow()
            _state.update {
                it.copy(
                    pendingSignup = draft,
                    busy = false,
                    message = "A six-digit verification code was sent to ${draft.email}."
                )
            }
        }
    }

    fun verifyRegistration(code: String) {
        val draft = _state.value.pendingSignup ?: return
        execute("Verifying code…") {
            authRepository.verifyRegistrationOtp(draft.email, code).getOrThrow()
            val userId = authRepository.currentUserId() ?: error("Verified account could not be loaded.")
            val profile = profileRepository.createProfile(userId, draft)
            _state.update {
                it.copy(
                    profile = profile,
                    pendingSignup = null,
                    loading = false,
                    busy = false,
                    message = "Email verified. Your account is active."
                )
            }
        }
    }

    fun resendRegistrationCode() {
        val email = _state.value.pendingSignup?.email ?: return
        execute("Sending a new code…") {
            authRepository.resendRegistrationOtp(email).getOrThrow()
            _state.update { it.copy(busy = false, message = "A new verification code was sent.") }
        }
    }

    fun requestPasswordReset(email: String) {
        execute("Sending recovery code…") {
            authRepository.requestPasswordReset(email).getOrThrow()
            _state.update {
                it.copy(
                    recoveryEmail = email.trim().lowercase(),
                    recoveryVerified = false,
                    busy = false,
                    message = "If the account exists, a recovery code was sent."
                )
            }
        }
    }

    fun verifyRecoveryCode(code: String) {
        val email = _state.value.recoveryEmail ?: return
        execute("Verifying recovery code…") {
            authRepository.verifyRecoveryOtp(email, code).getOrThrow()
            _state.update {
                it.copy(
                    recoveryVerified = true,
                    busy = false,
                    message = "Code verified. Create a new password."
                )
            }
        }
    }

    fun resendRecoveryCode() {
        val email = _state.value.recoveryEmail ?: return
        execute("Sending a new code…") {
            authRepository.requestPasswordReset(email).getOrThrow()
            _state.update { it.copy(busy = false, message = "A new recovery code was sent.") }
        }
    }

    fun setNewPassword(password: String) {
        execute("Updating password…") {
            authRepository.updatePassword(password).getOrThrow()
            authRepository.signOut().getOrThrow()
            _state.value = AuthUiState(
                loading = false,
                message = "Password updated. Sign in with your new password."
            )
        }
    }

    fun cancelPendingFlow() {
        _state.update {
            it.copy(
                pendingSignup = null,
                recoveryEmail = null,
                recoveryVerified = false,
                busy = false,
                error = null,
                message = null
            )
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId() ?: return@launch
            val profile = profileRepository.getProfile(userId) ?: return@launch
            val authEmail = authRepository.currentEmail()
            val synced = if (profile.role == com.example.condominium_maintenance_system.ui.UserRole.MANAGEMENT &&
                authEmail != null && authEmail != profile.email
            ) {
                profileRepository.syncManagementEmail(userId, authEmail) ?: profile
            } else {
                profile
            }
            _state.update { it.copy(profile = synced) }
        }
    }

    fun signOut() {
        execute("Signing out…") {
            authRepository.signOut().getOrThrow()
            _state.value = AuthUiState(loading = false, message = "Signed out.")
        }
    }

    private fun restoreSession() {
        if (!SupabaseProvider.isConfigured) {
            _state.value = AuthUiState(
                loading = false,
                error = "Supabase is not configured. Add its URL and publishable key to local.properties."
            )
            return
        }
        viewModelScope.launch {
            val result = withTimeoutOrNull(10_000) {
                authRepository.restoreSession().mapCatching { userId ->
                    userId?.let { id ->
                        val stored = profileRepository.getProfile(id)
                        val authEmail = authRepository.currentEmail()
                        if (stored?.role == com.example.condominium_maintenance_system.ui.UserRole.MANAGEMENT &&
                            authEmail != null && stored.email != authEmail
                        ) {
                            profileRepository.syncManagementEmail(id, authEmail) ?: stored
                        } else {
                            stored
                        }
                    }
                }
            }
            if (result == null) {
                _state.value = AuthUiState(
                    loading = false,
                    error = "Supabase did not respond. Check your internet connection and project URL."
                )
            } else {
                result
                    .onSuccess { profile ->
                        _state.value = AuthUiState(loading = false, profile = profile)
                    }
                    .onFailure {
                        _state.value = AuthUiState(
                            loading = false,
                            error = it.userMessage("Unable to restore your session.")
                        )
                    }
            }
        }
    }

    private fun execute(status: String, operation: suspend () -> Unit) {
        _state.update { it.copy(busy = true, error = null, message = status) }
        viewModelScope.launch {
            runCatching { operation() }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            busy = false,
                            error = error.userMessage("Something went wrong."),
                            message = null
                        )
                    }
                }
        }
    }
}

internal fun Throwable.userMessage(fallback: String): String {
    val raw = message?.takeIf { it.isNotBlank() } ?: return fallback
    val normalized = raw.lowercase()
    val retrySeconds = Regex("""after\s+(\d+)\s+seconds?""", RegexOption.IGNORE_CASE)
        .find(raw)
        ?.groupValues
        ?.getOrNull(1)
    return when {
        "error sending confirmation email" in normalized ||
            "unexpected_failure" in normalized ->
            "Verification email could not be sent. Check the Supabase SMTP sender and use a verified Resend sending domain."

        "invalid login credentials" in normalized ->
            "Email or password is incorrect."

        "email not confirmed" in normalized ->
            "Verify your email code before signing in."

        "user already registered" in normalized ->
            "An account with this email already exists. Sign in or reset your password."

        "rate limit" in normalized ||
            "too many requests" in normalized ||
            "security purposes" in normalized ->
            retrySeconds?.let {
                "Please wait $it seconds before requesting another email code."
            } ?: "Too many email requests. Please wait before requesting another code."

        else -> raw.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { fallback }
    }
}
