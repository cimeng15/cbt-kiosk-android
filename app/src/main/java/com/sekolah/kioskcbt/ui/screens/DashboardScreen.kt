package com.sekolah.kioskcbt.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowForward
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
import com.sekolah.kioskcbt.ui.theme.InversePrimary
import com.sekolah.kioskcbt.ui.theme.OnTertiaryContainer
import com.sekolah.kioskcbt.ui.theme.Primary
import com.sekolah.kioskcbt.ui.theme.Secondary
import com.sekolah.kioskcbt.ui.theme.SurfaceContainerLow
import com.sekolah.kioskcbt.ui.theme.SurfaceContainerLowest
import com.sekolah.kioskcbt.ui.theme.TertiaryFixed
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
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // ── Gradient blur background ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .blur(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Secondary.copy(alpha = 0.10f),
                            Primary.copy(alpha = 0.05f),
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
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
                                .background(OnTertiaryContainer),
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
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = TertiaryFixed,
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = OnTertiaryContainer,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.dashboard_header_lock),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = OnTertiaryContainer,
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
                Spacer(Modifier.height(28.dp))

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .blur(20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Secondary.copy(alpha = 0.20f)),
                    )
                    Image(
                        painter = painterResource(R.drawable.logo_yayasan),
                        contentDescription = "Logo SMK",
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Secondary.copy(alpha = 0.10f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Secondary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.dashboard_badge_locked),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                            ),
                            color = Secondary,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = Primary,
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

                // ══════════════════════════════════
                // 3. STATUS CARD — REAL VALIDATION
                // ══════════════════════════════════
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        Divider(color = SurfaceContainerLow)
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
                            color = SurfaceContainerLow,
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Primary,
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
                // 4. TOMBOL "MULAI CBT" — DI TENGAH
                // ══════════════════════════════════
                Spacer(Modifier.height(32.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White,
                        disabledContainerColor = Primary.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f),
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                    ),
                    onClick = {
                        error = null
                        isLoading = true
                        scope.launch {
                            // Gunakan settings yang sudah di-fetch jika valid,
                            // kalau tidak fetch ulang.
                            val cachedSettings = status.settings
                            val result = if (cachedSettings != null
                                && !cachedSettings.isExpired
                                && cachedSettings.exitPassword.isNotBlank()
                            ) {
                                Result.success(cachedSettings)
                            } else {
                                withContext(Dispatchers.IO) {
                                    BackendApi.fetchSettings(AppConfig.API_SETTINGS_URL)
                                }
                            }

                            result.onSuccess { settings ->
                                if (settings.isExpired) {
                                    error = "Password di server sudah kedaluwarsa. Aktifkan di panel CBT."
                                    isLoading = false
                                } else if (settings.exitPassword.isBlank()) {
                                    error = "Password kosong. Set password di panel CBT."
                                    isLoading = false
                                } else {
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
                            }.onFailure { e ->
                                error = e.message
                                isLoading = false
                            }
                        }
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_btn_start),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                    ),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.10f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 10.dp, top = 4.dp, end = 8.dp, bottom = 4.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(R.string.dashboard_btn_enter),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InversePrimary,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White,
                                    )
                                }
                            }
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
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (allReady) OnTertiaryContainer else MaterialTheme.colorScheme.error),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (allReady) stringResource(R.string.dashboard_status_ready)
                else stringResource(R.string.dashboard_status_not_ready),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (allReady) OnTertiaryContainer else MaterialTheme.colorScheme.error,
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
                Surface(
                    shape = RoundedCornerShape(50),
                    color = OnTertiaryContainer.copy(alpha = 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(OnTertiaryContainer),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = okText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnTertiaryContainer,
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
