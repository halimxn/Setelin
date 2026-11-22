package id.cekendaraan.app.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Prefs
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.data.Vehicle
import id.cekendaraan.app.data.VehicleKind
import id.cekendaraan.app.data.slug
import id.cekendaraan.app.domain.DueLimit
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
import id.cekendaraan.app.ui.theme.Gradients
import id.cekendaraan.app.ui.theme.Type
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Dasbor (Layar 1) conforming strictly to docs/DESIGN.md & art/mockup-tema.html (Neon Cockpit):
 * 1. Sapaan (Meta) + nama kendaraan (H1 + chevron) + lonceng notifikasi (IconButton42).
 * 2. Kartu kendaraan (vcard): render transparan dengan glow aksen di tengah,
 *    dan 3 statistik di bawah (Odometer · Kondisi % · Servis berikutnya).
 * 3. Tiga aksi cepat (Scan odometer · Catat servis · Isi bensin).
 * 4. Bagian "Perlu perhatian" (1–3 item SOON/DUE dengan sorot status).
 * 5. Bagian "Semua item" dengan chip filter dan tombol "＋ Tambah" (membuka katalog sheet).
 */
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
    onScanOdometer: () -> Unit = {},
    onLogService: () -> Unit = {},
) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    val remindersOn = remember(context) { Prefs.remindersEnabled(context) }

    var tambahOpen by rememberSaveable { mutableStateOf(false) }
    var filterIdx by rememberSaveable { mutableIntStateOf(section.coerceIn(0, 3)) }

    // Rata-rata sisa umur item aktif (0–100%)
    val kondisi = remember(ui.components) {
        calculateCondition(ui.components)
    }

    // Hari menuju item terdekat yang jatuh tempo
    val nextDays = remember(ui.components) {
        nextServiceDays(ui.components)
    }

    // Item SOON / DUE, maksimal 3
    val urgentItems = remember(ui.components) {
        ui.components
            .filter { it.due.status == DueStatus.DUE || it.due.status == DueStatus.SOON }
            .sortedWith(compareBy<ComponentUi>({ it.due.status != DueStatus.DUE }, { -it.due.ratio }))
            .take(3)
    }

    // Filter komponen untuk "Semua item"
    val filteredItems = remember(ui.components, filterIdx) {
        when (filterIdx) {
            1 -> ui.ofKind(Kind.COMPONENT)
            2 -> ui.ofKind(Kind.TREATMENT)
            3 -> ui.ofKind(Kind.PAPERWORK)
            else -> ui.components
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 130.dp,
            ),
        ) {
            // Status bar spacer
            item { Spacer(Modifier.statusBarsPadding()) }

            // 1. Header: Sapaan + Nama Kendaraan (H1 + chevron) + Lonceng
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenGarage),
                    ) {
                        Text(
                            text = greetingText(),
                            style = Type.Meta.copy(color = C.t2),
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.name,
                                style = Type.H1,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Ganti kendaraan",
                                tint = C.t1,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    IconButton42(
                        icon = Icons.Rounded.Notifications,
                        contentDescription = "Pengingat harian",
                        hasBadge = remindersOn,
                        onClick = {
                            val newSetting = !Prefs.remindersEnabled(context)
                            Prefs.setRemindersEnabled(context, newSetting)
                            val msg = if (newSetting) "Pengingat harian aktif (08.00)" else "Pengingat dinonaktifkan"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // 2. Kartu Kendaraan (vcard)
            item {
                VehicleCard(
                    vehicle = vehicle,
                    kind = ui.vehicleKind,
                    kondisi = kondisi,
                    nextDays = nextDays,
                    onUpdateOdometer = onUpdateOdometer,
                )
                Spacer(Modifier.height(12.dp))
            }

            // 3. Tiga Aksi Cepat
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickActionButton(
                        icon = ImageVector.vectorResource(R.drawable.ic_scan_fab),
                        label = "Scan odometer",
                        onClick = onScanOdometer,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionButton(
                        icon = Icons.Rounded.Add,
                        label = "Catat servis",
                        onClick = onLogService,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionButton(
                        icon = ImageVector.vectorResource(R.drawable.ic_comp_fuel),
                        label = "Isi bensin",
                        onClick = { Toast.makeText(context, "Segera hadir", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // 4. Perlu Perhatian (1-3 item SOON/DUE)
            if (urgentItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Perlu perhatian",
                            style = Type.H2,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                items(urgentItems.size) { idx ->
                    val item = urgentItems[idx]
                    val subtitle = attentionSubtitle(item)
                    AttentionCard(
                        title = item.component.name,
                        subtitle = subtitle,
                        iconKey = item.component.icon,
                        status = item.due.status,
                        onClick = { onOpenComponent(item.component.id) },
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item { Spacer(Modifier.height(14.dp)) }
            }

            // 5. Semua Item
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Semua item",
                        style = Type.H2,
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { tambahOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = C.acc,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Tambah",
                            style = Type.Small.copy(fontWeight = FontWeight.W700, color = C.acc),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Semua", "Komponen", "Perawatan", "Dokumen").forEachIndexed { idx, label ->
                        Chip(
                            label = label,
                            selected = filterIdx == idx,
                            onClick = {
                                filterIdx = idx
                                onSection(idx.coerceIn(0, 2))
                            },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(C.s1)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Belum ada item untuk kategori ini.",
                                style = Type.Body.copy(color = C.t2),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(12.dp))
                            SecondaryButton(
                                text = "＋ Tambah item",
                                onClick = { tambahOpen = true },
                                modifier = Modifier.width(180.dp),
                            )
                        }
                    }
                }
            } else {
                items(filteredItems.size) { idx ->
                    val item = filteredItems[idx]
                    val progress = if (item.due.limit == DueLimit.KM) item.due.progressKm else item.due.progressTime
                    ItemRow(
                        title = item.component.name,
                        value = Fmt.tileHeadline(item.due),
                        subtitle = Fmt.tileSub(item.due),
                        iconKey = item.component.icon,
                        status = item.due.status,
                        progress = progress,
                        onClick = { onOpenComponent(item.component.id) },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    if (tambahOpen) {
        TambahItemSheet(
            ui = ui,
            onDismiss = { tambahOpen = false },
            onSelectTemplate = onSetupTemplate,
            onCustom = onCustom,
        )
    }
}

/** Vehicle card with radial glow, vehicle art, and 3 telemetries at the bottom */
@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    kind: VehicleKind,
    kondisi: Int,
    nextDays: Int?,
    onUpdateOdometer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val art = rememberVehicleArt(vehicle.brand, vehicle.model, kind)

    val kondisiColor = when {
        kondisi >= 75 -> C.ok
        kondisi >= 40 -> C.warn
        else -> C.bad
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(C.s1),
    ) {
        Column {
            // Vehicle illustration with radial accent glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(Gradients.statusGlow(C.acc)),
                )

                if (art != null) {
                    Image(
                        painter = painterResource(art),
                        contentDescription = "${vehicle.brand} ${vehicle.model}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                } else {
                    Icon(
                        imageVector = if (kind.isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = C.t3,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }

            HorizontalDivider(color = C.div, thickness = 1.dp)

            // 3 Stats: Odometer · Kondisi · Servis berikutnya
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Stat 1: Odometer (clickable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onUpdateOdometer),
                ) {
                    Text(
                        text = "Odometer",
                        style = Type.Label,
                        color = C.t3,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = Fmt.number(vehicle.odometerKm),
                            style = Type.Stat,
                            color = C.t1,
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "km",
                            style = Type.Caption.copy(fontWeight = FontWeight.W600, color = C.t3),
                            modifier = Modifier.padding(bottom = 1.dp),
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(C.div),
                )

                Spacer(Modifier.width(12.dp))

                // Stat 2: Kondisi
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kondisi",
                        style = Type.Label,
                        color = C.t3,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$kondisi",
                            style = Type.Stat,
                            color = kondisiColor,
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "%",
                            style = Type.Caption.copy(fontWeight = FontWeight.W600, color = C.t3),
                            modifier = Modifier.padding(bottom = 1.dp),
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(C.div),
                )

                Spacer(Modifier.width(12.dp))

                // Stat 3: Servis berikutnya
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "Servis berikutnya",
                        style = Type.Label,
                        color = C.t3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (nextDays != null) {
                            Text(
                                text = "$nextDays",
                                style = Type.Stat,
                                color = C.t1,
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "hari",
                                style = Type.Caption.copy(fontWeight = FontWeight.W600, color = C.t3),
                                modifier = Modifier.padding(bottom = 1.dp),
                            )
                        } else {
                            Text(
                                text = "—",
                                style = Type.Stat,
                                color = C.t1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Quick Action button (Scan odometer, Catat servis, Isi bensin) */
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(C.s1)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(C.s2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = C.acc,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = Type.Small.copy(fontSize = 12.sp, color = C.t1),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
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
    glow: Boolean = false,
    modifier: Modifier = Modifier,
    height: Int = 190,
    brand: String = "",
    model: String = "",
    kind: VehicleKind = VehicleKind.MATIC,
) {
    val art = rememberVehicleArt(brand, model, kind)
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    listOf(
                        if (glow) C.acc.copy(alpha = 0.28f) else C.s2,
                        androidx.compose.ui.graphics.Color.Transparent,
                    ),
                ),
            ),
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
            Icon(
                imageVector = if (kind.isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                contentDescription = "Gambar kendaraan",
                tint = C.t3,
                modifier = Modifier.size((height * 0.62f).dp),
            )
        }
    }
}

/** Computes condition average (0-100%) from remaining lifespan of active components */
private fun calculateCondition(components: List<ComponentUi>): Int {
    val tracked = components.filter { it.due.status != DueStatus.UNKNOWN && it.due.status != DueStatus.NONE }
    if (tracked.isEmpty()) return 100
    val totalRemainingRatio = tracked.map { item ->
        val kmRatio = item.due.remainingKm?.let { km ->
            val interval = item.component.intervalKm ?: 1
            (km.toFloat() / interval).coerceIn(0f, 1f)
        }
        val timeRatio = item.due.remainingDays?.let { days ->
            val intervalDays = (item.component.intervalMonths ?: 1) * 30.4f
            (days.toFloat() / intervalDays).coerceIn(0f, 1f)
        }
        val minRatio = when {
            kmRatio != null && timeRatio != null -> minOf(kmRatio, timeRatio)
            kmRatio != null -> kmRatio
            timeRatio != null -> timeRatio
            else -> 1f - maxOf(item.due.progressKm, item.due.progressTime)
        }
        minRatio.coerceIn(0f, 1f)
    }.sum()
    return ((totalRemainingRatio / tracked.size) * 100).roundToInt().coerceIn(0, 100)
}

/** Calculates days until closest due item */
private fun nextServiceDays(components: List<ComponentUi>): Int? {
    val withDays = components.mapNotNull { it.due.remainingDays }
    if (withDays.isEmpty()) return null
    val minDays = withDays.minOrNull() ?: return null
    return minDays.toInt().coerceAtLeast(0)
}

private fun greetingText(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 4..10 -> "Selamat pagi"
        in 11..14 -> "Selamat siang"
        in 15..18 -> "Selamat sore"
        else -> "Selamat malam"
    }
}

private fun attentionSubtitle(item: ComponentUi): String {
    val due = item.due
    return when {
        due.remainingKm != null && due.remainingKm > 0 && due.dueDate != null ->
            "Ganti dalam ${Fmt.number(due.remainingKm)} km atau sebelum ${Fmt.monthYearShort(due.dueDate)}"
        due.remainingKm != null && due.remainingKm > 0 ->
            "Ganti dalam ${Fmt.number(due.remainingKm)} km"
        due.remainingKm != null && due.remainingKm <= 0 ->
            "Lewat ${Fmt.number(abs(due.remainingKm))} km, segera ganti"
        due.remainingDays != null && due.remainingDays > 0 ->
            "Jatuh tempo dalam ${due.remainingDays} hari"
        due.remainingDays != null && due.remainingDays <= 0 ->
            "Lewat ${abs(due.remainingDays)} hari, segera diurus"
        else -> Fmt.remaining(due)
    }
}
