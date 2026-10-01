package com.sekolah.kioskcbt.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekolah.kioskcbt.R

/**
 * Splash / bootstrap screen.
 *
 * Latar tetap brand navy di kedua tema (memang identitas visual), tetapi
 * diperhalus:
 *  - gradient dua arah, bukan warna flat
 *  - glow radial lembut di belakang logo
 *  - logo dibungkus container ber-border & ber-shadow supaya kotak putihnya
 *    tidak terlihat "nempel" seperti stiker
 *  - hierarki tipografi (nama app → tagline → status → footer)
 *
 * Nilai gradient sedikit berbeda antara terang & gelap agar transisi ke
 * layar berikutnya tidak terasa "melompat".
 *
 * @param status Teks status yang ditampilkan di bawah indikator loading.
 */
@Composable
fun SplashScreen(status: String) {
    val dark = isSystemInDarkTheme()
    val topColor = if (dark) Color(0xFF0C1A44) else Color(0xFF0A2A9E)
    val bottomColor = if (dark) Color(0xFF050B1C) else Color(0xFF021064)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bottomColor,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Gradient vertikal ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(topColor, bottomColor)),
                    ),
            )

            // ── Glow radial di belakang logo ──
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-90).dp)
                    .size(340.dp)
                    .blur(70.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))

                // ── Logo dalam kartu putih membulat + shadow ──
                // Sengaja tetap putih di kedua tema: logo adalah JPEG berlatar
                // putih, jadi latarnya harus putih agar tidak terlihat kotak.
                Surface(
                    modifier = Modifier
                        .size(104.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.35f),
                            spotColor = Color.Black.copy(alpha = 0.30f),
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_yayasan),
                        contentDescription = null,
                        modifier = Modifier.padding(9.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(Modifier.size(24.dp))

                Text(
                    text = stringResource(R.string.app_full_name),
                    color = Color.White.copy(alpha = 0.97f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )

                Spacer(Modifier.size(6.dp))

                Text(
                    text = stringResource(R.string.splash_caption),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    letterSpacing = 1.6.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1.1f))

                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    strokeWidth = 2.5.dp,
                )

                Spacer(Modifier.size(14.dp))

                Text(
                    text = status,
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(0.35f))

                Text(
                    text = stringResource(R.string.splash_footer),
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}
