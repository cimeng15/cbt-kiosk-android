package com.sekolah.kioskcbt.data

/**
 * Konfigurasi kiosk yang disimpan lokal (lihat [ConfigStore]).
 *
 * Direkonstruksi dari `KioskConfig.java` (compiled from ConfigStore.kt).
 *
 * @param cbtUrl       URL halaman ujian (protocol + host, tanpa path).
 * @param apiUrl       URL API backend lengkap.
 * @param exitPassword Password untuk keluar dari mode kiosk.
 * @param expiresAt    Timestamp (millis) kapan password kedaluwarsa.
 */
data class KioskConfig(
    val cbtUrl: String,
    val apiUrl: String,
    val exitPassword: String,
    val expiresAt: Long,
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}
