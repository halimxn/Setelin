package id.cekendaraan.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Co2
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.OilBarrel
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R
import androidx.compose.ui.res.vectorResource
import id.cekendaraan.app.domain.DueInfo
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
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

object Fmt {
    fun number(value: Int): String = intFormat.format(value)
    fun money(value: Long): String = "Rp ${longFormat.format(value)}"
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

    /** Small line under the headline, e.g. "perk. Jan 2027". */
    fun tileSub(due: DueInfo): String {
        if (due.status == DueStatus.UNKNOWN) return "Ketuk untuk catat"
        val date = due.dueDate ?: return ""
        return "perk. ${monthYearShort(date)}"
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
    DueStatus.OK -> C.Green
    DueStatus.SOON -> C.Amber
    DueStatus.DUE -> C.Red
    DueStatus.UNKNOWN -> C.Muted
    DueStatus.NONE -> C.Muted
}

fun statusLabel(status: DueStatus): String = when (status) {
    DueStatus.OK -> "Sehat"
    DueStatus.SOON -> "Cek Segera"
    DueStatus.DUE -> "Perlu Diganti"
    DueStatus.UNKNOWN -> "Belum Tercatat"
    DueStatus.NONE -> "Tanpa Interval"
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

/* ---------- building blocks ---------- */

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(C.Blue))
        Spacer(Modifier.width(8.dp))
        Text("Cek Kendaraan", color = C.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** Round dark back button used on full screen flows. */
@Composable
fun BackCircle(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(C.Surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Kembali", tint = C.Text, modifier = Modifier.size(28.dp))
    }
}

/** Plain top bar: arrow on the left, centred title. */
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
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.Text)
            }
        }
        Text(
            title,
            color = C.Text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
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
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.Text)
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

/** Grey pill segmented control ("Komponen | Perawatan | Dokumen"). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    equalWidth: Boolean = true,
    fontSize: Int = 16,
) {
    Row(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(C.Surface)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            val bg by animateColorAsState(if (active) C.Segment else Color.Transparent, label = "seg")
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
                    label,
                    color = if (active) C.Text else C.Muted,
                    fontSize = fontSize.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
fun StatusPill(status: DueStatus, compact: Boolean = false) {
    val color = statusColor(status)
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = CircleShape,
    ) {
        Text(
            statusLabel(status),
            color = color,
            fontSize = if (compact) 11.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = if (compact) 9.dp else 12.dp,
                vertical = if (compact) 3.dp else 5.dp,
            ),
        )
    }
}

@Composable
fun ProgressLine(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Int = 8,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "progress")
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(CircleShape)
            .background(C.Surface2),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun IconWell(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 46,
    iconSize: Int = 23,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
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
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.Text, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** Full-width blue pill button. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = C.BlueButton,
            contentColor = C.OnAccent,
            disabledContainerColor = C.BlueSoft,
            disabledContentColor = C.Muted2,
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 0.sp)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.dp, C.LineStrong),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = C.Surface, contentColor = C.Text),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) C.Segment else C.Surface,
        contentColor = if (selected) C.Text else C.Muted,
        shape = CircleShape,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Pill-shaped text field like the vehicle-name input. */
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
            .background(C.Field)
            .border(1.dp, C.LineStrong, CircleShape)
            .padding(start = 22.dp, end = if (trailing != null) 6.dp else 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = C.Muted2, fontSize = 17.sp, maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = C.Text, fontSize = 17.sp),
                cursorBrush = SolidColor(C.Blue),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        trailing?.invoke()
    }
}

/** Small black box input used for intervals ("2000"). */
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
            .background(C.Inset)
            .border(1.dp, C.Line, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (value.isEmpty()) Text(placeholder, color = C.Muted2, fontSize = 18.sp)
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter(Char::isDigit).take(7)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = C.Text,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(C.Blue),
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
        Text(label, color = C.Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        val shape = if (singleLine) CircleShape else RoundedCornerShape(18.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(C.Field)
                .border(1.dp, if (isError) C.Red else C.LineStrong, shape)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(placeholder, color = C.Muted2, fontSize = 16.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    minLines = minLines,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = C.Text),
                    cursorBrush = SolidColor(C.Blue),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (suffix != null) {
                Spacer(Modifier.width(8.dp))
                Text(suffix, color = C.Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (supportingText != null) {
            Text(
                supportingText,
                color = if (isError) C.Red else C.Muted,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(start = 8.dp, top = 6.dp),
            )
        }
    }
}

/** Centred empty state: grey circle with icon, bold title, muted message. */
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
                .background(C.Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = C.Muted, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = C.Text, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = C.Muted,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onAction,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = C.BlueButton, contentColor = C.OnAccent),
                modifier = Modifier.height(40.dp),
            ) {
                Text(actionLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = C.Muted)
    }
}

@Composable
fun DueStat(value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(value, color = C.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(label, color = C.Muted, fontSize = 12.sp)
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
            Text(title, style = MaterialTheme.typography.headlineSmall)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
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
            .background(C.Field)
            .border(1.dp, C.LineStrong, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = C.Blue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = C.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InfoNote(text: String, color: Color = C.Muted) {
    Text(
        text,
        color = color,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

/** Rounded dark card container. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    radius: Dp = 26.dp,
    color: Color = C.Surface,
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

