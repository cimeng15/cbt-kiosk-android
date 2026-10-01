package com.sekolah.kioskcbt.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sekolah.kioskcbt.ui.theme.skadaAccents
import com.sekolah.kioskcbt.webview.KioskWebView

/**
 * Layar ujian: WebView fullscreen + swipe-to-refresh + tombol exit kecil.
 *
 * Perubahan dari versi sebelumnya:
 *  - Tombol exit: pojok kanan ATAS, opacity 60%, hanya icon lock
 *  - Zoom: diizinkan (pinch-to-zoom)
 *  - Reload: swipe-to-refresh (tarik ke bawah dari atas)
 *  - Tidak ada button lain selain exit
 */
@Composable
fun KioskScreen(
    url: String,
    onUnlockClick: () -> Unit,
) {
    var webViewRef by remember { mutableStateOf<KioskWebView?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Warna diambil dari tema di level @Composable (factory AndroidView bukan
    // konteks composable, jadi tidak boleh memanggil MaterialTheme di dalamnya).
    val refreshColor = MaterialTheme.colorScheme.primary.toArgb()
    val refreshTrack = MaterialTheme.skadaAccents.inset.toArgb()
    val exitScrim = MaterialTheme.skadaAccents.scrim

    Box(modifier = Modifier.fillMaxSize()) {

        // ── WebView di dalam SwipeRefreshLayout untuk pull-to-refresh ──
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val webView = KioskWebView(
                    context = ctx,
                    onPageStarted = { isRefreshing = true },
                    onPageFinished = { isRefreshing = false },
                ).apply {
                    loadUrl(url)
                }
                webViewRef = webView

                SwipeRefreshLayout(ctx).apply {
                    addView(
                        webView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ),
                    )
                    setOnRefreshListener {
                        webView.reload()
                    }
                    // Sinkronkan state refresh indicator
                    setColorSchemeColors(refreshColor)
                    setProgressBackgroundColorSchemeColor(refreshTrack)
                }
            },
            update = { swipeLayout ->
                swipeLayout.isRefreshing = isRefreshing
            },
        )

        // ── Tombol Exit: pojok kanan atas, opacity 60%, icon saja ──
        // Chip ini menumpuk di atas WebView yang warnanya tidak bisa diprediksi,
        // jadi sengaja memakai scrim gelap + ikon putih: kontrasnya terjamin
        // di light maupun dark mode.
        IconButton(
            onClick = onUnlockClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp)
                .alpha(0.6f)
                .size(36.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = exitScrim,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Exit",
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
