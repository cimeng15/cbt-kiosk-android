package com.sekolah.kioskcbt

import android.app.Application

/**
 * Application class — menyimpan instance aplikasi.
 *
 * Direkonstruksi dari `KioskApp.java` (compiled from KioskApp.kt).
 */
class KioskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: KioskApp? = null

        fun get(): KioskApp =
            instance ?: throw IllegalStateException("KioskApp not initialized")
    }
}
