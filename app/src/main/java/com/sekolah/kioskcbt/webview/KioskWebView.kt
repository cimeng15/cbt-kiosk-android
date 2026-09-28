package com.sekolah.kioskcbt.webview

import android.content.Context
import android.graphics.Bitmap
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

/**
 * WebView yang dikunci untuk mode ujian.
 *
 * Direkonstruksi dari `KioskWebView.java` (compiled from KioskWebView.kt).
 *
 * - JavaScript + DOM storage aktif (dibutuhkan halaman CBT).
 * - Zoom, file access, content access dimatikan.
 * - Tombol BACK ditelan (tidak bisa navigasi mundur).
 */
class KioskWebView(
    context: Context,
    private val onPageStarted: (String) -> Unit = {},
    private val onPageFinished: (String) -> Unit = {},
) : WebView(context) {

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false  // sembunyikan tombol +/- visual, pinch tetap jalan
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@KioskWebView, false)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let(onPageStarted)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let(onPageFinished)
            }
        }

        webChromeClient = WebChromeClient()
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    /** Membersihkan cookies, cache, history, form data, dan WebStorage. */
    fun clearAllData() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        clearCache(true)
        clearHistory()
        clearFormData()
        WebStorage.getInstance().deleteAllData()
    }
}
