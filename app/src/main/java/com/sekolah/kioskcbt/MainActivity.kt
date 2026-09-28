package com.sekolah.kioskcbt

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.sekolah.kioskcbt.data.BackendApi
import com.sekolah.kioskcbt.data.ConfigStore
import com.sekolah.kioskcbt.setup.SetupActivity
import com.sekolah.kioskcbt.ui.screens.SplashScreen
import com.sekolah.kioskcbt.ui.theme.KioskCBTTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Splash screen + bootstrap.
 *
 * Direkonstruksi dari `MainActivity.java` + `MainActivityKt.java` (MainActivity.kt).
 *
 * Alur:
 *  - Tampilkan splash 1.5s.
 *  - Jika config tersimpan ada: fetch password terbaru dari API (fallback ke cache).
 *  - Lanjut ke [SetupActivity].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            KioskCBTTheme {
                var status by remember { mutableStateOf("Memuat…") }
                val scope = rememberCoroutineScope()

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    delay(1500L)
                    val config = withContext(Dispatchers.IO) { ConfigStore.getConfig(this@MainActivity) }
                    if (config == null) {
                        goToSetup()
                    } else {
                        status = "Menghubungi server…"
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                BackendApi.fetchSettings(config.apiUrl)
                            }
                            result.onSuccess { settings ->
                                withContext(Dispatchers.IO) {
                                    ConfigStore.updatePassword(
                                        this@MainActivity,
                                        settings.exitPassword,
                                        settings.expiresAt,
                                    )
                                    ConfigStore.appendLog(this@MainActivity, "Password refreshed from API")
                                }
                            }.onFailure { e ->
                                withContext(Dispatchers.IO) {
                                    ConfigStore.appendLog(
                                        this@MainActivity,
                                        "API fetch failed, using cached: ${e.message}",
                                    )
                                }
                            }
                            goToSetup()
                        }
                    }
                }

                SplashScreen(status)
            }
        }
    }

    private fun goToSetup() {
        startActivity(Intent(this, SetupActivity::class.java))
        finish()
    }
}
