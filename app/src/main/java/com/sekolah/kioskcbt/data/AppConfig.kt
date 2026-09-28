package com.sekolah.kioskcbt.data

/**
 * Konstanta konfigurasi aplikasi.
 *
 * Di versi decompile, URL API di-hardcode di dalam `SetupActivityKt.DashboardScreen`:
 *   `val hardcodedApiUrl = "https://cbt.smkdata.sch.id/api/kiosk/settings"`
 *
 * Dipindahkan ke sini agar mudah diganti / diparametrisasi (mis. via BuildConfig
 * atau QR scan di masa depan).
 */
object AppConfig {

    /**
     * Base URL portal CBT. Halaman ujian akan dibuka di `cbtUrl` = protocol://host.
     */
    const val CBT_HOST = "https://cbt.smkdata.sch.id"

    /**
     * Endpoint API pengaturan kiosk.
     */
    const val API_SETTINGS_URL = "$CBT_HOST/api/kiosk/settings"
}
