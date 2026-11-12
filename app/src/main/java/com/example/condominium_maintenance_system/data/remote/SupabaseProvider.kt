package com.example.condominium_maintenance_system.data.remote

import com.example.condominium_maintenance_system.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client by lazy {
        check(isConfigured) {
            "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                scheme = BuildConfig.SUPABASE_SCHEME.ifBlank { "condotrack" }
                host = BuildConfig.SUPABASE_HOST.ifBlank { "reset" }
            }
            install(Postgrest)
            install(Storage)
        }
    }
}
