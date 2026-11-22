package id.cekendaraan.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R

/**
 * Neon Cockpit design tokens from docs/DESIGN.md.
 * All screen files must consume these tokens instead of hard-coded colors.
 */
object C {
    // Exact tokens from DESIGN.md
    val bg = Color(0xFF060A10)
    val bgTop = Color(0xFF0E2332)
    val s1 = Color(0xFF0C141D)
    val s2 = Color(0xFF111C28)
    val s3 = Color(0xFF1A2A3B)
    val t1 = Color(0xFFF2F8FC)
    val t2 = Color(0xFF9AB0C2)
    val t3 = Color(0xFF5D7385)
    val acc = Color(0xFF22D3EE)
    val acc2 = Color(0xFF7C8CFF)
    val ink = Color(0xFF031820)
    val ok = Color(0xFF34D399)
    val warn = Color(0xFFFBBF24)
    val bad = Color(0xFFF87171)

    // Token helpers
    val div = Color(0x0FF2F8FC) // t1 at 6% alpha for card dividers
    val cardHighlight = Color(0x2422D3EE) // 14% accent

    // Aliases to maintain backwards compatibility across screens
    @get:JvmName("getLegacyBg")
    val Bg = bg
    val Surface = s1
    val Surface2 = s2
    val Surface3 = s3
    val Field = s1
    val Inset = s1
    val Line = s2
    val LineStrong = s3
    val Edge = Color(0x3322D3EE)

    val Blue = acc
    val BlueButton = acc
    val BlueChip = Color(0xFF0B3B4A)
    val BlueSoft = acc.copy(alpha = 0.15f)
    val BlueDeep = s1
    val BlueGlow = acc.copy(alpha = 0.28f)
    val Violet = acc2
    val OnAccent = ink

    val Green = ok
    val GreenSoft = ok.copy(alpha = 0.15f)
    val Amber = warn
    val AmberSoft = warn.copy(alpha = 0.15f)
    val Red = bad
    val RedSoft = bad.copy(alpha = 0.15f)
    val Star = warn
    val Purple = acc2

    val Text = t1
    val Muted = t2
    val Muted2 = t3
    val Segment = s2
    val Chrome = t1

    val Lime = acc
    val LimeDeep = acc
    val LimeSoft = acc.copy(alpha = 0.15f)
    val OnLime = ink
}

/**
 * 4 standard gradients defined in docs/DESIGN.md:
 * 1. Aksen: 135° linear (acc -> acc2)
 * 2. Glow: radial accent or status glow
 * 3. Sorot status: horizontal gradient (status color 14-18% -> s1 at 75%)
 * 4. Langit: 180° vertical gradient (bgTop -> bg at 300dp)
 */
object Gradients {
    val Accent = Brush.linearGradient(
        colors = listOf(C.acc, C.acc2),
        start = Offset.Zero,
        end = Offset(500f, 500f),
    )

    fun statusGlow(color: Color) = Brush.radialGradient(
        colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.08f), Color.Transparent),
    )

    val Sky = Brush.verticalGradient(
        colors = listOf(C.bgTop, C.bg),
        startY = 0f,
        endY = 900f,
    )

    fun statusHighlight(color: Color) = Brush.horizontalGradient(
        0.0f to color.copy(alpha = 0.16f),
        0.75f to C.s1,
    )
}

val Manrope = FontFamily(
    Font(R.font.manrope, weight = FontWeight.W500),
    Font(R.font.manrope, weight = FontWeight.W600),
    Font(R.font.manrope, weight = FontWeight.W700),
)

/** Monospaced digits fallback. Prefer Type styles which have fontFeatureSettings = "tnum". */
val Digits = Manrope

/**
 * Neon Cockpit typography strictly defined in docs/DESIGN.md.
 * Weights are strictly limited to 500 (Medium), 600 (SemiBold), 700 (Bold).
 */
object Type {
    val Display = TextStyle(
        fontFamily = Manrope,
        fontSize = 40.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.035).em,
        fontFeatureSettings = "tnum",
        color = C.t1,
    )
    val DisplayRing = Display.copy(fontSize = 36.sp)
    val H1 = TextStyle(
        fontFamily = Manrope,
        fontSize = 24.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.025).em,
        color = C.t1,
    )
    val H2 = TextStyle(
        fontFamily = Manrope,
        fontSize = 17.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.015).em,
        color = C.t1,
    )
    val Stat = TextStyle(
        fontFamily = Manrope,
        fontSize = 19.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = "tnum",
        color = C.t1,
    )
    val Item = TextStyle(
        fontFamily = Manrope,
        fontSize = 15.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = (-0.01).em,
        color = C.t1,
    )
    val Small = TextStyle(
        fontFamily = Manrope,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.em,
        fontFeatureSettings = "tnum",
        color = C.t1,
    )
    val Body = TextStyle(
        fontFamily = Manrope,
        fontSize = 14.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.em,
        lineHeight = 21.7.sp,
        color = C.t2,
    )
    val Meta = TextStyle(
        fontFamily = Manrope,
        fontSize = 13.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.em,
        lineHeight = 18.2.sp,
        color = C.t2,
    )
    val Label = TextStyle(
        fontFamily = Manrope,
        fontSize = 12.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.01.em,
        color = C.t3,
    )
    val Caption = TextStyle(
        fontFamily = Manrope,
        fontSize = 12.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.em,
        color = C.t3,
    )
    val Button = TextStyle(
        fontFamily = Manrope,
        fontSize = 15.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = 0.em,
        color = C.ink,
    )
    val Nav = TextStyle(
        fontFamily = Manrope,
        fontSize = 11.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.em,
        color = C.t3,
    )
}

val AppTypography = Typography(
    displayLarge = Type.Display,
    displayMedium = Type.DisplayRing,
    displaySmall = Type.DisplayRing,
    headlineLarge = Type.H1,
    headlineMedium = Type.H2,
    headlineSmall = Type.H2,
    titleLarge = Type.Stat,
    titleMedium = Type.Item,
    titleSmall = Type.Small,
    bodyLarge = Type.Body.copy(color = C.t1),
    bodyMedium = Type.Body,
    bodySmall = Type.Meta,
    labelLarge = Type.Button,
    labelMedium = Type.Label,
    labelSmall = Type.Nav,
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CekKendaraanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.acc,
            onPrimary = C.ink,
            primaryContainer = C.BlueSoft,
            onPrimaryContainer = C.acc,
            secondary = C.acc2,
            onSecondary = C.ink,
            secondaryContainer = C.s2,
            onSecondaryContainer = C.t1,
            background = C.bg,
            onBackground = C.t1,
            surface = C.s1,
            onSurface = C.t1,
            surfaceVariant = C.s2,
            onSurfaceVariant = C.t2,
            surfaceContainerLowest = C.bg,
            surfaceContainerLow = C.s1,
            surfaceContainer = C.s1,
            surfaceContainerHigh = C.s2,
            surfaceContainerHighest = C.s3,
            outline = C.s3,
            outlineVariant = C.s2,
            error = C.bad,
            onError = C.t1,
            errorContainer = C.RedSoft,
            onErrorContainer = C.bad,
        ),
        typography = AppTypography,
        shapes = shapes,
        content = content,
    )
}
