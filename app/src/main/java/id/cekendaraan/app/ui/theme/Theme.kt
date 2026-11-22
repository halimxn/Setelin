package id.cekendaraan.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Near-black garage palette with one bright blue accent and iOS-like status colours.
 * The old names (Lime, LimeSoft, OnLime…) stay as aliases so older screens follow the new look.
 */
object C {
    // "Neon Cockpit": deep navy-black, glassy panels, one cyan instrument glow.
    val Bg = Color(0xFF05080D)
    val Surface = Color(0xFF0C141D)
    val Surface2 = Color(0xFF132130)
    val Surface3 = Color(0xFF1E3245)
    val Field = Color(0xFF0E1822)
    val Inset = Color(0xFF060B11)
    val Line = Color(0xFF15232F)
    val LineStrong = Color(0xFF1F3446)
    /** Thin cyan edge used on glass panels. */
    val Edge = Color(0x3322D3EE)

    val Blue = Color(0xFF22D3EE)
    val BlueButton = Color(0xFF22D3EE)
    val BlueChip = Color(0xFF0B3B4A)
    val BlueSoft = Color(0xFF0A2A36)
    val BlueDeep = Color(0xFF071E28)
    val BlueGlow = Color(0xFF0B6C87)
    /** Second stop of the accent gradient. */
    val Violet = Color(0xFF6C8CFF)
    /** Text/icons placed on the cyan accent. */
    val OnAccent = Color(0xFF02131A)

    val Green = Color(0xFF34E7B0)
    val GreenSoft = Color(0xFF0D2C27)
    val Amber = Color(0xFFFFC24B)
    val AmberSoft = Color(0xFF332611)
    val Red = Color(0xFFFF5470)
    val RedSoft = Color(0xFF33121C)
    val Star = Color(0xFFFFC24B)
    val Purple = Color(0xFF6C5CE7)

    val Text = Color(0xFFEAF6FF)
    val Muted = Color(0xFF86A0B5)
    val Muted2 = Color(0xFF52687A)
    val Segment = Color(0xFF1E3245)
    val Chrome = Color(0xFFD5E6F2)

    // Aliases kept for screens that still use the earlier names.
    val Lime = Blue
    val LimeDeep = BlueChip
    val LimeSoft = BlueSoft
    val OnLime = OnAccent
}

/** Monospaced digits for instrument-style numbers (odometer, km left, percentages). */
val Digits = androidx.compose.ui.text.font.FontFamily.Monospace

private val Sans = FontFamily.SansSerif

private fun text(
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    color: Color = C.Text,
    spacing: Float = 0f,
    lineHeight: Int = 0,
) = TextStyle(
    fontFamily = Sans,
    fontSize = size.sp,
    fontWeight = weight,
    color = color,
    letterSpacing = spacing.sp,
    lineHeight = if (lineHeight > 0) lineHeight.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
)

private val typography = Typography(
    displaySmall = text(34, FontWeight.Bold, spacing = -0.6f, lineHeight = 40),
    headlineLarge = text(30, FontWeight.Bold, spacing = -0.5f, lineHeight = 36),
    headlineMedium = text(26, FontWeight.Bold, spacing = -0.4f, lineHeight = 32),
    headlineSmall = text(22, FontWeight.Bold, spacing = -0.2f, lineHeight = 28),
    titleLarge = text(19, FontWeight.Bold),
    titleMedium = text(17, FontWeight.SemiBold),
    titleSmall = text(15, FontWeight.SemiBold),
    bodyLarge = text(16, lineHeight = 22),
    bodyMedium = text(14, lineHeight = 20),
    bodySmall = text(13, color = C.Muted, lineHeight = 18),
    labelLarge = text(15, FontWeight.SemiBold),
    labelMedium = text(13, FontWeight.SemiBold),
    labelSmall = text(11, FontWeight.SemiBold, color = C.Muted, spacing = 0.3f),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun CekKendaraanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.Blue,
            onPrimary = Color.White,
            primaryContainer = C.BlueSoft,
            onPrimaryContainer = C.Blue,
            secondary = C.Chrome,
            onSecondary = Color.Black,
            secondaryContainer = C.Surface2,
            onSecondaryContainer = C.Text,
            background = C.Bg,
            onBackground = C.Text,
            surface = C.Surface,
            onSurface = C.Text,
            surfaceVariant = C.Surface2,
            onSurfaceVariant = C.Muted,
            surfaceContainerLowest = C.Bg,
            surfaceContainerLow = C.Surface,
            surfaceContainer = C.Surface,
            surfaceContainerHigh = C.Surface,
            surfaceContainerHighest = C.Surface2,
            outline = C.LineStrong,
            outlineVariant = C.Line,
            error = C.Red,
            onError = Color.White,
            errorContainer = C.RedSoft,
            onErrorContainer = C.Red,
        ),
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
