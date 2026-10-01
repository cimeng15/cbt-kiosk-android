package com.sekolah.kioskcbt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tema Material3 untuk SKADA EXAM.
 *
 * ── Struktur ────────────────────────────────────────────────────────────────
 * 1. [LightColors] / [DarkColors] — skema Material3. Sebelumnya skema gelap
 *    hanya mengisi 10 slot, sehingga sisanya jatuh ke ungu default Material3
 *    (inilah sumber utama tampilan dark mode yang tidak konsisten).
 * 2. [SkadaAccents] — warna permukaan & semantik brand yang tidak punya slot
 *    resmi di Material3 versi ini. Diambil lewat [skadaAccents] sehingga
 *    otomatis ikut terang/gelap.
 *
 * ── Catatan versi ───────────────────────────────────────────────────────────
 * Project ini memakai Material3 1.1.2 (Compose BOM 2023.10.01). Slot
 * `surfaceContainer*` baru ada di 1.2.0, jadi "tangga permukaan" (kartu,
 * panel, tombol nonaktif) didefinisikan di [SkadaAccents] — bukan mengandalkan
 * default Material3 yang tidak mengikuti brand kita.
 *
 * Aturan: komponen UI harus membaca warna dari tema ini. Jangan menulis
 * `Color.White` / `Color(0xFF…)` langsung di dalam screen.
 */

// ════════════════════════════════════════════════════════════════════════════
//  PALET BRAND (skema TERANG)
// ════════════════════════════════════════════════════════════════════════════

// Primary — Deep Navy
val Primary = Color(0xFF021064)
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFFDFE0FF)
val OnPrimaryContainer = Color(0xFF001159)
val InversePrimary = Color(0xFFBCC3FF)

// Secondary — Vivid Blue
val Secondary = Color(0xFF0051D5)
val OnSecondary = Color.White
val SecondaryContainer = Color(0xFFDBE1FF)
val OnSecondaryContainer = Color(0xFF001551)

// Tertiary — Emerald (dipakai sebagai "success")
val Tertiary = Color(0xFF00694B)
val OnTertiary = Color.White
val TertiaryContainer = Color(0xFF85F8C4)
val OnTertiaryContainer = Color(0xFF002115)

// Error
val Error = Color(0xFFBA1A1A)
val OnError = Color.White
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// Surface — Terang
val Surface = Color(0xFFF8F9FF)
val OnSurface = Color(0xFF0D1C2E)
val SurfaceVariant = Color(0xFFD5E3FC)
val OnSurfaceVariant = Color(0xFF454651)
val InverseSurface = Color(0xFF233144)
val InverseOnSurface = Color(0xFFEAF1FF)

// Netral / garis
val Outline = Color(0xFF767682)
val OutlineVariant = Color(0xFFC6C5D3)

// ════════════════════════════════════════════════════════════════════════════
//  AKSEN & PERMUKAAN BRAND
// ════════════════════════════════════════════════════════════════════════════

/**
 * Warna yang dipakai UI tapi tidak punya slot di Material3 1.1.2.
 *
 * `card` / `inset` / `disabledTrack` sengaja eksplisit. Di Material3 versi ini
 * permukaan bawaan tidak ada, dan kalau mengandalkan default-nya hasilnya
 * abu-abu ungu yang keluar dari identitas brand — terutama di mode gelap.
 */
@Immutable
data class SkadaAccents(
    /** Permukaan kartu & app bar (terang: putih, gelap: navy terang). */
    val card: Color,
    /** Panel di dalam kartu (kotak info). */
    val inset: Color,
    /** Latar tombol nonaktif — harus tetap terbaca di kedua tema. */
    val disabledTrack: Color,
    /** Warna "berhasil / online / siap". */
    val success: Color,
    /** Latar chip success dengan alpha rendah. */
    val successContainer: Color,
    /** Teks & ikon di atas [successContainer]. */
    val onSuccessContainer: Color,
    /** Garis tipis pemisah kartu. */
    val hairline: Color,
    /** Lapisan peredup di belakang dialog (khusus mode terang). */
    val scrim: Color,
    /** Latar tombol mengambang di atas WebView (harus tetap kontras di 2 mode). */
    val floatingControl: Color,
    /**
     * Warna tombol aksi utama ("Start Ujian").
     *
     * Sengaja terpisah dari `colorScheme.primary`: di mode gelap primary
     * adalah navy pastel (#B3C5FF) sehingga teks PUTIH di atasnya hanya
     * 1.9:1. Token ini selalu cukup gelap agar teks putih terbaca
     * (>= 4.5:1) sekaligus tetap terlihat bentuknya di atas latar
     * (>= 3:1).
     */
    val cta: Color,
    /** Aksen terang untuk gradient hero & glow. */
    val glow: Color,
)

