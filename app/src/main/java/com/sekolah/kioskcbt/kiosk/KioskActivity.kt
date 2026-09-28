package com.sekolah.kioskcbt.kiosk

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sekolah.kioskcbt.data.ConfigStore
import com.sekolah.kioskcbt.security.LockTaskManager
import com.sekolah.kioskcbt.setup.SetupActivity
import com.sekolah.kioskcbt.ui.screens.KioskScreen
import com.sekolah.kioskcbt.ui.screens.UnlockScreen
import com.sekolah.kioskcbt.ui.theme.KioskCBTTheme

/**
 * Activity mode ujian terkunci.
 *
 * Anti-escape measures:
 *  - Lock Task Mode (Device Owner / standard pinning)
 *  - FLAG_SECURE + immersive sticky fullscreen
 *  - excludeFromRecents → tidak muncul di recent apps
 *  - onPause → langsung bring back to front (anti gesture nav)
 *  - onStop → re-launch diri sendiri jika bukan unlock
 *  - onKeyDown → blokir HOME, BACK, APP_SWITCH
 *  - onBackPressed → diblokir
 *  - onUserLeaveHint → re-focus
 *  - onWindowFocusChanged → re-focus
 */
class KioskActivity : ComponentActivity() {

    private lateinit var lockTaskManager: LockTaskManager
    private val handler = Handler(Looper.getMainLooper())

    private var showUnlock by mutableStateOf(false)
    private var configUrl by mutableStateOf("")
    private var isExiting = false  // flag supaya onPause/onStop tidak fight saat keluar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySecureFlags()
        applyImmersiveMode()

        val config = ConfigStore.getConfig(this)
        if (config == null) {
            Toast.makeText(this, "Konfigurasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            isExiting = true
            finish()
            return
        }
        configUrl = config.cbtUrl
        lockTaskManager = LockTaskManager(this)

        setContent {
            KioskCBTTheme {
                if (showUnlock) {
                    UnlockScreen(
                        onSuccess = { handleUnlockSuccess() },
                        onCancel = { showUnlock = false },
                    )
                } else {
                    KioskScreen(
                        url = configUrl,
                        onUnlockClick = { showUnlock = true },
                    )
                }
            }
        }

        ConfigStore.appendLog(this, "Kiosk started: $configUrl")
        startLockTaskWithRetry()
    }

    // ═══════════════════════════════════
    //  ANTI-ESCAPE: Gesture Navigation
    // ═══════════════════════════════════

    /**
     * onPause dipanggil saat gesture swipe-up mulai (recent apps / home).
     * Kita langsung paksa kembali ke foreground.
     */
    override fun onPause() {
        super.onPause()
        if (!isExiting && !showUnlock) {
            bringBackToFront()
        }
    }

    /**
     * onStop dipanggil jika activity benar-benar kehilangan visibility.
     * Fail-safe: re-launch activity ini.
     */
    override fun onStop() {
        super.onStop()
        if (!isExiting && !showUnlock) {
            handler.postDelayed({
                if (!isFinishing && !isExiting) {
                    bringBackToFront()
                }
            }, 100L)
        }
    }

    /** Paksa activity ini ke depan. */
    private fun bringBackToFront() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // moveTaskToFront — memerlukan REORDER_TASKS tapi sudah implicit untuk own task
            am.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        } catch (_: Exception) {
            // Fallback: re-launch via intent
            val intent = Intent(this, KioskActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
        // Re-apply immersive setelah kembali
        handler.postDelayed({ applyImmersiveMode() }, 200L)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Saat di-relaunch, pastikan immersive mode aktif
        applyImmersiveMode()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isExiting) {
            ConfigStore.appendLog(this, "Exit attempt (Home/Gesture) — blocked")
            bringBackToFront()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !showUnlock && !isExiting) {
            handler.postDelayed({
                if (!isFinishing && !isExiting) {
                    applyImmersiveMode()
                    bringBackToFront()
                }
            }, 100L)
        }
        if (hasFocus) {
            applyImmersiveMode()
        }
    }

    // ═══════════════════════════════════
    //  WINDOW FLAGS & IMMERSIVE
    // ═══════════════════════════════════

    private fun applySecureFlags() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
        // Cegah task muncul di recent apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
    }

    private fun applyImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // ═══════════════════════════════════
    //  LOCK TASK
    // ═══════════════════════════════════

    private fun startLockTaskWithRetry() {
        handler.postDelayed({
            if (!lockTaskManager.isInLockTaskMode) {
                if (!lockTaskManager.startLockTask(this)) {
                    handler.postDelayed({ startLockTaskWithRetry() }, 1000L)
                }
            }
        }, 500L)
    }

    // ═══════════════════════════════════
    //  UNLOCK & EXIT
    // ═══════════════════════════════════

    private fun handleUnlockSuccess() {
        isExiting = true  // Stop semua anti-escape
        showUnlock = false
        ConfigStore.appendLog(this, "Unlocked by admin")
        clearWebData()
        lockTaskManager.stopLockTask(this)
        startActivity(
            Intent(this, SetupActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        finish()
    }

    private fun clearWebData() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
        ConfigStore.appendLog(this, "Web data cleared (forced exit)")
    }

    // ═══════════════════════════════════
    //  KEY BLOCKING
    // ═══════════════════════════════════

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_APP_SWITCH,
        KeyEvent.KEYCODE_MENU,
        KeyEvent.KEYCODE_SEARCH,
        KeyEvent.KEYCODE_ASSIST,
        KeyEvent.KEYCODE_VOICE_ASSIST,
        -> {
            ConfigStore.appendLog(this, "Blocked key: $keyCode")
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        ConfigStore.appendLog(this, "Back pressed (blocked)")
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val TAG = "KioskActivity"
    }
}
