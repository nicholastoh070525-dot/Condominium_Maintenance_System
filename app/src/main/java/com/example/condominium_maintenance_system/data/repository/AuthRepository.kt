package com.example.condominium_maintenance_system.data.repository

import com.example.condominium_maintenance_system.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val auth
        get() = SupabaseProvider.client.auth

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email.trim().lowercase()
            this.password = password
        }
    }

    suspend fun startRegistration(email: String, password: String): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email.trim().lowercase()
            this.password = password
        }
        if (auth.currentSessionOrNull() != null) {
            auth.signOut()
            error("Email confirmation is disabled in Supabase. Enable Confirm Email to enforce OTP registration.")
        }
    }

    suspend fun verifyRegistrationOtp(email: String, code: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email.trim().lowercase(),
            token = code.trim()
        )
        Unit
    }

    suspend fun resendRegistrationOtp(email: String): Result<Unit> = runCatching {
        auth.resendEmail(OtpType.Email.SIGNUP, email.trim().lowercase())
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email.trim().lowercase(), redirectUrl = null)
    }

    suspend fun verifyRecoveryOtp(email: String, code: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(
            type = OtpType.Email.RECOVERY,
            email = email.trim().lowercase(),
            token = code.trim()
        )
        Unit
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.updateUser { password = newPassword }
    }

    suspend fun requestEmailChange(newEmail: String): Result<Unit> = runCatching {
        auth.updateUser { email = newEmail.trim().lowercase() }
    }

    suspend fun currentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun currentEmail(): String? = auth.currentUserOrNull()?.email

    suspend fun restoreSession(): Result<String?> = runCatching {
        auth.awaitInitialization()
        auth.currentUserOrNull()?.id
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }
}
