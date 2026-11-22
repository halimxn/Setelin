package id.cekendaraan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.data.ComponentTemplate
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.ui.theme.C
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Setup page shown before a template starts being tracked. [template] null means a custom
 * item of [customKind].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentSetupScreen(
    template: ComponentTemplate?,
    customKind: Kind,
    onBack: () -> Unit,
    onSave: (ComponentTemplate, LocalDate) -> Unit,
) {
    val kind = template?.kind ?: customKind
    val paperwork = kind == Kind.PAPERWORK
    val defaultIcon = when (kind) {
        Kind.COMPONENT -> "service"
        Kind.TREATMENT -> "tune"
        Kind.PAPERWORK -> "doc"
    }
    var name by rememberSaveable { mutableStateOf(template?.name ?: "") }
    var kmText by rememberSaveable { mutableStateOf(template?.km?.toString() ?: "") }
    var monthText by rememberSaveable { mutableStateOf(template?.months?.toString() ?: "") }
    var dueEpoch by rememberSaveable {
        mutableLongStateOf(LocalDate.now().plusMonths((template?.months ?: 12).toLong()).toEpochDay())
    }
    var pickDate by rememberSaveable { mutableStateOf(false) }

    val km = kmText.toIntOrNull()?.takeIf { it > 0 }
    val months = monthText.toIntOrNull()?.takeIf { it > 0 }
    val valid = name.isNotBlank() && (if (paperwork) months != null else (km != null || months != null))
    val title = template?.name ?: when (kind) {
        Kind.COMPONENT -> "Komponen Kustom"
        Kind.TREATMENT -> "Perawatan Kustom"
        Kind.PAPERWORK -> "Dokumen Kustom"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(C.Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        CenterTopBar(title = title, onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(C.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    componentIcon(template?.icon ?: defaultIcon),
                    contentDescription = null,
                    tint = C.Text,
                    modifier = Modifier.size(54.dp),
                )
            }
            Spacer(Modifier.height(26.dp))

            if (template != null && template.risk.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(C.Surface)
                        .border(1.dp, C.LineStrong, RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = C.Red, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (paperwork) "JIKA TERLAMBAT" else "JIKA TIDAK DIGANTI",
                            color = C.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(template.risk, color = C.Muted, fontSize = 15.sp, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(18.dp))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(C.Surface)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                if (!paperwork) {
                    IntervalRow(
                        title = "Interval Jarak (km)",
                        hint = template?.km?.let { "Rekomendasi: ${Fmt.number(it)} km" } ?: "Kosongkan bila tidak dipakai",
                        value = kmText,
                        onValue = { kmText = it },
                    )
                    Divider()
                }
                IntervalRow(
                    title = if (paperwork) "Masa Berlaku (Bulan)" else "Interval Waktu (Bulan)",
                    hint = template?.months?.let { "Rekomendasi: Tiap $it bulan" } ?: "Kosongkan bila tidak dipakai",
                    value = monthText,
                    onValue = { monthText = it },
                )
                Divider()
                if (paperwork) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { pickDate = true }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Berlaku Sampai", color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Tanggal jatuh tempo berikutnya", color = C.Muted, fontSize = 14.sp)
                        }
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(C.Inset)
                                .border(1.dp, C.Line, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = C.Blue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Fmt.date(dueEpoch), color = C.Text, fontSize = 15.sp)
                        }
                    }
                    Divider()
                }
                Row(Modifier.padding(vertical = 16.dp)) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = C.Muted, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (paperwork) {
                            "Pengingat muncul menjelang tanggal jatuh tempo."
                        } else {
                            "Umur pakai komponen adalah perkiraan umum. Cek buku manual untuk angka yang akurat."
                        },
                        color = C.Muted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
                Divider()
                // Amount: every template is tracked once per vehicle, so the stepper is fixed at 1.
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Jumlah", color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text("Item ini hanya bisa ditambahkan sekali", color = C.Muted, fontSize = 13.sp, lineHeight = 17.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Row(
                        Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(C.Bg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = null, tint = C.Surface3, modifier = Modifier.padding(horizontal = 12.dp).size(18.dp))
                        Text("1", color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = C.Surface3, modifier = Modifier.padding(horizontal = 12.dp).size(18.dp))
                    }
                }
                Divider()
                Spacer(Modifier.height(16.dp))
                Text(
                    when (kind) {
                        Kind.PAPERWORK -> "Nama Dokumen"
                        Kind.TREATMENT -> "Nama Perawatan"
                        Kind.COMPONENT -> "Nama Komponen"
                    },
                    color = C.Muted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(C.Bg),
                ) {
                    PillField(value = name, onValueChange = { name = it.take(40) }, placeholder = "Contoh: Ganti oli")
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
        Box(
            Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
        ) {
            PrimaryButton(
                text = "Simpan",
                enabled = valid,
                onClick = {
                    val base = template ?: ComponentTemplate(
                        key = "custom_${System.currentTimeMillis()}",
                        name = name,
                        icon = defaultIcon,
                        category = when (kind) {
                            Kind.COMPONENT -> "Lainnya"
                            Kind.TREATMENT -> "Perawatan"
                            Kind.PAPERWORK -> "Dokumen"
                        },
                        km = null,
                        months = null,
                        kind = kind,
                    )
                    val start = if (paperwork && months != null) {
                        LocalDate.ofEpochDay(dueEpoch).minusMonths(months.toLong())
                    } else {
                        LocalDate.now()
                    }
                    onSave(
                        base.copy(name = name.trim(), km = if (paperwork) null else km, months = months),
                        start,
                    )
                },
            )
        }
    }

    if (pickDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = dueEpoch * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker.selectedDateMillis?.let {
                            dueEpoch = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                        }
                        pickDate = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = C.Blue),
                ) { Text("Pilih", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pickDate = false }, colors = ButtonDefaults.textButtonColors(contentColor = C.Muted)) {
                    Text("Batal")
                }
            },
        ) { DatePicker(picker) }
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = C.LineStrong, thickness = 1.dp)

@Composable
fun IntervalRow(title: String, hint: String, value: String, onValue: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(hint, color = C.Muted, fontSize = 13.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(12.dp))
        BoxNumberField(value = value, onValueChange = onValue)
    }
}
