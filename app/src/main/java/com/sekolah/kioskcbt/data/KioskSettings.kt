package com.sekolah.kioskcbt.data

/**
 * Response pengaturan dari backend (lihat [BackendApi.fetchSettings]).
 *
 * Direkonstruksi dari `KioskSettings.java` (compiled from BackendApi.kt).
 *
 * @param exitPassword Password keluar dari server.
 * @param expiresAt    Timestamp (millis) hasil parsing `password_expires_at`.
 * @param isExpired    Flag kedaluwarsa sesuai kata server.
 */
data class KioskSettings(
    val exitPassword: String,
    val expiresAt: Long,
    val isExpired: Boolean,
)
