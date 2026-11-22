package id.cekendaraan.app.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R
import id.cekendaraan.app.data.ComponentTemplate
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Seed
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.TwoWheeler
import id.cekendaraan.app.data.slug
import id.cekendaraan.app.data.VehicleKind
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C

private data class SectionCopy(
    val title: String,
    val empty: String,
    val add: String,
    val icon: ImageVector,
    val custom: String,
)

private fun copyFor(kind: Kind) = when (kind) {
    Kind.COMPONENT -> SectionCopy("Komponen", "Belum Ada Komponen Aktif", "Tambah Komponen", Icons.Rounded.Handyman, "Komponen Kustom")
    Kind.TREATMENT -> SectionCopy("Perawatan", "Belum Ada Perawatan Aktif", "Tambah Perawatan", Icons.Rounded.CleaningServices, "Perawatan Kustom")
    Kind.PAPERWORK -> SectionCopy("Dokumen", "Belum Ada Dokumen", "Tambah Dokumen", Icons.Rounded.Description, "Dokumen Kustom")
}

private val kinds = listOf(Kind.COMPONENT, Kind.TREATMENT, Kind.PAPERWORK)

@Composable
fun HomeScreen(
    ui: UiState,
    section: Int,
    onSection: (Int) -> Unit,
    onOpenComponent: (Long) -> Unit,
    onSetupTemplate: (String) -> Unit,
    onCustom: (Kind) -> Unit,
    onServiceMany: (List<Long>) -> Unit,
    onUpdateOdometer: () -> Unit,
    onOpenGarage: () -> Unit,
) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    val reminders = rememberReminderToggle()
    var selecting by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var addOpen by rememberSaveable { mutableStateOf(true) }
    var info by remember { mutableStateOf<ComponentTemplate?>(null) }

    val kind = kinds[section]
    val copy = copyFor(kind)
    val active = ui.ofKind(kind)
    val trackedKeys = ui.components.map { it.component.templateKey }.toSet()
    val untracked = Seed.templates.filter { it.kind == kind && it.key !in trackedKeys && Seed.fits(it, ui.vehicleKind) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 130.dp),
        ) {
            item { Spacer(Modifier.statusBarsPadding().height(64.dp)) }

            item {
                Spacer(Modifier.height(4.dp))
                StatusPanel(
                    odometerKm = vehicle.odometerKm,
                    remindersOn = reminders.enabled,
                    onAutoTrack = {
                        Toast.makeText(context, "Auto-Track segera hadir", Toast.LENGTH_SHORT).show()
                    },
                    onReminders = { reminders.set(!reminders.enabled) },
                    onOdometer = onUpdateOdometer,
                )
            }

            item {
                VehicleHero(
                    glow = ui.components.isNotEmpty(),
                    brand = vehicle.brand,
                    model = vehicle.model,
                    kind = ui.vehicleKind,
                )
            }

            item {
                SummaryBanner(ui)
                Spacer(Modifier.height(18.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SegmentedControl(
                        options = listOf("Komponen", "Perawatan", "Dokumen"),
                        selected = section,
                        onSelect = {
                            onSection(it)
                            selecting = false
                            selected = emptySet()
                        },
                        height = 34.dp,
                        fontSize = 13,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier
                            .height(32.dp)
                            .width(72.dp)
                            .clip(CircleShape)
                            .background(if (selecting) C.Surface2 else C.BlueChip)
                            .clickable {
                                selecting = !selecting
                                selected = emptySet()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (selecting) "Batal" else "Pilih", color = if (selecting) C.Text else C.Blue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(22.dp))
            }

            if (active.isEmpty()) {
                item {
                    Text(copy.title, color = C.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(copy.icon, contentDescription = null, tint = C.Segment, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(22.dp))
                        Text(copy.empty, color = C.Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                statusGroup(DueStatus.DUE, active, selecting, selected, onOpenComponent) { selected = selected.toggle(it) }
                statusGroup(DueStatus.SOON, active, selecting, selected, onOpenComponent) { selected = selected.toggle(it) }
                statusGroup(DueStatus.OK, active, selecting, selected, onOpenComponent) { selected = selected.toggle(it) }
                statusGroup(DueStatus.UNKNOWN, active, selecting, selected, onOpenComponent) { selected = selected.toggle(it) }
                statusGroup(DueStatus.NONE, active, selecting, selected, onOpenComponent) { selected = selected.toggle(it) }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(CircleShape)
                        .background(C.Surface)
                        .clickable { addOpen = !addOpen },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(copy.add, color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (addOpen) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = C.Blue,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (addOpen) {
                val tiles: List<ComponentTemplate?> = untracked + listOf(null)
                tiles.chunked(2).forEach { row ->
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { template ->
                                if (template == null) {
                                    CustomTile(copy.custom, Modifier.weight(1f)) { onCustom(kind) }
                                } else {
                                    TemplateTile(
                                        template = template,
                                        modifier = Modifier.weight(1f),
                                        onInfo = { info = template },
                                        onClick = { onSetupTemplate(template.key) },
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        // Sticky header: stays on top while the page scrolls under a fading scrim.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(C.Bg, C.Bg.copy(alpha = 0.92f), C.Bg.copy(alpha = 0f))))
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, bottom = 10.dp)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenGarage),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        vehicle.name,
                        color = C.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Pilih kendaraan", tint = C.Text, modifier = Modifier.size(22.dp))
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenGarage),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Garasi", tint = C.Text, modifier = Modifier.size(26.dp))
                }
            }

        if (selecting) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 84.dp),
            ) {
                PrimaryButton(
                    text = if (selected.isEmpty()) "Pilih item untuk diservis" else "Servis ${selected.size} item",
                    enabled = selected.isNotEmpty(),
                    onClick = {
                        onServiceMany(selected.toList())
                        selecting = false
                        selected = emptySet()
                    },
                )
            }
        }
    }

    info?.let { template ->
        AlertDialog(
            onDismissRequest = { info = null },
            containerColor = C.Surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text(template.name, color = C.Text, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("JIKA DIABAIKAN", color = C.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(template.risk, color = C.Muted, fontSize = 15.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(14.dp))
                    Text("INTERVAL DEFAULT", color = C.Blue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(Fmt.interval(template.km, template.months), color = C.Text, fontSize = 15.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { info = null }) { Text("Tutup", color = C.Blue, fontWeight = FontWeight.Bold) }
            },
        )
    }
}

private fun Set<Long>.toggle(id: Long) = if (id in this) this - id else this + id

private fun LazyListScope.statusGroup(
    status: DueStatus,
    all: List<ComponentUi>,
    selecting: Boolean,
    selected: Set<Long>,
    onOpen: (Long) -> Unit,
    onToggle: (Long) -> Unit,
) {
    val items = all.filter { it.due.status == status }
    if (items.isEmpty()) return
    item {
        GroupHeader(status, items.size)
        Spacer(Modifier.height(10.dp))
    }
    items.chunked(2).forEach { row ->
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    val id = item.component.id
                    ActiveTile(
                        item = item,
                        selecting = selecting,
                        checked = id in selected,
                        modifier = Modifier.weight(1f),
                        onClick = { if (selecting) onToggle(id) else onOpen(id) },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
}

@Composable
private fun StatusPanel(
    odometerKm: Int,
    remindersOn: Boolean,
    onAutoTrack: () -> Unit,
    onReminders: () -> Unit,
    onOdometer: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(C.Surface)
            .padding(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 0.dp)) {
            ToggleChip("Auto-Track", false, onAutoTrack)
            ToggleChip("Notifikasi", remindersOn, onReminders)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(C.Bg)
                .border(1.5.dp, C.LineStrong, RoundedCornerShape(14.dp))
                .clickable(onClick = onOdometer),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Speed, contentDescription = null, tint = C.Text, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(Fmt.number(odometerKm), color = C.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.width(6.dp))
            Text("km", color = C.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleChip(label: String, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .height(25.dp)
            .clip(CircleShape)
            .background(C.Surface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (on) C.Green else C.Muted))
        Spacer(Modifier.width(5.dp))
        Text(label, color = if (on) C.Text else C.Muted, fontSize = 12.sp)
    }
}

/**
 * Hero picture lookup. A model's own render is res/drawable-nodpi/vehicle_<brand_model>.webp
 * (see art/PROMPTS.md); otherwise the kind's picture vehicle_kind_<kind>.webp; otherwise the
 * matic scooter. New renders only need dropping in with the right file name.
 */
@Composable
fun rememberVehicleArt(brand: String, model: String, kind: VehicleKind): Int? {
    val context = LocalContext.current
    return remember(brand, model, kind) {
        val res = context.resources
        val pkg = context.packageName
        fun find(name: String) = res.getIdentifier(name, "drawable", pkg).takeIf { it != 0 }
        find("vehicle_" + slug(brand, model))
            ?: find("vehicle_kind_" + kind.name.lowercase())
            ?: if (kind.isMotor) find("vehicle_kind_matic") else null
    }
}

@Composable
fun VehicleHero(
    glow: Boolean,
    modifier: Modifier = Modifier,
    height: Int = 190,
    brand: String = "",
    model: String = "",
    kind: VehicleKind = VehicleKind.MATIC,
) {
    val art = rememberVehicleArt(brand, model, kind)
    // Static, pre-rendered picture: no GPU scene, so scrolling stays smooth.
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        if (glow) C.BlueGlow.copy(alpha = 0.55f) else Color(0xFF15181C),
                        Color.Transparent,
                    ),
                ),
            )
            .drawBehind {
                // soft floor shadow under the wheels
                val w = size.width * 0.62f
                val h = size.height * 0.10f
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent),
                        center = Offset(size.width / 2, size.height * 0.90f),
                        radius = w / 2,
                    ),
                    topLeft = Offset((size.width - w) / 2, size.height * 0.90f - h / 2),
                    size = Size(w, h),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (art != null) {
            Image(
                painter = painterResource(art),
                contentDescription = "Gambar kendaraan",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 10.dp),
            )
        } else {
            // No render for this kind yet: a quiet silhouette instead of a wrong vehicle.
            Icon(
                if (kind.isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                contentDescription = "Gambar kendaraan",
                tint = C.Surface3,
                modifier = Modifier.size((height * 0.62f).dp),
            )
        }
    }
}

@Composable
private fun SummaryBanner(ui: UiState) {
    val active = ui.components.filter { it.due.status != DueStatus.NONE }
    val (icon, color, text) = when {
        active.isEmpty() -> Triple(null, C.Muted, "Tambah komponen untuk melihat ringkasan di sini.")
        ui.dueCount > 0 -> Triple(Icons.Rounded.Error, C.Red, "${ui.dueCount} item perlu diganti")
        ui.soonCount > 0 -> Triple(Icons.Rounded.Warning, C.Amber, "${ui.soonCount} item perlu dicek segera")
        ui.unknownCount > 0 && ui.okCount == 0 -> Triple(Icons.Rounded.HelpOutline, C.Muted, "Catat servis terakhir untuk memulai")
        else -> Triple(Icons.Rounded.CheckCircle, C.Green, "Semua komponen aman")
    }
    val border = if (icon == null) C.LineStrong else color.copy(alpha = 0.45f).let {
        if (color == C.Green) Color(0xFF29457A) else it
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(C.Surface)
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = if (icon == null) 18.dp else 12.dp),
        horizontalArrangement = if (icon == null) Arrangement.Center else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text,
            color = if (icon == null) C.Muted else C.Text,
            fontSize = 16.sp,
            fontWeight = if (icon == null) FontWeight.Normal else FontWeight.SemiBold,
            textAlign = if (icon == null) TextAlign.Center else TextAlign.Start,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun GroupHeader(status: DueStatus, count: Int) {
    val color = statusColor(status)
    val icon = when (status) {
        DueStatus.DUE -> Icons.Rounded.Error
        DueStatus.SOON -> Icons.Rounded.Warning
        DueStatus.OK -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.HelpOutline
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(18.dp))
        Text(statusLabel(status), color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f))
                .border(1.dp, color.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$count", color = C.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActiveTile(
    item: ComponentUi,
    selecting: Boolean,
    checked: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val color = statusColor(item.due.status)
    Box(
        modifier
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(C.Surface)
            .then(if (checked) Modifier.border(2.dp, C.Blue, RoundedCornerShape(28.dp)) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                item.component.name,
                color = C.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(38.dp).padding(horizontal = 14.dp),
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(
                    componentIcon(item.component.icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(58.dp),
                )
            }
            Text(
                Fmt.tileHeadline(item.due),
                color = C.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            val sub = Fmt.tileSub(item.due)
            if (sub.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(sub, color = C.Muted, fontSize = 12.sp, maxLines = 1)
            }
        }
        if (selecting) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (checked) C.Blue else Color.Transparent)
                    .border(2.dp, if (checked) C.Blue else C.Muted2, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = C.OnAccent, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun TemplateTile(
    template: ComponentTemplate,
    modifier: Modifier,
    onInfo: () -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(C.Surface)
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                if (template.important) {
                    Box(
                        Modifier.size(18.dp).clip(CircleShape).background(C.Star),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = "Paling penting", tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    template.name,
                    color = C.Text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Tentang ${template.name}",
                    tint = C.Blue,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onInfo),
                )
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(componentIcon(template.icon), contentDescription = null, tint = C.Muted, modifier = Modifier.size(46.dp))
            }
            if (template.important) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(C.BlueChip)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text("Paling Penting", color = C.Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Spacer(Modifier.height(8.dp))
            }
            Text("Ketuk untuk atur", color = C.Muted, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun CustomTile(label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(C.Surface)
            .dashedBorder(C.LineStrong, 28.dp)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = C.Muted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(10.dp))
        Text(label, color = C.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
