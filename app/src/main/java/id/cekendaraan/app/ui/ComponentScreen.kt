package id.cekendaraan.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.cekendaraan.app.data.Component
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Prefs
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    var page by rememberSaveable(component.id) { mutableStateOf(0) }
    var adjustOpen by rememberSaveable(component.id) { mutableStateOf(false) }
    var kmText by rememberSaveable(component.id) { mutableStateOf(component.intervalKm?.toString().orEmpty()) }
    var monthText by rememberSaveable(component.id) { mutableStateOf(component.intervalMonths?.toString().orEmpty()) }
    var note by rememberSaveable(component.id) { mutableStateOf(Prefs.note(context, component.id)) }
    var confirmDelete by remember { mutableStateOf(false) }

    val lastService = events.firstOrNull()?.event?.servicedOn?.let(Fmt::date)

    Column(
        Modifier
            .fillMaxSize()
            .background(C.Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Box(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.Text)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(componentIcon(component.icon), contentDescription = null, tint = C.Text, modifier = Modifier.size(60.dp))
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(component.name, color = C.Text, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = C.Muted)) {
                                append(if (kind == Kind.PAPERWORK) "Terakhir diurus: " else "Servis terakhir: ")
                            }
                            withStyle(SpanStyle(color = C.Text)) { append(lastService ?: "Belum ada") }
                        },
                        fontSize = 16.sp,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            SegmentedControl(
                options = listOf("Saat Ini", "Riwayat"),
                selected = page,
                onSelect = { page = it },
                height = 44.dp,
                fontSize = 16,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            if (page == 0) {
                val tip = template?.tip?.takeIf { it.isNotEmpty() }
                if (tip != null) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(C.Surface)
                            .border(1.dp, C.LineStrong, RoundedCornerShape(24.dp))
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = C.Blue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Saran perawatan", color = C.Blue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(tip, color = C.Muted, fontSize = 14.sp, lineHeight = 19.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(C.Surface)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                ) {
                    if (due.status == DueStatus.UNKNOWN) {
                        Text("Belum ada data servis", color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Catat kapan terakhir diservis supaya sisa pakainya bisa dihitung.",
                            color = C.Muted,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                        )
                    } else {
                        var first = true
                        if (component.intervalKm != null && due.remainingKm != null) {
                            val left = 1f - due.progressKm
                            UsageLine(
                                icon = null,
                                label = "Sisa Pakai:",
                                value = if (due.remainingKm >= 0) {
                                    "${Fmt.number(due.remainingKm)} km"
                                } else {
                                    "Lewat ${Fmt.number(-due.remainingKm)} km"
                                },
                                left = left,
                            )
                            first = false
                        }
                        if (component.intervalMonths != null && due.dueDate != null) {
                            if (!first) {
                                Spacer(Modifier.height(24.dp))
                                HorizontalDivider(color = C.LineStrong)
                                Spacer(Modifier.height(24.dp))
                            }
                            val days = due.remainingDays ?: 0
                            UsageLine(
                                icon = Icons.Rounded.CalendarMonth,
                                label = "Sampai",
                                value = "${Fmt.date(due.dueDate)} · ${days} hr",
                                left = 1f - due.progressTime,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = C.Muted, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Beranda menampilkan yang lebih dulu habis dari kedua ukuran ini untuk menentukan kondisi.",
                                color = C.Muted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(C.Surface2)
                            .clickable { adjustOpen = !adjustOpen },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Atur Umur Pakai", color = C.Blue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            if (adjustOpen) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = C.Blue,
                        )
                    }
                    AnimatedVisibility(adjustOpen) {
                        Column {
                            Spacer(Modifier.height(4.dp))
                            if (kind != Kind.PAPERWORK) {
                                IntervalRow(
                                    title = "Interval Jarak (km)",
                                    hint = template?.km?.let { "Rekomendasi: ${Fmt.number(it)} km" } ?: "Kosongkan bila tidak dipakai",
                                    value = kmText,
                                    onValue = { kmText = it },
                                )
                                HorizontalDivider(color = C.LineStrong)
                            }
                            IntervalRow(
                                title = "Interval Waktu (Bulan)",
                                hint = template?.months?.let { "Rekomendasi: Tiap $it bulan" } ?: "Kosongkan bila tidak dipakai",
                                value = monthText,
                                onValue = { monthText = it },
                            )
                            val km = kmText.toIntOrNull()?.takeIf { it > 0 }
                            val months = monthText.toIntOrNull()?.takeIf { it > 0 }
                            Spacer(Modifier.height(8.dp))
                            PrimaryButton(
                                text = "Simpan Umur Pakai",
                                enabled = km != null || months != null,
                                onClick = {
                                    onSave(component.copy(intervalKm = km, intervalMonths = months))
                                    adjustOpen = false
                                },
                            )
                            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Hapus dari daftar", color = C.Red, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(C.Surface)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = C.Muted, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(14.dp))
                        Text("Catatan", color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(C.Bg)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        if (note.isEmpty()) {
                            Text("Tambahkan catatan tentang komponen ini…", color = C.Muted2, fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = note,
                            onValueChange = {
                                note = it.take(300)
                                Prefs.setNote(context, component.id, note)
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = C.Text, fontSize = 15.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(C.Blue),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            } else {
                if (events.isEmpty()) {
                    Spacer(Modifier.height(40.dp))
                    EmptyState(
                        icon = Icons.Rounded.Schedule,
                        title = "Belum Ada Riwayat",
                        message = "Item ini belum pernah diservis.",
                        actionLabel = "Tambah Servis Lama",
                        onAction = onAddPastService,
                    )
                } else {
                    events.forEach { record ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(C.Surface)
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(C.BlueSoft),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Build, contentDescription = null, tint = C.Blue, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(Fmt.date(record.event.servicedOn), color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = C.Muted, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${Fmt.number(record.event.odometerKm)} km", color = C.Muted, fontSize = 14.sp)
                                    record.event.cost?.let {
                                        Text("  ·  ${Fmt.money(it)}", color = C.Muted, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    SecondaryButton("Tambah Servis Lama", onAddPastService)
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
        if (page == 0) {
            Box(
                Modifier
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            ) {
                if (due.status == DueStatus.UNKNOWN) {
                    PrimaryButton(text = "Catat Servis Terakhir", onClick = onAddPastService)
                } else {
                    PrimaryButton(
                        text = if (kind == Kind.PAPERWORK) "Tandai Sudah Diurus" else "Servis Komponen",
                        onClick = onService,
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = C.Surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Hapus ${component.name}?", color = C.Text, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Item berhenti dipantau. Riwayat servis yang sudah dicatat tetap tersimpan.",
                    color = C.Muted,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Hapus", color = C.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Batal", color = C.Muted) }
            },
        )
    }
}

@Composable
private fun UsageLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    value: String,
    left: Float,
) {
    val fraction = left.coerceIn(0f, 1f)
    val color = when {
        fraction <= 0f -> C.Red
        fraction <= 0.2f -> C.Amber
        else -> C.Green
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = C.Muted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, color = C.Muted, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(value, color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
        Spacer(Modifier.width(8.dp))
        Text("${(fraction * 100).roundToInt()}%", color = C.Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(10.dp))
    ProgressLine(fraction, color, height = 6)
}

/** Confirmation with a slide-to-confirm control, like a physical switch. */
@Composable
fun SlideToServiceDialog(names: List<String>, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(34.dp))
                .background(C.Surface)
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Servis Komponen?", color = C.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                "Yakin ingin menandai item ini sudah diservis hari ini?",
                color = C.Muted,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            Text("Komponen:", color = C.Muted, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            names.forEach {
                Text("- $it", color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
            SlideToConfirm(label = "Geser untuk Servis", onConfirm = onConfirm)
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = onDismiss) {
                Text("Batal", color = C.Muted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            .height(64.dp)
            .clip(CircleShape)
            .background(C.Surface2),
    ) {
        val knob = 56.dp
        val maxPx = with(density) { (maxWidth - knob - 8.dp).toPx() }
        Text(
            label,
            color = C.Blue.copy(alpha = (1f - offset.value / maxPx.coerceAtLeast(1f)).coerceIn(0.2f, 1f)),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center).padding(start = 48.dp),
        )
        Box(
            Modifier
                .padding(4.dp)
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .size(knob)
                .clip(CircleShape)
                .background(C.BlueButton)
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
            Icon(Icons.Outlined.Build, contentDescription = "Geser", tint = C.OnAccent, modifier = Modifier.size(26.dp))
        }
    }
}
