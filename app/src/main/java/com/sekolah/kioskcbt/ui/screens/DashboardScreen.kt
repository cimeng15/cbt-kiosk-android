package com.sekolah.kioskcbt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekolah.kioskcbt.R
import com.sekolah.kioskcbt.data.AppConfig
import com.sekolah.kioskcbt.data.BackendApi
import com.sekolah.kioskcbt.data.ConfigStore
import com.sekolah.kioskcbt.data.KioskSettings
import com.sekolah.kioskcbt.ui.theme.skadaAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Status results dari pengecekan server ──

/** null = sedang cek, true = OK, false = gagal */
private data class ServerStatus(
    val cbtHostOnline: Boolean? = null,  // Server ujian reachable?
    val apiPasswordOk: Boolean? = null,  // API mengembalikan password valid & belum expired?
    val apiDetail: String? = null,       // Detail info ("Kedaluwarsa", "Password kosong", dll)
    val settings: KioskSettings? = null, // Response API jika sukses
)

/**
 * Landing page / Dashboard.
 *
 * Status card melakukan 2 pengecekan REAL:
 *   1. Server Ujian — HEAD request ke CBT host, cek apakah online.
 *   2. API Password — GET ke API settings, cek password valid & belum expired.
 *
 * Tombol "MULAI CBT" di tengah-tengah layar.
 */
