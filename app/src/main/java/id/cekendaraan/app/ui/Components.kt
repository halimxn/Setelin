package id.cekendaraan.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R
import id.cekendaraan.app.domain.DueInfo
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
import id.cekendaraan.app.ui.theme.Gradients
import id.cekendaraan.app.ui.theme.Type
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val idLocale = Locale.forLanguageTag("id-ID")
private val intFormat = NumberFormat.getIntegerInstance(idLocale)
private val longFormat = NumberFormat.getIntegerInstance(idLocale)
private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", idLocale)
private val shortDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM", idLocale)
private val dayFormat = DateTimeFormatter.ofPattern("d", idLocale)
private val monthFormat = DateTimeFormatter.ofPattern("MMM", idLocale)
private val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", idLocale)
private val monthYearShort = DateTimeFormatter.ofPattern("MMM yyyy", idLocale)

/** Indonesian text and number formatting conforming strictly to docs/DESIGN.md */
object Fmt {
    fun number(value: Int): String = intFormat.format(value)
    // DESIGN.md: rupiah tanpa spasi, misal Rp85.000
    fun money(value: Long): String = "Rp${longFormat.format(value)}"
    fun date(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormat)
    fun date(date: LocalDate): String = date.format(dateFormat)
    fun dateShort(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortDateFormat)
    fun day(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dayFormat)
    fun month(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(monthFormat)
    fun monthYear(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(monthYearFormat)
    fun monthYearShort(date: LocalDate): String = date.format(monthYearShort)

    fun interval(km: Int?, months: Int?): String {
        val parts = buildList {
            km?.let { add("${number(it)} km") }
            months?.let { add("$it bulan") }
        }
        return if (parts.isEmpty()) "Tanpa interval" else parts.joinToString(" · ")
    }

    /** Big line on a component tile, e.g. "2.000 km lagi" or "Lewat 120 km". */
    fun tileHeadline(due: DueInfo): String {
        if (due.status == DueStatus.UNKNOWN) return "Belum tercatat"
        if (due.status == DueStatus.NONE) return "Tanpa interval"
        val km = due.remainingKm
        val days = due.remainingDays
        return when {
            km != null && (due.limit == DueLimit.KM || days == null) ->
                if (km >= 0) "${number(km)} km lagi" else "Lewat ${number(abs(km))} km"
            days != null ->
                if (days >= 0) "$days hari lagi" else "Lewat ${abs(days)} hari"
            else -> "—"
        }
    }

    /** Small line under headline, e.g. "Estimasi ganti Jan 2027". */
    fun tileSub(due: DueInfo): String {
        if (due.status == DueStatus.UNKNOWN) return "Ketuk untuk catat"
        val date = due.dueDate ?: return ""
        return "Estimasi ganti ${monthYearShort(date)}"
    }

    fun remaining(due: DueInfo): String {
        if (due.status == DueStatus.UNKNOWN) return "Belum ada catatan servis"
        if (due.status == DueStatus.NONE) return "Tanpa interval"
        val parts = buildList {
            due.remainingKm?.let {
                add(
                    when {
                        it > 0 -> "sisa ${number(it)} km"
                        it == 0 -> "jatuh tempo"
                        else -> "lewat ${number(abs(it))} km"
                    },
                )
            }
            due.remainingDays?.let {
                add(
                    when {
                        it > 0 -> "$it hari lagi"
                        it == 0L -> "hari ini"
                        else -> "lewat ${abs(it)} hari"
                    },
                )
            }
        }
        return if (parts.isEmpty()) interval(null, null) else parts.joinToString(" · ")
    }
}

private typealias DueLimit = id.cekendaraan.app.domain.DueLimit

fun statusColor(status: DueStatus): Color = when (status) {
    DueStatus.OK -> C.ok
    DueStatus.SOON -> C.warn
    DueStatus.DUE -> C.bad
    DueStatus.UNKNOWN -> C.t3
    DueStatus.NONE -> C.t3
}

fun statusLabel(status: DueStatus): String = when (status) {
    DueStatus.OK -> "Aman"
    DueStatus.SOON -> "Segera"
    DueStatus.DUE -> "Lewat"
    DueStatus.UNKNOWN -> "Belum tercatat"
    DueStatus.NONE -> "Tanpa interval"
}

/** Custom glyph set in res/drawable/ic_comp_*.xml (generated, 48dp, tintable). */
private fun componentIconRes(key: String): Int = when (key) {
    "oil" -> R.drawable.ic_comp_oil
    "gear" -> R.drawable.ic_comp_gear
    "cvt" -> R.drawable.ic_comp_cvt
    "belt" -> R.drawable.ic_comp_belt
    "air" -> R.drawable.ic_comp_air
    "plug" -> R.drawable.ic_comp_plug
    "brake" -> R.drawable.ic_comp_brake
    "disc" -> R.drawable.ic_comp_disc
    "fluid" -> R.drawable.ic_comp_fluid
    "battery" -> R.drawable.ic_comp_battery
    "tire" -> R.drawable.ic_comp_tire
    "coolant" -> R.drawable.ic_comp_coolant
    "filter" -> R.drawable.ic_comp_filter
    "fuel" -> R.drawable.ic_comp_fuel
    "roller" -> R.drawable.ic_comp_roller
    "spring" -> R.drawable.ic_comp_spring
    "shock" -> R.drawable.ic_comp_shock
    "water" -> R.drawable.ic_comp_water
    "tune" -> R.drawable.ic_comp_tune
    "bearing" -> R.drawable.ic_comp_bearing
    "doc" -> R.drawable.ic_comp_doc
    "plate" -> R.drawable.ic_comp_plate
    "license" -> R.drawable.ic_comp_license
    "shield" -> R.drawable.ic_comp_shield
    "emission" -> R.drawable.ic_comp_emission
    else -> R.drawable.ic_comp_service
}

@Composable
fun componentIcon(key: String): ImageVector = ImageVector.vectorResource(componentIconRes(key))

/* ---------- Neon Cockpit Fundamental Components ---------- */

/**
 * Primary action button:
 * - 135° accent gradient (acc -> acc2)
 * - Height: 54dp, Corner radius: 18dp
 * - Text: 15sp, weight 700, color ink
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val bgBrush = if (enabled) Gradients.Accent else SolidColor(C.s2)
    val contentColor = if (enabled) C.ink else C.t3

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = Type.Button.copy(color = contentColor),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Secondary action button:
 * - Surface s2 background, no border
 * - Height: 54dp, Corner radius: 18dp
 * - Text: 15sp, weight 700, color t1
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val contentColor = if (enabled) C.t1 else C.t3

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(C.s2)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = Type.Button.copy(color = contentColor),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Icon button (42×42dp):
 * - Surface s1 background, radius 14dp
 * - Optional notification badge (7dp, acc)
 */
@Composable
fun IconButton42(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    hasBadge: Boolean = false,
    tint: Color = C.t1,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(C.s1)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        if (hasBadge) {
            Box(
                Modifier
                    .size(7.dp)
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 4.dp)
                    .clip(CircleShape)
                    .background(C.acc),
            )
        }
    }
}

