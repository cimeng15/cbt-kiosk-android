package com.sekolah.kioskcbt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * HTTP client minimal untuk backend kiosk.
 *
 * Direkonstruksi dari `BackendApi.java` (compiled from BackendApi.kt).
 *
 * Catatan: versi asli memakai `HttpURLConnection` langsung. Eventual hardening
 * dapat diganti ke OkHttp + CertificatePinner (temuan keamanan: tanpa cert pinning).
 *
 * Bug pada versi asli: timeout memakai konstanta ML Kit `ModuleDescriptor.MODULE_VERSION`.
 * Di sini diganti dengan konstanta eksplisit yang benar.
 */
object BackendApi {

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 10_000

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    }

    /**
     * Mengambil pengaturan kiosk dari [apiUrl].
     *
     * Endpoint: `GET {apiUrl}` dengan `Accept: application/json`.
     * Response: `{ "success": true, "data": { "exit_password", "password_expires_at", "is_expired" } }`
     */
    suspend fun fetchSettings(apiUrl: String): Result<KioskSettings> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }
            try {
                val code = conn.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    error("Server error: $code")
                }
                val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) {
                    error("API return success=false")
                }
                val data = json.optJSONObject("data") ?: error("Data kosong dari API")
                val password = data.optString("exit_password", "")
                val expiresStr = data.optString("password_expires_at", "")
                val isExpired = data.optBoolean("is_expired", false)
                KioskSettings(
                    exitPassword = password,
                    expiresAt = parseExpiry(expiresStr),
                    isExpired = isExpired,
                )
            } finally {
                conn.disconnect()
            }
        }.recoverCatching { e ->
            throw Exception("Gagal fetch: ${e.message}", e)
        }
    }

    /** Parse `yyyy-MM-dd'T'HH:mm` (Asia/Jakarta) menjadi epoch millis. 0 jika gagal. */
    fun parseExpiry(expiresStr: String): Long =
        runCatching { dateFormat.parse(expiresStr)?.time ?: 0L }.getOrDefault(0L)

    /** Mengambil `protocol://host` dari URL API; jika gagal, kembalikan [apiUrl] apa adanya. */
    fun extractCbtUrl(apiUrl: String): String =
        runCatching {
            val url = URL(apiUrl)
            "${url.protocol}://${url.host}"
        }.getOrDefault(apiUrl)
}
