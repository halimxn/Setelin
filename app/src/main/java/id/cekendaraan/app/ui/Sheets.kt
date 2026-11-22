package id.cekendaraan.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.data.ComponentTemplate
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.data.Vehicle
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.ui.theme.C
import id.cekendaraan.app.ui.theme.Type
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun OdometerDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(vehicle.odometerKm.toString()) }
    val value = text.toIntOrNull()
    val valid = value != null && value >= vehicle.odometerKm && value <= 999_999

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.Surface2,
        titleContentColor = C.Text,
        textContentColor = C.Muted,
        icon = { Icon(Icons.Rounded.Speed, contentDescription = null, tint = C.Lime) },
        title = { Text("Update odometer") },
        text = {
            Column {
                Text(
                    "Odometer sekarang ${Fmt.number(vehicle.odometerKm)} km. Masukkan angka terbaru dari panel Vario.",
                    color = C.Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(6) },
                    label = "Odometer",
                    suffix = "km",
                    keyboardType = KeyboardType.Number,
                    isError = !valid && text.isNotBlank(),
                    supportingText = if (value != null && value < vehicle.odometerKm) {
                        "Odometer tidak boleh turun."
                    } else {
                        "Hanya bisa naik."
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { value?.let(onSave) },
                enabled = valid,
                colors = ButtonDefaults.textButtonColors(contentColor = C.Lime),
            ) { Text("Simpan", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = C.Muted),
            ) { Text("Batal") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleEditSheet(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(vehicle.name) }
    var plate by rememberSaveable { mutableStateOf(vehicle.plate) }
    var brand by rememberSaveable { mutableStateOf(vehicle.brand) }
    var model by rememberSaveable { mutableStateOf(vehicle.model) }
    var yearText by rememberSaveable { mutableStateOf(vehicle.year.toString()) }
    var odoText by rememberSaveable { mutableStateOf(vehicle.odometerKm.toString()) }
    var monthlyText by rememberSaveable { mutableStateOf(vehicle.monthlyKm.toString()) }

    val year = yearText.toIntOrNull()
    val odo = odoText.toIntOrNull()
    val monthly = monthlyText.toIntOrNull()
    val valid = name.isNotBlank() && year != null && year in 1990..2035 &&
        odo != null && odo >= vehicle.odometerKm && odo <= 999_999 &&
        monthly != null && monthly > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = C.Surface,
        contentColor = C.Text,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            SheetHeader(
                title = "Ubah kendaraan",
                subtitle = "Data ini dipakai di seluruh aplikasi.",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                label = "Nama kendaraan",
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    value = brand,
                    onValueChange = { brand = it.take(24) },
                    label = "Merek",
                    placeholder = "Honda",
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = model,
                    onValueChange = { model = it.take(30) },
                    label = "Model",
                    placeholder = "Vario 160",
                    modifier = Modifier.weight(1.3f),
                )
            }
            Spacer(Modifier.height(10.dp))
            AppTextField(
                value = plate,
                onValueChange = { plate = it.take(12).uppercase() },
                label = "Plat nomor",
                placeholder = "Contoh: B 160 EVO",
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter(Char::isDigit).take(4) },
                    label = "Tahun",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = monthlyText,
                    onValueChange = { monthlyText = it.filter(Char::isDigit).take(6) },
                    label = "Rata-rata",
                    suffix = "km/bln",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            AppTextField(
                value = odoText,
                onValueChange = { odoText = it.filter(Char::isDigit).take(6) },
                label = "Odometer",
                suffix = "km",
                keyboardType = KeyboardType.Number,
                isError = odo != null && odo < vehicle.odometerKm,
                supportingText = if (odo != null && odo < vehicle.odometerKm) {
                    "Odometer tidak boleh turun."
                } else {
                    "Update odometer rutin agar hitungan servis akurat."
                },
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Simpan perubahan",
                onClick = {
                    onSave(
                        vehicle.copy(
                            name = name.trim(),
                            brand = brand.trim(),
                            model = model.trim(),
                            plate = plate.trim(),
                            year = year ?: vehicle.year,
                            odometerKm = odo ?: vehicle.odometerKm,
                            monthlyKm = monthly ?: vehicle.monthlyKm,
                        ),
                    )
                },
                enabled = valid,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogServiceSheet(
    ui: UiState,
    preselectId: Long?,
    onDismiss: () -> Unit,
    onSave: (ids: List<Long>, date: LocalDate, odometerKm: Int, cost: Long?, note: String) -> Unit,
) {
    val vehicle = ui.vehicle ?: return
    val initialSelection = remember(preselectId, ui.components) {
        if (preselectId != null) {
            setOf(preselectId)
        } else {
            ui.components
                .filter { it.due.status == DueStatus.DUE || it.due.status == DueStatus.SOON }
                .map { it.component.id }
                .toSet()
        }
    }
    var selected by remember { mutableStateOf(initialSelection) }
    var dateEpoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var odoText by rememberSaveable { mutableStateOf(vehicle.odometerKm.toString()) }
    var costText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val date = LocalDate.ofEpochDay(dateEpoch)
    val odo = odoText.toIntOrNull()
    val valid = selected.isNotEmpty() && odo != null && odo in 0..999_999 && date <= LocalDate.now()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = C.Surface,
        contentColor = C.Text,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            SheetHeader(
                title = "Catat servis",
                subtitle = "Pilih komponen yang dikerjakan.",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "TANGGAL",
                        color = C.Muted2,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    )
                    CalendarPill(
                        text = Fmt.date(dateEpoch),
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                AppTextField(
                    value = odoText,
                    onValueChange = { odoText = it.filter(Char::isDigit).take(6) },
                    label = "Odometer",
                    suffix = "km",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Komponen", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        selected = if (selected.size == ui.components.size) {
                            emptySet()
                        } else {
                            ui.components.map { it.component.id }.toSet()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = C.Lime),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        if (selected.size == ui.components.size) "Kosongkan" else "Pilih semua",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            Surface(
                color = C.Surface2,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, C.Line),
            ) {
                Column {
                    ui.components.forEachIndexed { index, item ->
                        val checked = item.component.id in selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = if (checked) {
                                        selected - item.component.id
                                    } else {
                                        selected + item.component.id
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconWell(item.component.icon, statusColor(item.due.status), size = 36, iconSize = 18)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.component.name,
                                    color = C.Text,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    when (item.due.status) {
                                        DueStatus.DUE -> "Sudah lewat jadwal"
                                        DueStatus.SOON -> "Mendekati jadwal"
                                        DueStatus.UNKNOWN -> "Belum ada catatan"
                                        else -> Fmt.remaining(item.due)
                                    },
                                    color = if (item.due.status == DueStatus.DUE) C.Red else C.Muted2,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Checkbox(
                                checked = checked,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = C.Lime,
                                    checkmarkColor = C.OnLime,
                                    uncheckedColor = C.Muted2,
                                ),
                            )
                        }
                        if (index < ui.components.lastIndex) {
                            HorizontalDivider(color = C.Line.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    value = costText,
                    onValueChange = { costText = it.filter(Char::isDigit).take(9) },
                    label = "Biaya (opsional)",
                    suffix = "Rp",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = note,
                    onValueChange = { note = it.take(120) },
                    label = "Catatan",
                    placeholder = "Bengkel, keluhan…",
                    modifier = Modifier.weight(1.35f),
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "${selected.size} komponen dipilih",
                color = if (selected.isEmpty()) C.Red else C.Muted,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Simpan servis",
                onClick = {
                    val parsedCost = costText.toLongOrNull()?.takeIf { it > 0 }
                    val currentOdo = odo
                    if (valid && currentOdo != null) {
                        onSave(selected.toList(), date, currentOdo, parsedCost, note)
                    }
                },
                enabled = valid,
            )
            Spacer(Modifier.height(10.dp))
            InfoNote(
                "Servis di masa lalu boleh dicatat. Hitungan dipakai dari servis terbaru tiap komponen.",
            )
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showDatePicker) {
        val todayUtc = LocalDate.now().toEpochDay() * 86_400_000L
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = dateEpoch * 86_400_000L,
            yearRange = 2015..LocalDate.now().year,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtc
                override fun isSelectableYear(year: Int): Boolean =
                    year in 2015..LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker.selectedDateMillis?.let { millis ->
                            dateEpoch = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toEpochDay()
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = C.Lime),
                ) { Text("Pilih", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = C.Muted),
                ) { Text("Batal") }
            },
        ) {
            DatePicker(picker)
        }
    }
}

/**
 * Bottom sheet to browse and search untracked templates or add custom items.
 * Replaces the old dashboard grid template catalog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahItemSheet(
    ui: UiState,
    onDismiss: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onCustom: (Kind) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedKindIdx by rememberSaveable { mutableStateOf(0) } // 0: Semua, 1: Komponen, 2: Perawatan, 3: Dokumen

    val targetKind = when (selectedKindIdx) {
        1 -> Kind.COMPONENT
        2 -> Kind.TREATMENT
        3 -> Kind.PAPERWORK
        else -> null
    }

    val trackedKeys = remember(ui.components) {
        ui.components.map { it.component.templateKey }.toSet()
    }

    val availableTemplates = remember(ui.vehicleKind, trackedKeys, query, targetKind) {
        Seed.templates.filter { template ->
            template.key !in trackedKeys &&
                Seed.fits(template, ui.vehicleKind) &&
                (targetKind == null || template.kind == targetKind) &&
                (query.isBlank() || template.name.contains(query, ignoreCase = true) || template.category.contains(query, ignoreCase = true) || template.tip.contains(query, ignoreCase = true))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = C.s1,
        contentColor = C.t1,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            SheetHeader(
                title = "Tambah item",
                subtitle = "Pilih item untuk mulai dipantau.",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(14.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(C.s2)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = C.t3,
                        modifier = Modifier.size(20.dp),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Cari item, misal: oli, rem, busi...",
                                style = Type.Small.copy(color = C.t3),
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it.take(40) },
                            textStyle = Type.Small.copy(color = C.t1),
                            cursorBrush = SolidColor(C.acc),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Hapus",
                            tint = C.t3,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Semua", "Komponen", "Perawatan", "Dokumen").forEachIndexed { idx, label ->
                    Chip(
                        label = label,
                        selected = selectedKindIdx == idx,
                        onClick = { selectedKindIdx = idx },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // List of templates (Scrollable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (availableTemplates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (query.isNotBlank()) "Tidak ada item yang cocok dengan \"$query\"." else "Semua rekomendasi item sudah dipantau.",
                            style = Type.Meta,
                            color = C.t3,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    availableTemplates.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(C.s2)
                                .clickable {
                                    onDismiss()
                                    onSelectTemplate(template.key)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(C.s3),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = componentIcon(template.key),
                                    contentDescription = null,
                                    tint = C.acc,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.name,
                                    style = Type.Item,
                                    color = C.t1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = Fmt.interval(template.km, template.months),
                                    style = Type.Caption,
                                    color = C.t3,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(C.acc.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Pilih",
                                    tint = C.acc,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Custom Item Button
                SecondaryButton(
                    text = "＋ Buat item kustom",
                    onClick = {
                        onDismiss()
                        onCustom(targetKind ?: Kind.COMPONENT)
                    },
                )
            }
        }
    }
}

/**
 * Bottom sheet to adjust component intervals (km & months) or remove from tracking.
 * Replaces the old accordion "Atur Umur Pakai" on ComponentDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalAdjustSheet(
    item: ComponentUi,
    onDismiss: () -> Unit,
    onSave: (km: Int?, months: Int?) -> Unit,
    onDelete: () -> Unit,
) {
    val component = item.component
    val template = Seed.template(component.templateKey)
    val kind = Seed.kindOf(component.templateKey, component.category)

    var kmText by rememberSaveable(component.id) {
        mutableStateOf(component.intervalKm?.toString().orEmpty())
    }
    var monthText by rememberSaveable(component.id) {
        mutableStateOf(component.intervalMonths?.toString().orEmpty())
    }
    var confirmDelete by remember { mutableStateOf(false) }

    val km = kmText.toIntOrNull()?.takeIf { it > 0 }
    val months = monthText.toIntOrNull()?.takeIf { it > 0 }
    val valid = km != null || months != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = C.s1,
        contentColor = C.t1,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            SheetHeader(
                title = "Ubah interval",
                subtitle = "Atur batas pemakaian untuk ${component.name}.",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(16.dp))

            if (kind != Kind.PAPERWORK) {
                AppTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter(Char::isDigit).take(6) },
                    label = "Interval jarak",
                    suffix = "km",
                    keyboardType = KeyboardType.Number,
                    supportingText = template?.km?.let { "Rekomendasi: ${Fmt.number(it)} km" } ?: "Kosongkan jika tidak memakai batas km.",
                )
                Spacer(Modifier.height(12.dp))
            }

            AppTextField(
                value = monthText,
                onValueChange = { monthText = it.filter(Char::isDigit).take(3) },
                label = "Interval waktu",
                suffix = "bulan",
                keyboardType = KeyboardType.Number,
                supportingText = template?.months?.let { "Rekomendasi: Tiap $it bulan" } ?: "Kosongkan jika tidak memakai batas bulan.",
            )

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Simpan interval",
                enabled = valid,
                onClick = {
                    onSave(km, months)
                    onDismiss()
                },
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(
                    text = "Hapus dari daftar",
                    style = Type.Button.copy(color = C.bad),
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = C.s1,
            titleContentColor = C.t1,
            textContentColor = C.t2,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Hapus ${component.name}?", style = Type.H2) },
            text = {
                Text(
                    "Item akan berhenti dipantau. Catatan servis masa lalu tetap tersimpan di jurnal.",
                    style = Type.Body,
                    color = C.t2,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDismiss()
                        onDelete()
                    },
                ) {
                    Text("Hapus", style = Type.Button.copy(color = C.bad))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Batal", style = Type.Button.copy(color = C.t2))
                }
            },
        )
    }
}