/**
 * Chip for horizontal filter bars:
 * - Active: acc background, ink text
 * - Inactive: s2 background, t2 text
 * - Weight 600, pill shape
 */
@Composable
fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) C.acc else C.s2
    val textColor = if (selected) C.ink else C.t2

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = Type.Small.copy(fontSize = 13.sp, color = textColor),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Alias for older code calling FilterPill */
@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) =
    Chip(label = label, selected = selected, onClick = onClick)

/**
 * Status Tag:
 * - Pill shape, 12sp, weight 700
 * - Text in status color, background at 15% alpha
 */
@Composable
fun StatusTag(
    status: DueStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
) {
    val color = statusColor(status)
    val text = customLabel ?: statusLabel(status)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = Type.Label.copy(fontWeight = FontWeight.W700, color = color),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Backward compatible StatusPill */
@Composable
fun StatusPill(status: DueStatus, compact: Boolean = false) =
    StatusTag(status = status)

/**
 * 5dp Progress Bar conforming strictly to DESIGN.md:
 * - Height: 5dp, radius 3dp
 * - Track: s3
 * - Fill: animated status color or accent
 */
@Composable
fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 5.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progressbar",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(C.s3),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color),
        )
    }
}

/** Backward compatible ProgressLine */
@Composable
fun ProgressLine(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Int = 5,
) = ProgressBar(progress = progress, color = color, modifier = modifier, height = height.dp)

