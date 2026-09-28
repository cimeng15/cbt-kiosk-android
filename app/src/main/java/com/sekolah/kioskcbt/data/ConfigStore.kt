package com.sekolah.kioskcbt.data

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat

/**
 * Wrapper SharedPreferences untuk konfigurasi kiosk + log aktivitas.
 *
 * Direkonstruksi dari `ConfigStore.java` (compiled from ConfigStore.kt).
 *
 * TODO (hardening): pindahkan password ke EncryptedSharedPreferences
 * (temuan keamanan: password disimpan plaintext).
 */
object ConfigStore {

    private const val PREFS_NAME = "kiosk_cbt_prefs"
    private const val KEY_CBT_URL = "cbt_url"
    private const val KEY_API_URL = "api_url"
    private const val KEY_PASSWORD = "exit_password"
    private const val KEY_EXPIRES_AT = "expires_at"
    private const val KEY_LOG = "activity_log"

    private const val MAX_LOG_LENGTH = 5000

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfig(context: Context): KioskConfig? {
        val p = prefs(context)
        val apiUrl = p.getString(KEY_API_URL, null) ?: return null
        val cbtUrl = p.getString(KEY_CBT_URL, null) ?: return null
        val password = p.getString(KEY_PASSWORD, null) ?: return null
        val expires = p.getLong(KEY_EXPIRES_AT, 0L)
        return KioskConfig(
            cbtUrl = cbtUrl,
            apiUrl = apiUrl,
            exitPassword = password,
            expiresAt = expires,
        )
    }

    fun hasConfig(context: Context): Boolean = getConfig(context) != null

    fun saveConfig(
        context: Context,
        cbtUrl: String,
        apiUrl: String,
        password: String,
        expiresAt: Long,
    ) {
        prefs(context).edit()
            .putString(KEY_CBT_URL, cbtUrl)
            .putString(KEY_API_URL, apiUrl)
            .putString(KEY_PASSWORD, password)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun updatePassword(context: Context, password: String, expiresAt: Long) {
        prefs(context).edit()
            .putString(KEY_PASSWORD, password)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun clearConfig(context: Context) {
        prefs(context).edit().clear().apply()
    }

    /** Menambahkan baris log paling baru di atas, dibatasi [MAX_LOG_LENGTH] karakter. */
    fun appendLog(context: Context, message: String) {
        val p = prefs(context)
        val current = p.getString(KEY_LOG, "") ?: ""
        val timestamp = DateFormat.format("dd/MM HH:mm:ss", System.currentTimeMillis())
        val newLog = "[$timestamp] $message\n$current"
        val trimmed = if (newLog.length > MAX_LOG_LENGTH) newLog.substring(0, MAX_LOG_LENGTH) else newLog
        p.edit().putString(KEY_LOG, trimmed).apply()
    }

    fun getLog(context: Context): String = prefs(context).getString(KEY_LOG, "") ?: ""
}
