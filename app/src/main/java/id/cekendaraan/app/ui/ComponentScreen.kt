package id.cekendaraan.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.cekendaraan.app.data.Component
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Prefs
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.domain.DueLimit
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
import id.cekendaraan.app.ui.theme.Gradients
import id.cekendaraan.app.ui.theme.Type
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Detail item (Layar 2) conforming strictly to docs/DESIGN.md & art/mockup-tema.html (Neon Cockpit):
 * - Top bar (kembali · "Detail item" · menu opsi)
 * - Cincin gauge (GaugeRing 176dp) dengan radial glow status, glyph 26dp, angka sisa (Display 36), dan satuan
 * - Di bawah cincin: nama item (H1), kategori · kendaraan (Meta), dan StatusTag
 * - Dua kartu mini berdampingan (Jarak & Waktu) dengan progress 5dp dan fraksi pemakaian
 * - InfoCard (kunci-nilai: Estimasi ganti, Servis terakhir, Odometer saat servis, Interval)
 * - Baris tip perawatan (lampu + saran pabrikan)
 * - Riwayat servis (maksimal 3 servis terakhir dengan dot indicator)
 * - Kartu catatan (Notes)
 * - Bilah aksi bawah tetap (Ubah interval · Catat servis)
 */
@Composable
fun ComponentDetailScreen(
    item: ComponentUi,
    ui: UiState,
    onBack: () -> Unit,
    onSave: (Component) -> Unit,
    onDelete: () -> Unit,
    onService: () -> Unit,
    onAddPastService: () -> Unit,
) {
    val component = item.component
    val due = item.due
    val context = LocalContext.current
    val template = Seed.template(component.templateKey)
    val kind = Seed.kindOf(component.templateKey, component.category)
    val events = ui.eventsFor(component.id)
    val vehicle = ui.vehicle

    var adjustOpen by rememberSaveable(component.id) { mutableStateOf(false) }
    var note by rememberSaveable(component.id) { mutableStateOf(Prefs.note(context, component.id)) }

    val lastServiceEvent = events.firstOrNull()?.event
    val lastServiceDate = lastServiceEvent?.servicedOn?.let(Fmt::date)

    val vehicleOdo = vehicle?.odometerKm ?: 0
    val usedKm = component.lastServiceOdo?.let { (vehicleOdo - it).coerceAtLeast(0) } ?: 0
    val usedMonths = component.lastServiceOn?.let {
        ChronoUnit.MONTHS.between(LocalDate.ofEpochDay(it), LocalDate.now()).toInt().coerceAtLeast(0)
    } ?: 0

    // Telemetry text for GaugeRing center
    val (remainingVal, remainingUnit) = remember(due) {
        when {
            due.status == DueStatus.UNKNOWN -> "—" to "Belum ada catatan"
            due.status == DueStatus.NONE -> "—" to "Tanpa interval"
            due.remainingKm != null && (due.limit == DueLimit.KM || due.remainingDays == null) -> {
                val km = due.remainingKm
                if (km >= 0) Fmt.number(km) to "km lagi" else Fmt.number(abs(km)) to "km lewat"
            }
            due.remainingDays != null -> {
                val days = due.remainingDays
                if (days >= 0) "$days" to "hari lagi" else "${abs(days)}" to "hari lewat"
            }
            else -> "—" to "Tanpa interval"
        }
    }

    val remainingRatio = remember(due) {
        if (due.status == DueStatus.UNKNOWN || due.status == DueStatus.NONE) 1f
        else (1f - due.ratio).coerceIn(0f, 1f)
    }

    val statusColor = statusColor(due.status)
    val scrollState = rememberScrollState()

    Box(
        Modifier
            .fillMaxSize()
            .background(C.bg),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .imePadding(),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton42(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    onClick = onBack,
                )
                Text(
                    text = "Detail item",
                    style = Type.H2,
                )
                IconButton42(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = "Menu opsi",
                    onClick = { adjustOpen = true },
                )
            }

            Spacer(Modifier.height(10.dp))

            // Hero: Gauge Ring, Name, Category, Status Tag
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GaugeRing(
                    remainingValue = remainingVal,
                    unitText = remainingUnit,
                    remainingRatio = remainingRatio,
                    statusColor = statusColor,
                    iconKey = component.icon,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = component.name,
                    style = Type.H1,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                val vehicleTitle = listOfNotNull(vehicle?.brand, vehicle?.model).filter { it.isNotBlank() }.joinToString(" ")
                val metaDesc = if (vehicleTitle.isNotBlank()) "${categoryLabel(kind)} · $vehicleTitle" else categoryLabel(kind)
                Text(
                    text = metaDesc,
                    style = Type.Meta.copy(color = C.t2),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                StatusTag(
                    status = due.status,
                    customLabel = if (due.status == DueStatus.DUE) "Perlu dicek segera" else null,
                )

                Spacer(Modifier.height(22.dp))
            }

            // Pair of mini metric cards (Jarak & Waktu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Jarak Mini Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(C.s1)
                        .padding(14.dp),
                ) {
                    Column {
                        Text("Jarak", style = Type.Label, color = C.t3)
                        Spacer(Modifier.height(4.dp))
                        if (component.intervalKm != null && component.intervalKm > 0) {
                            val kmPercent = (due.progressKm * 100).roundToInt().coerceIn(0, 999)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$kmPercent",
                                    style = Type.Stat,
                                    color = C.t1,
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "% terpakai",
                                    style = Type.Caption.copy(fontWeight = FontWeight.W600, color = C.t3),
                                    modifier = Modifier.padding(bottom = 1.dp),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            val kmStatusColor = when {
                                due.progressKm >= 1f -> C.bad
                                due.progressKm >= 0.8f -> C.warn
                                else -> C.ok
                            }
                            ProgressBar(progress = due.progressKm, color = kmStatusColor)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${Fmt.number(usedKm)} / ${Fmt.number(component.intervalKm)} km",
                                style = Type.Caption.copy(color = C.t3),
                                maxLines = 1,
                                softWrap = false,
                            )
                        } else {
                            Text(
                                text = "Tidak dipantau",
                                style = Type.Body.copy(color = C.t3),
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                }

                // Waktu Mini Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(C.s1)
                        .padding(14.dp),
                ) {
                    Column {
                        Text("Waktu", style = Type.Label, color = C.t3)
                        Spacer(Modifier.height(4.dp))
                        if (component.intervalMonths != null && component.intervalMonths > 0) {
                            val timePercent = (due.progressTime * 100).roundToInt().coerceIn(0, 999)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$timePercent",
                                    style = Type.Stat,
                                    color = C.t1,
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "% terpakai",
                                    style = Type.Caption.copy(fontWeight = FontWeight.W600, color = C.t3),
                                    modifier = Modifier.padding(bottom = 1.dp),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            val timeStatusColor = when {
                                due.progressTime >= 1f -> C.bad
                                due.progressTime >= 0.8f -> C.warn
                                else -> C.ok
                            }
                            ProgressBar(progress = due.progressTime, color = timeStatusColor)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "$usedMonths / ${component.intervalMonths} bulan",
                                style = Type.Caption.copy(color = C.t3),
                                maxLines = 1,
                                softWrap = false,
                            )
                        } else {
                            Text(
                                text = "Tidak dipantau",
                                style = Type.Body.copy(color = C.t3),
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Key-Value InfoCard
            Box(Modifier.padding(horizontal = 20.dp)) {
                InfoCard(
                    items = listOf(
                        InfoRowItem(
                            label = "Estimasi ganti",
                            value = due.dueDate?.let { Fmt.monthYearShort(it) } ?: "—",
                        ),
                        InfoRowItem(
                            label = if (kind == Kind.PAPERWORK) "Terakhir diurus" else "Servis terakhir",
                            value = lastServiceDate ?: "Belum ada",
                        ),
                        InfoRowItem(
                            label = "Odometer saat servis",
                            value = component.lastServiceOdo?.let { "${Fmt.number(it)} km" } ?: "—",
                        ),
                        InfoRowItem(
                            label = "Interval",
                            value = Fmt.interval(component.intervalKm, component.intervalMonths),
                        ),
                    ),
                )
            }

            // Tip line (Saran perawatan)
            val tip = template?.tip?.takeIf { it.isNotBlank() }
            if (tip != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = C.acc,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp),
                    )
                    Text(
                        text = tip,
                        style = Type.Meta.copy(color = C.t2, lineHeight = 20.sp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Riwayat Servis (maks 3 item)
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Riwayat",
                        style = Type.H2,
                    )
                    if (events.isNotEmpty()) {
                        Text(
                            text = "Tambah lama",
                            style = Type.Small.copy(fontWeight = FontWeight.W700, color = C.acc),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onAddPastService)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(C.s1)
                        .padding(16.dp),
                ) {
                    if (events.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Belum ada riwayat servis untuk item ini.",
                                style = Type.Meta.copy(color = C.t3),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(10.dp))
                            SecondaryButton(
                                text = "＋ Tambah servis lama",
                                onClick = onAddPastService,
                                modifier = Modifier.width(200.dp),
                            )
                        }
                    } else {
                        val recentEvents = events.take(3)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recentEvents.forEachIndexed { idx, record ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Indicator dot (8dp)
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == 0) C.acc else C.s3),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.event.note.ifBlank { "Servis ${component.name}" },
                                            style = Type.Item,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "${Fmt.date(record.event.servicedOn)} · ${Fmt.number(record.event.odometerKm)} km",
                                            style = Type.Caption.copy(color = C.t3),
                                        )
                                    }
                                    if (record.event.cost != null && record.event.cost > 0) {
                                        Text(
                                            text = Fmt.money(record.event.cost),
                                            style = Type.Small.copy(fontWeight = FontWeight.W700, color = C.t1),
                                        )
                                    }
                                }
                                if (idx < recentEvents.lastIndex) {
                                    HorizontalDivider(color = C.div, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Catatan (Notes)
            Box(Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(C.s1)
                        .padding(16.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = C.t3,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Catatan", style = Type.Item)
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(C.s2)
                                .padding(12.dp),
                        ) {
                            if (note.isEmpty()) {
                                Text(
                                    text = "Tambahkan catatan untuk item ini...",
                                    style = Type.Body.copy(color = C.t3),
                                )
                            }
                            BasicTextField(
                                value = note,
                                onValueChange = {
                                    note = it.take(300)
                                    Prefs.setNote(context, component.id, note)
                                },
                                textStyle = Type.Body.copy(color = C.t1),
                                cursorBrush = SolidColor(C.acc),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            // Bottom clearance for the fixed action bar
            Spacer(Modifier.height(120.dp))
        }

        // Fixed Action Bar at Bottom (Ubah interval · Catat servis)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.38f to C.bg.copy(alpha = 0.90f),
                        1.0f to C.bg,
                    ),
                )
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SecondaryButton(
                    text = "Ubah interval",
                    onClick = { adjustOpen = true },
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = if (kind == Kind.PAPERWORK) "Tandai diurus" else "Catat servis",
                    onClick = onService,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (adjustOpen) {
        IntervalAdjustSheet(
            item = item,
            onDismiss = { adjustOpen = false },
            onSave = { km, months ->
                onSave(component.copy(intervalKm = km, intervalMonths = months))
            },
            onDelete = onDelete,
        )
    }
}

private fun categoryLabel(kind: Kind): String = when (kind) {
    Kind.COMPONENT -> "Komponen"
    Kind.TREATMENT -> "Perawatan"
    Kind.PAPERWORK -> "Dokumen"
}

/** Confirmation with a slide-to-confirm control, like a physical switch. */
@Composable
fun SlideToServiceDialog(names: List<String>, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(C.s1)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Servis komponen?", style = Type.H1, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(
                "Yakin ingin menandai item ini sudah diservis hari ini?",
                style = Type.Body.copy(color = C.t2),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(C.s2)
                    .padding(14.dp),
            ) {
                Text("Daftar komponen:", style = Type.Label, color = C.t3)
                Spacer(Modifier.height(6.dp))
                names.forEach {
                    Text("• $it", style = Type.Item, color = C.t1)
                }
            }
            Spacer(Modifier.height(24.dp))
            SlideToConfirm(label = "Geser untuk catat", onConfirm = onConfirm)
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onDismiss) {
                Text("Batal", style = Type.Button.copy(color = C.t2))
            }
        }
    }
}

@Composable
private fun SlideToConfirm(label: String, onConfirm: () -> Unit) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val density = LocalDensity.current
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(CircleShape)
            .background(C.s2),
    ) {
        val knob = 50.dp
        val maxPx = with(density) { (maxWidth - knob - 8.dp).toPx() }
        Text(
            text = label,
            style = Type.Button.copy(color = C.t2),
            modifier = Modifier.align(Alignment.Center).padding(start = 32.dp),
        )
        Box(
            Modifier
                .padding(4.dp)
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .size(knob)
                .clip(CircleShape)
                .background(Gradients.Accent)
                .pointerInput(maxPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offset.value > maxPx * 0.85f) {
                                    offset.animateTo(maxPx)
                                    onConfirm()
                                } else {
                                    offset.animateTo(0f)
                                }
                            }
                        },
                    ) { change, drag ->
                        change.consume()
                        scope.launch { offset.snapTo((offset.value + drag).coerceIn(0f, maxPx)) }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = "Geser",
                tint = C.ink,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