/**
 * Item row card in list:
 * - Background s1, radius 20dp, padding 14dp, NO border
 * - Left: 44dp box with status color (13% alpha) + icon
 * - Right: Name (Item, 15/600) + Value (14/700 tnum)
 * - Middle: 5dp progress bar (track s3)
 * - Bottom: Meta description (13/500, t2)
 */
@Composable
fun ItemRow(
    title: String,
    value: String,
    subtitle: String,
    iconKey: String,
    status: DueStatus,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = statusColor(status)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(C.s1)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ① Icon well 44dp
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = componentIcon(iconKey),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Right content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // ② Name + ③ Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = Type.Item,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = value,
                        style = Type.Small.copy(fontWeight = FontWeight.W700),
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Spacer(Modifier.height(9.dp))

                // ④ Progress 5dp
                ProgressBar(progress = progress, color = color, height = 5.dp)

                Spacer(Modifier.height(6.dp))

                // ⑤ Meta text
                Text(
                    text = subtitle,
                    style = Type.Meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Attention card (Perlu perhatian):
 * - Highlight gradient (statusHighlight) from status color (14-18% alpha) fading to s1 at 75%
 * - Radius 20dp, padding 14dp, NO border
 * - Left: 44dp icon well with status color 13% alpha + icon
 * - Right: Name (Item, 15/600) + StatusTag (pill 12/700)
 * - Bottom: Meta explanation (13/500, t2)
 */
@Composable
fun AttentionCard(
    title: String,
    subtitle: String,
    iconKey: String,
    status: DueStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = statusColor(status)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Gradients.statusHighlight(color))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = componentIcon(iconKey),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = Type.Item,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusTag(status = status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = Type.Meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Key-Value info item inside InfoCard.
 */
data class InfoRowItem(
    val label: String,
    val value: String,
    val valueColor: Color = C.t1,
)

/**
 * Info Card (Key–Value card):
 * - Background s1, radius 20-22dp, padding 16dp
 * - Label: Meta (13/500, t2)
 * - Value: Small (13.5/600, tnum, t1)
 * - Divider between rows: 6% t1 (C.div)
 */
@Composable
fun InfoCard(
    items: List<InfoRowItem>,
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(C.s1)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.label,
                        style = Type.Meta,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.value,
                        style = Type.Small.copy(color = item.valueColor),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(color = C.div, thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * Circular Gauge Ring (176dp):
 * - 176dp, thickness 12dp
 * - Track: s3
 * - Arc: starts at 12 o'clock (-90°), sweeps clockwise equal to remaining lifespan
 * - Center: Display number (36sp, 700, tnum) + "km lagi" (Meta, t2)
 */
@Composable
fun GaugeRing(
    remainingValue: String,
    unitText: String,
    remainingRatio: Float,
    statusColor: Color,
    modifier: Modifier = Modifier,
    iconKey: String? = null,
    size: Dp = 176.dp,
    strokeWidth: Dp = 12.dp,
) {
    val animatedRatio by animateFloatAsState(
        targetValue = remainingRatio.coerceIn(0f, 1f),
        label = "gaugering",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle radial status glow behind the ring
        Box(
            modifier = Modifier
                .size(size * 1.3f)
                .background(Gradients.statusGlow(statusColor)),
        )

        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background track s3
            drawArc(
                color = C.s3,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Dynamic remaining arc
            if (animatedRatio > 0f) {
                drawArc(
                    color = statusColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedRatio,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        // Center telemetry
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (iconKey != null) {
                Icon(
                    imageVector = componentIcon(iconKey),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = remainingValue,
                style = Type.DisplayRing,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = unitText,
                style = Type.Meta,
                maxLines = 1,
            )
        }
    }
}

/* ---------- Common Utility Elements ---------- */

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(C.acc))
        Spacer(Modifier.width(8.dp))
        Text("Setelin", style = Type.Item.copy(fontWeight = FontWeight.W700))
    }
}

@Composable
fun BackCircle(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(C.s1)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Kembali", tint = C.t1, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun CenterTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.t1)
            }
        }
        Text(
            text = title,
            style = Type.H2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 96.dp),
        )
        Row(
            Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.t1)
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = Type.H2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = Type.Meta, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    equalWidth: Boolean = true,
    fontSize: Int = 14,
) {
    Row(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(C.s1)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            val bg by animateColorAsState(if (active) C.s2 else Color.Transparent, label = "seg")
            Box(
                (if (equalWidth) Modifier.weight(1f) else Modifier)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(index) }
                    .padding(horizontal = if (equalWidth) 4.dp else 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = Type.Small.copy(
                        fontSize = fontSize.sp,
                        color = if (active) C.t1 else C.t2,
                        fontWeight = if (active) FontWeight.W700 else FontWeight.W500,
                    ),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
fun IconWell(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 44,
    iconSize: Int = 22,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(componentIcon(icon), contentDescription = null, tint = color, modifier = Modifier.size(iconSize.dp))
    }
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = Type.H2, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun PillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(C.s1)
            .border(1.dp, C.s3, CircleShape)
            .padding(start = 22.dp, end = if (trailing != null) 6.dp else 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, style = Type.Body.copy(color = C.t3), maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = Type.Body.copy(color = C.t1, fontSize = 16.sp),
                cursorBrush = SolidColor(C.acc),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun BoxNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "—",
) {
    Box(
        modifier
            .width(80.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(C.s1)
            .border(1.dp, C.s2, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (value.isEmpty()) Text(placeholder, style = Type.Stat.copy(color = C.t3))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter(Char::isDigit).take(7)) },
            singleLine = true,
            textStyle = Type.Stat.copy(textAlign = TextAlign.Center, color = C.t1),
            cursorBrush = SolidColor(C.acc),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    Column(modifier) {
        Text(label, style = Type.Label)
        Spacer(Modifier.height(8.dp))
        val shape = if (singleLine) CircleShape else RoundedCornerShape(18.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(C.s1)
                .border(1.dp, if (isError) C.bad else C.s3, shape)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(placeholder, style = Type.Body.copy(color = C.t3))
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    minLines = minLines,
                    textStyle = Type.Body.copy(color = C.t1),
                    cursorBrush = SolidColor(C.acc),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (suffix != null) {
                Spacer(Modifier.width(8.dp))
                Text(suffix, style = Type.Small.copy(color = C.t2))
            }
        }
        if (supportingText != null) {
            Text(
                supportingText,
                style = Type.Caption.copy(color = if (isError) C.bad else C.t3),
                modifier = Modifier.padding(start = 8.dp, top = 6.dp),
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(C.s1),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = C.t2, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = Type.H2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = Type.Body,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.width(180.dp),
            )
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = C.t2)
    }
}

@Composable
fun DueStat(value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(value, style = Type.Small.copy(fontWeight = FontWeight.W700))
        Spacer(Modifier.width(4.dp))
        Text(label, style = Type.Label)
    }
}

fun percentLabel(due: DueInfo): String =
    if (due.status == DueStatus.UNKNOWN || due.status == DueStatus.NONE) {
        "—"
    } else {
        "${(due.ratio * 100).roundToInt().coerceAtMost(999)}%"
    }

@Composable
fun SheetHeader(title: String, onClose: () -> Unit, subtitle: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = Type.H2)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = Type.Meta)
            }
        }
        CloseButton(onClose)
    }
}

@Composable
fun CalendarPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(C.s1)
            .border(1.dp, C.s3, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = C.acc, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = Type.Item)
    }
}

@Composable
fun InfoNote(text: String, color: Color = C.t2) {
    Text(
        text,
        style = Type.Meta.copy(color = color),
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

/** Rounded dark card container, no border by default. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    color: Color = C.s1,
    border: Color? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(color)
            .then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(radius)) else Modifier),
    ) { content() }
}

/** Dashed outline used for the locked/custom "+" tile. */
fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    val r = radius.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
    )
}