@Composable
fun DashboardScreen(onStartExam: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf(ServerStatus()) }

    // ── Pengecekan awal saat layar dibuka ──
    LaunchedEffect(Unit) {
        // 1. Cek server ujian (HEAD request)
        val hostOnline = BackendApi.checkCbtHost(AppConfig.CBT_HOST)
        status = status.copy(cbtHostOnline = hostOnline)

        // 2. Cek API password
        val result = withContext(Dispatchers.IO) {
            BackendApi.fetchSettings(AppConfig.API_SETTINGS_URL)
        }
        result.onSuccess { settings ->
            val passwordValid = settings.exitPassword.isNotBlank() && !settings.isExpired
            val detail = when {
                settings.isExpired -> "Kedaluwarsa"
                settings.exitPassword.isBlank() -> "Password kosong"
                else -> null
            }
            status = status.copy(
                apiPasswordOk = passwordValid,
                apiDetail = detail,
                settings = settings,
            )
        }.onFailure { e ->
            status = status.copy(
                apiPasswordOk = false,
                apiDetail = e.message,
            )
        }
    }

    // ── Apakah semua status OK? ──
    val allReady = status.cbtHostOnline == true && status.apiPasswordOk == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Gradient glow background ──
        // Memakai `glow` dari tema supaya intensitasnya ikut menyesuaikan:
        // tipis di mode terang, lebih pekat di mode gelap.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .blur(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.skadaAccents.glow.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ══════════════════════════════════
            // 1. HEADER BAR
            // ══════════════════════════════════
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.skadaAccents.card.copy(alpha = 0.9f),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.skadaAccents.success),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.dashboard_header_label),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Chip "TERKUNCI" — pakai successContainer agar kontras
                    // tetap benar di terang maupun gelap.
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.skadaAccents.successContainer,
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.skadaAccents.onSuccessContainer,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.dashboard_header_lock),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = MaterialTheme.skadaAccents.onSuccessContainer,
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════
            // 2. SCROLLABLE CONTENT
            // ══════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // ── Hero Section ──
                Spacer(Modifier.height(24.dp))

                // Logo aplikasi kecil di atas
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .blur(20.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.skadaAccents.glow.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                    // Logo punya latar putih solid: di mode gelap kotak putihnya
                    // menyala dan terlihat "nempel". Solusinya bungkus dengan
                    // container terang ber-border tipis agar menyatu dengan tema.
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                spotColor = MaterialTheme.skadaAccents.glow.copy(alpha = 0.12f),
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.skadaAccents.card,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_yayasan),
                            contentDescription = "Logo Aplikasi",
                            modifier = Modifier.padding(3.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.dashboard_badge_locked),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Judul: `primary` otomatis menjadi navy di mode terang dan
                // pastel-biru di mode gelap, jadi tidak pernah tenggelam.
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.dashboard_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )

                // Landing page hero image
                Spacer(Modifier.height(20.dp))

                Image(
                    painter = painterResource(R.drawable.landing_page),
                    contentDescription = "Landing Page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )

                // ══════════════════════════════════
                // 3. STATUS CARD — REAL VALIDATION
                // ══════════════════════════════════
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.skadaAccents.card,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.skadaAccents.hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Header: title + overall status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_status_title),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            OverallStatusBadge(allReady = allReady, isChecking = status.cbtHostOnline == null)
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider(color = MaterialTheme.skadaAccents.hairline)
                        Spacer(Modifier.height(12.dp))

                        // ── Row 1: Server Ujian ──
                        StatusRow(
                            icon = Icons.Filled.Cloud,
                            label = stringResource(R.string.dashboard_status_cbt_server),
                            isOk = status.cbtHostOnline,
                            okText = stringResource(R.string.dashboard_status_online),
                            failText = stringResource(R.string.dashboard_status_offline),
                        )

                        Spacer(Modifier.height(10.dp))

                        // ── Row 2: API Password ──
                        StatusRow(
                            icon = Icons.Filled.VpnKey,
                            label = stringResource(R.string.dashboard_status_api_password),
                            isOk = status.apiPasswordOk,
                            okText = stringResource(R.string.dashboard_status_valid),
                            failText = status.apiDetail
                                ?: stringResource(R.string.dashboard_status_invalid),
                        )

                        Spacer(Modifier.height(12.dp))

                        // Info hint
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.skadaAccents.inset,
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_info_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                // ── Error Card ──
                error?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // ══════════════════════════════════
                // 4. TOMBOL "START UJIAN" — DI TENGAH
                // ══════════════════════════════════
                Spacer(Modifier.height(32.dp))

                // Label + spinner di tengah tombol, teks putih.
                // Warna tombol diambil dari `skadaAccents.cta` (selalu navy
                // pekat) supaya teks putih tetap terbaca di mode terang
                // maupun gelap — di mode gelap `colorScheme.primary` adalah
                // navy pastel dan teks putih di atasnya tidak kontras.
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.skadaAccents.cta,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.skadaAccents.disabledTrack,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                        disabledElevation = 0.dp,
                    ),
                    onClick = {
                        error = null
                        isLoading = true
                        scope.launch {
                            // ═══ STEP 1: Cek URL ujian online ═══
                            status = status.copy(cbtHostOnline = null) // reset → spinner
                            val hostOnline = BackendApi.checkCbtHost(AppConfig.CBT_HOST)
                            status = status.copy(cbtHostOnline = hostOnline)

                            if (!hostOnline) {
                                error = "Server ujian tidak dapat dijangkau. Pastikan perangkat terhubung ke jaringan."
                                status = status.copy(apiPasswordOk = null, apiDetail = null, settings = null)
                                isLoading = false
                                return@launch
                            }

                            // ═══ STEP 2: Cek API password valid ═══
                            status = status.copy(apiPasswordOk = null) // reset → spinner
                            val result = withContext(Dispatchers.IO) {
                                BackendApi.fetchSettings(AppConfig.API_SETTINGS_URL)
                            }

                            result.onSuccess { settings ->
                                when {
                                    settings.isExpired -> {
                                        status = status.copy(
                                            apiPasswordOk = false,
                                            apiDetail = "Kedaluwarsa",
                                            settings = settings,
                                        )
                                        error = "Password di server sudah kedaluwarsa. Aktifkan di panel CBT."
                                        isLoading = false
                                    }
                                    settings.exitPassword.isBlank() -> {
                                        status = status.copy(
                                            apiPasswordOk = false,
                                            apiDetail = "Password kosong",
                                            settings = settings,
                                        )
                                        error = "Password kosong. Set password di panel CBT."
                                        isLoading = false
                                    }
                                    else -> {
                                        // ═══ STEP 3: Semua OK → simpan & masuk kiosk ═══
                                        status = status.copy(
                                            apiPasswordOk = true,
                                            apiDetail = null,
                                            settings = settings,
                                        )
                                        val cbtUrl = BackendApi.extractCbtUrl(AppConfig.API_SETTINGS_URL)
                                        withContext(Dispatchers.IO) {
                                            ConfigStore.saveConfig(
                                                context = context,
                                                cbtUrl = cbtUrl,
                                                apiUrl = AppConfig.API_SETTINGS_URL,
                                                password = settings.exitPassword,
                                                expiresAt = settings.expiresAt,
                                            )
                                            ConfigStore.appendLog(context, "Exam started: $cbtUrl")
                                        }
                                        isLoading = false
                                        onStartExam()
                                    }
                                }
                            }.onFailure { e ->
                                status = status.copy(
                                    apiPasswordOk = false,
                                    apiDetail = e.message ?: "Gagal",
                                )
                                error = "Gagal terhubung ke server: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.fetching),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    } else {
                        // Rata tengah: ikon + label sebagai satu grup.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = if (allReady) Icons.Filled.PlayArrow
                                else Icons.Filled.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.dashboard_btn_start),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ══════════════════════════════════
//  Komponen Status Card
// ══════════════════════════════════

/** Badge ringkasan di header status card: "Siap Ujian" / "Tidak Tersedia" / spinner */
@Composable
private fun OverallStatusBadge(allReady: Boolean, isChecking: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isChecking) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.dashboard_status_checking),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            val statusColor = if (allReady) {
                MaterialTheme.skadaAccents.success
            } else {
                MaterialTheme.colorScheme.error
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (allReady) stringResource(R.string.dashboard_status_ready)
                else stringResource(R.string.dashboard_status_not_ready),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = statusColor,
            )
        }
    }
}

/** Satu baris status: icon + label + badge OK/FAIL */
@Composable
private fun StatusRow(
    icon: ImageVector,
    label: String,
    isOk: Boolean?,   // null = sedang cek
    okText: String,
    failText: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kiri: icon + label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Kanan: badge
        when (isOk) {
            null -> {
                // Masih mengecek
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.dashboard_status_checking),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            true -> {
                val success = MaterialTheme.skadaAccents.success
                Surface(
                    shape = RoundedCornerShape(50),
                    color = success.copy(alpha = 0.14f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(success),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = okText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = success,
                        )
                    }
                }
            }
            false -> {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = failText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
