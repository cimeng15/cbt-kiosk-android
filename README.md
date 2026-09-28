# CBT Kiosk Android (CBT SMKDT)

Aplikasi **Kiosk Browser untuk ujian CBT (Computer Based Test)** sekolah. Perangkat
Android dikunci dalam mode kiosk saat ujian berlangsung — siswa hanya bisa mengakses
halaman ujian, tidak bisa keluar, membuka aplikasi lain, atau menekan tombol navigasi.
Guru membuka kunci dengan password yang di-fetch dari backend.

> Package: `com.sekolah.kioskcbt` · Min SDK 26 (Android 8.0) · Target SDK 34 (Android 14)

---

## Fitur

- **Splash / bootstrap** — cek config + refresh password dari API saat startup.
- **Landing page / Dashboard** — status perangkat & server, tombol "MULAI CBT".
- **Kiosk mode terkunci** — WebView fullscreen dengan banyak layer anti-escape.
- **Unlock via password** — maks 3 percobaan, password dari backend + expiry.
- **Zoom (pinch-to-zoom)** dan **pull-to-refresh** di halaman ujian.
- **Anti screenshot** (`FLAG_SECURE`).

## Arsitektur

Tiga Activity dengan alur linear:

```
MainActivity (Splash)  ─▶  SetupActivity (Dashboard)  ─▶  KioskActivity (Exam Lock)
     │                                                          │
     │ config ada? ── ya ─▶ fetch API ──┐                       │
     │ config null? ──▶ Setup           │                       │
     └──────────────────────────────────┘              unlock (password) ─▶ Setup
```

### Struktur source

```
app/src/main/java/com/sekolah/kioskcbt/
├── KioskApp.kt                    # Application singleton
├── MainActivity.kt                # Splash + refresh password dari API
├── data/
│   ├── AppConfig.kt               # URL API terpusat
│   ├── BackendApi.kt              # HTTP client (HttpURLConnection)
│   ├── ConfigStore.kt             # SharedPreferences + activity log
│   ├── KioskConfig.kt             # Data class config lokal
│   └── KioskSettings.kt           # Data class response API
├── security/
│   ├── LockTaskManager.kt         # Lock Task Mode handler
│   └── KioskDeviceAdminReceiver.kt
├── webview/
│   └── KioskWebView.kt            # WebView terkunci (zoom + anti-back)
├── setup/
│   └── SetupActivity.kt
├── kiosk/
│   └── KioskActivity.kt           # Kiosk mode + multi-layer anti-escape
└── ui/
    ├── theme/Theme.kt             # Material3 theme (navy/indigo)
    └── screens/
        ├── SplashScreen.kt
        ├── DashboardScreen.kt     # Landing page
        ├── KioskScreen.kt         # WebView + exit button + pull-to-refresh
        └── UnlockScreen.kt        # Dialog password
```

---

## Layer Anti-Escape (Kiosk Lock)

```
Layer 1: Lock Task Mode            (Android system level; penuh bila Device Owner)
Layer 2: excludeFromRecents        (tidak tampil di recent apps)
Layer 3: onPause → moveTaskToFront (gesture swipe-up → app paksa kembali)
Layer 4: onStop → re-launch        (failsafe)
Layer 5: onUserLeaveHint → refocus
Layer 6: onWindowFocusChanged → refocus
Layer 7: onKeyDown → blokir HOME/BACK/APP_SWITCH/MENU/ASSIST
Layer 8: Immersive sticky mode     (system bars tersembunyi)
```

> Tanpa **Device Owner**, beberapa layer bisa lolos pada ROM tertentu (MIUI/OneUI, dll).
> Dengan Device Owner (`adb shell dpm set-device-owner com.sekolah.kioskcbt/.security.KioskDeviceAdminReceiver`),
> semua layer aktif penuh dan perangkat benar-benar terkunci.

---

## API Contract

```http
GET https://cbt.smkdata.sch.id/api/kiosk/settings
Accept: application/json
```

```json
{
  "success": true,
  "data": {
    "exit_password": "string",
    "password_expires_at": "2026-09-28T14:30",
    "is_expired": false
  }
}
```

- Format waktu: `yyyy-MM-dd'T'HH:mm` (timezone **Asia/Jakarta**).
- URL dapat diganti di `data/AppConfig.kt`.

---

## Build

Prasyarat: **JDK 17**, **Android SDK 34**.

```bash
# set path SDK
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# build debug
./gradlew :app:assembleDebug

# hasil APK
# app/build/outputs/apk/debug/app-debug.apk
```

Install ke device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Catatan

- Ini adalah rekonstruksi ulang source Kotlin dari APK hasil decompile, dengan
  perbaikan keamanan (non-debuggable release, timeout API diperbaiki, device admin
  di-harden) dan UI landing page yang diperbarui.
- Kredensial: file `local.properties`, keystore (`*.jks`/`*.keystore`) **tidak**
  di-commit (lihat `.gitignore`).
