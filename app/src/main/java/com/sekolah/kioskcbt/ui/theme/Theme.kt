package com.sekolah.kioskcbt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tema Material3 untuk Kiosk CBT — diperbarui sesuai referensi desain HTML.
 *
 * Palette utama: Navy/Indigo (#021064 primary, #0051D5 secondary, #35AE7F tertiary).
 * Surface: terang (#F8F9FF) dengan accent indigo.
 */

// ─── Warna Palette (dari referensi HTML Tailwind config) ───

// Primary (Deep Navy)
val Primary = Color(0xFF021064)
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFF1E2A78)
val OnPrimaryContainer = Color(0xFF8A95E9)
val PrimaryFixed = Color(0xFFDFE0FF)
val InversePrimary = Color(0xFFBCC3FF)

// Secondary (Vivid Blue)
val Secondary = Color(0xFF0051D5)
val OnSecondary = Color.White
val SecondaryContainer = Color(0xFF316BF3)
val OnSecondaryContainer = Color(0xFFFEFCFF)
val SecondaryFixed = Color(0xFFDBE1FF)

// Tertiary (Green/Emerald)
val Tertiary = Color(0xFF002316)
val OnTertiary = Color.White
val TertiaryContainer = Color(0xFF003B27)
val OnTertiaryContainer = Color(0xFF35AE7F)
val TertiaryFixed = Color(0xFF85F8C4)

// Error
val Error = Color(0xFFBA1A1A)
val OnError = Color.White
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// Surface (Light)
val Surface = Color(0xFFF8F9FF)
val OnSurface = Color(0xFF0D1C2E)
val SurfaceVariant = Color(0xFFD5E3FC)
val OnSurfaceVariant = Color(0xFF454651)
val SurfaceContainer = Color(0xFFE6EEFF)
val SurfaceContainerLow = Color(0xFFEFF4FF)
val SurfaceContainerHigh = Color(0xFFDCE9FF)
val SurfaceContainerHighest = Color(0xFFD5E3FC)
val SurfaceContainerLowest = Color.White
val SurfaceBright = Color(0xFFF8F9FF)
val SurfaceDim = Color(0xFFCCDBF3)

// Neutrals
val Outline = Color(0xFF767682)
val OutlineVariant = Color(0xFFC6C5D3)
val InverseSurface = Color(0xFF233144)
val InverseOnSurface = Color(0xFFEAF1FF)


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
    surfaceTint = Color(0xFF4C58A6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = TertiaryFixed,
    onTertiary = Color(0xFF003D29),
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF454651),
)

@Composable
fun KioskCBTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
