package com.sekolah.kioskcbt.security

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log

/**
 * Pengelola Lock Task Mode (kiosk lock).
 *
 * Direkonstruksi dari `LockTaskManager.java` (compiled from LockTaskManager.kt).
 *
 * Jika app adalah Device Owner, package dipaksa ke lock task daftar.
 * Jika bukan, hanya screen pinning standar.
 */
class LockTaskManager(context: Context) {

    private val context: Context = context.applicationContext
    private val dpm: DevicePolicyManager =
        this.context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val am: ActivityManager =
        this.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val adminComponent: ComponentName =
        ComponentName(this.context, KioskDeviceAdminReceiver::class.java)

    val isDeviceOwner: Boolean
        get() = runCatching { dpm.isDeviceOwnerApp(context.packageName) }.getOrDefault(false)

    val isInLockTaskMode: Boolean
        get() = runCatching { am.isInLockTaskMode }.getOrDefault(false)

    /** Memulai lock task. Kembalikan `true` bila sukses. */
    fun startLockTask(activity: Activity): Boolean = try {
        if (isDeviceOwner && !isInLockTaskMode) {
            dpm.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
            Log.i(TAG, "Lock task packages set (Device Owner)")
        }
        if (!isInLockTaskMode) {
            activity.startLockTask()
            Log.i(TAG, "Lock task started (${if (isDeviceOwner) "Device Owner" else "standard"})")
        }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start lock task: ${e.message}")
        false
    }

    /** Menghentikan lock task. Kembalikan `true` bila sukses. */
    fun stopLockTask(activity: Activity): Boolean = try {
        if (isInLockTaskMode) {
            activity.stopLockTask()
            Log.i(TAG, "Lock task stopped")
        }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to stop lock task: ${e.message}")
        false
    }

    companion object {
        private const val TAG = "LockTaskManager"
    }
}
