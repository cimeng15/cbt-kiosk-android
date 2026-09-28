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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.sekolah.kioskcbt.data.KioskConfig
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

/**
 * Landing page / Dashboard — desain baru sesuai referensi HTML.
 *
 * Elemen-elemen:
 *  1. Header bar (CBT EXAM + badge TERKUNCI)
 *  2. Hero section (logo + blur glow + badge + judul + subjudul)
 *  3. Status card (Perangkat & Server)
 *  4. Tombol CTA "MULAI CBT" (fetch → validate → kiosk)
 *  5. Error card jika ada masalah
 *
 * Logo memakai `logo_yayasan.png` bawaan APK asli.
 */
@Composable
fun DashboardScreen(onStartExam: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var existingConfig by remember { mutableStateOf<KioskConfig?>(null) }
    var serverReachable by remember { mutableStateOf<Boolean?>(null) }

    // Muat config tersimpan + cek koneksi awal
    LaunchedEffect(Unit) {
        existingConfig = withContext(Dispatchers.IO) { ConfigStore.getConfig(context) }
        // Cek server reachable (background, ringan)
        val result = withContext(Dispatchers.IO) {
            BackendApi.fetchSettings(AppConfig.API_SETTINGS_URL)
        }
        serverReachable = result.isSuccess
    }

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
                    // Kiri: dot + "CBT EXAM"
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

                    // Kanan: badge "TERKUNCI"
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
            // 2. CONTENT
            // ══════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {

                // ── Hero Section ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Logo dengan glow effect
                    Box(contentAlignment = Alignment.Center) {
                        // Blur glow di belakang logo
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

                    // Badge "SISTEM UJIAN TERKUNCI"
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

                    // Judul utama
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

                    // Subtitle
                    Text(
                        text = stringResource(R.string.dashboard_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }

                // ── Status Card + Error ──
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceContainerLowest,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Status title + indicator
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (serverReachable == true) OnTertiaryContainer
                                                else if (serverReachable == false) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.outline,
                                            ),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (serverReachable == true)
                                            stringResource(R.string.dashboard_status_ready)
                                        else if (serverReachable == false)
                                            stringResource(R.string.dashboard_status_not_ready)
                                        else "Mengecek…",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = if (serverReachable == true) OnTertiaryContainer
                                        else if (serverReachable == false) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }

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

                    Spacer(Modifier.height(12.dp))

                    // Error Card (muncul jika ada error)
                    error?.let { message ->
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
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // ── CTA Button ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                        ),
                        onClick = {
                            error = null
                            isLoading = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    BackendApi.fetchSettings(AppConfig.API_SETTINGS_URL)
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
                                // Kiri: ikon play + text
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

                                // Kanan: pill "Masuk Ujian →"
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.10f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            start = 10.dp,
                                            top = 4.dp,
                                            end = 8.dp,
                                            bottom = 4.dp,
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
                }
            }
        }
    }
}
