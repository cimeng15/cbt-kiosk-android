package com.sekolah.kioskcbt.security

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Device Admin receiver untuk mendukung Lock Task Mode.
 *
 * Direkonstruksi dari `KioskDeviceAdminReceiver.java`
 * (compiled from KioskDeviceAdminReceiver.kt).
 */
class KioskDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device Admin enabled")
        Toast.makeText(context, "Device Admin aktif", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Device Admin disabled")
        Toast.makeText(context, "Device Admin nonaktif", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Lock task mode entering: $pkg")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.i(TAG, "Lock task mode exiting")
    }

    companion object {
        private const val TAG = "KioskDeviceAdmin"
    }
}