private val LightAccents = SkadaAccents(
    card = Color(0xFFFFFFFF),
    inset = Color(0xFFE6EEFF),
    disabledTrack = Color(0xFFD5E3FC),
    // #00694B (bukan #00795A): pada chip ber-alpha 14% di atas putih,
    // #00795A hanya mencapai 4.42:1 — di bawah ambang WCAG AA.
    success = Color(0xFF00694B),
    successContainer = Color(0xFFD3F5E5),
    onSuccessContainer = Color(0xFF00553C),
    hairline = Color(0xFFE2E8F8),
    scrim = Color(0xFF0D1C2E).copy(alpha = 0.45f),
    floatingControl = Color(0xFF000000).copy(alpha = 0.65f),
    cta = Color(0xFF021064),
    glow = Color(0xFF0051D5),
)

private val DarkAccents = SkadaAccents(
    card = Color(0xFF111A33),
    inset = Color(0xFF1C2846),
    disabledTrack = Color(0xFF243152),
    success = Color(0xFF5BD9A6),
    successContainer = Color(0xFF003D2B),
    onSuccessContainer = Color(0xFF8EF7C6),
    hairline = Color(0xFF2A3852),
    // Di mode gelap latar sudah gelap — scrim cukup ditipiskan, bukan digelapkan.
    scrim = Color(0xFF050B1C).copy(alpha = 0.55f),
    floatingControl = Color(0xFF000000).copy(alpha = 0.65f),
    // Bukan primary gelap (#B3C5FF) — lihat catatan pada `cta`.
    cta = Color(0xFF1D4ED8),
    glow = Color(0xFF7FA6FF),
)

private val LocalSkadaAccents = staticCompositionLocalOf { LightAccents }

/** Akses aksen brand: `MaterialTheme.skadaAccents.success` */
val MaterialTheme.skadaAccents: SkadaAccents
    @Composable @ReadOnlyComposable get() = LocalSkadaAccents.current

// ════════════════════════════════════════════════════════════════════════════
//  COLOR SCHEMES
// ════════════════════════════════════════════════════════════════════════════

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    scrim = Color(0xFF0D1C2E),
    surfaceTint = Primary,
)

/**
 * Skema gelap navy — konsisten dengan identitas brand, bukan abu-abu netral M3.
 *
 * Latar  #080E22  → konten utama
 * Kartu  #111A33  → lebih terang dari latar, seperti di mode terang
 * Panel  #1C2846  → di dalam kartu
 *
 * Primary/secondary memakai varian pastel supaya teks navy & ikon tetap
 * kontras di atas latar gelap.
 */
private val DarkColors = darkColorScheme(
    // Primary: navy → versi pastel yang tetap terbaca di latar gelap
    primary = Color(0xFFB3C5FF),
    onPrimary = Color(0xFF001B5E),
    primaryContainer = Color(0xFF1B2C7A),
    onPrimaryContainer = Color(0xFFDCE1FF),
    inversePrimary = Color(0xFF2A3F9E),

    // Secondary: biru cerah
    secondary = Color(0xFF8FB0FF),
    onSecondary = Color(0xFF00276B),
    secondaryContainer = Color(0xFF1A3E8F),
    onSecondaryContainer = Color(0xFFDBE1FF),

    // Tertiary: emerald pastel (status "online / siap")
    tertiary = Color(0xFF5BD9A6),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF00513A),
    onTertiaryContainer = Color(0xFF8EF7C6),

    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Surface
    background = Color(0xFF080E22),
    onBackground = Color(0xFFE4E9F7),
    surface = Color(0xFF080E22),
    onSurface = Color(0xFFE4E9F7),
    surfaceVariant = Color(0xFF283450),
    onSurfaceVariant = Color(0xFFB4BDD6),

    // Garis
    outline = Color(0xFF7C88A6),
    outlineVariant = Color(0xFF33405F),
    inverseSurface = Color(0xFFE4E9F7),
    inverseOnSurface = Color(0xFF16203C),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF8FB0FF),
)

// ════════════════════════════════════════════════════════════════════════════
//  THEME
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun KioskCBTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSkadaAccents provides if (darkTheme) DarkAccents else LightAccents,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content,
        )
    }
}
