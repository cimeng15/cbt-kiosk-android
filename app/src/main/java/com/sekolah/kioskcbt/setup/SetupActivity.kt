package com.sekolah.kioskcbt.setup

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sekolah.kioskcbt.kiosk.KioskActivity
import com.sekolah.kioskcbt.ui.screens.DashboardScreen
import com.sekolah.kioskcbt.ui.theme.KioskCBTTheme

/**
 * Setup / dashboard activity.
 *
 * Direkonstruksi dari `SetupActivity.java` (SetupActivity.kt).
 * UI berada di [DashboardScreen] (paket `ui.screens`) agar mudah diganti.
 */
class SetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            KioskCBTTheme {
                DashboardScreen(onStartExam = { startKiosk() })
            }
        }
    }

    private fun startKiosk() {
        startActivity(Intent(this, KioskActivity::class.java))
        finish()
    }
}
