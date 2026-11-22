package id.cekendaraan.app.ui

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.data.Vehicle
import id.cekendaraan.app.data.VehicleKind
import androidx.compose.material.icons.rounded.DirectionsCar
import id.cekendaraan.app.ui.theme.C

@Composable
fun GarageScreen(
    ui: UiState,
    onBack: (() -> Unit)? = null,
    onUpdateOdometer: () -> Unit,
    onSaveVehicle: (Vehicle) -> Unit,
    onResetAll: () -> Unit,
    title: String = "Akun",
) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    val reminders = rememberReminderToggle()
    var page by rememberSaveable { mutableIntStateOf(0) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(C.Bg).statusBarsPadding()) {
        CenterTopBar(title = title, onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(C.Surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(C.Purple),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Pengendara", color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text("Mode offline", color = C.Muted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            SegmentedControl(
                options = listOf("Kelola", "Pengaturan", "Bantuan"),
                selected = page,
                onSelect = { page = it },
                height = 44.dp,
                fontSize = 16,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(26.dp))

            when (page) {
                0 -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Kelola Kendaraan",
                            color = C.Muted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                        )
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(C.BlueSoft)
                                .clickable { editing = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Ubah kendaraan", tint = C.Blue, modifier = Modifier.size(19.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(C.Surface)
                            .padding(16.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(C.BlueDeep)
                                .border(1.dp, C.Edge, RoundedCornerShape(26.dp))
                                .clickable { editing = true }
                                .padding(horizontal = 24.dp, vertical = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (VehicleKind.of(vehicle.kind).isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                                contentDescription = null,
                                tint = C.Blue,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.width(22.dp))
                            Column {
                                Text(vehicle.name, color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOf("${vehicle.brand} ${vehicle.model}".trim(), VehicleKind.of(vehicle.kind).label)
                                        .filter { it.isNotBlank() }.joinToString(" · "),
                                    color = C.Muted,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    GroupLabel("Kelola Data")
                    RowCard(
                        icon = Icons.Rounded.Speed,
                        iconColor = C.Blue,
                        title = "Perbarui Odometer",
                        subtitle = "Sekarang ${Fmt.number(vehicle.odometerKm)} km",
                        onClick = onUpdateOdometer,
                    )
                    Spacer(Modifier.height(12.dp))
                    RowCard(
                        icon = Icons.Rounded.DeleteForever,
                        iconColor = C.Red,
                        title = "Hapus Semua Data",
                        subtitle = "Kendaraan, komponen & riwayat",
                        onClick = { confirmReset = true },
                    )
                }

                1 -> {
                    GroupLabel("Notifikasi")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .background(C.Surface)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = C.Amber, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pengingat Servis", color = C.Text, fontSize = 18.sp)
                            Text("Cek harian pukul 08.00", color = C.Muted, fontSize = 14.sp)
                        }
                        Switch(
                            checked = reminders.enabled,
                            onCheckedChange = reminders.set,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = C.Green,
                                checkedThumbColor = Color.White,
                                uncheckedTrackColor = C.Surface2,
                                uncheckedThumbColor = C.Muted,
                                uncheckedBorderColor = C.Surface2,
                            ),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    GroupLabel("Kendaraan")
                    RowCard(
                        icon = Icons.Rounded.Speed,
                        iconColor = C.Blue,
                        title = "Rata-rata Pemakaian",
                        subtitle = "${Fmt.number(vehicle.monthlyKm)} km per bulan",
                        onClick = { editing = true },
                    )
                }

                else -> {
                    GroupLabel("Cara Kerja")
                    HelpCard(
                        "Hitungan ganda",
                        "Setiap item dihitung dari jarak (km) dan waktu (bulan). Yang lebih dulu habis menentukan kondisinya.",
                    )
                    Spacer(Modifier.height(12.dp))
                    HelpCard(
                        "Perbarui odometer",
                        "Ketuk angka odometer di beranda setiap kali isi bensin agar sisa km tetap akurat.",
                    )
                    Spacer(Modifier.height(12.dp))
                    HelpCard(
                        "Geser untuk servis",
                        "Setelah ganti komponen, buka komponennya lalu geser tombol untuk mencatat servis hari ini.",
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            Text(
                "Setelin v0.1.0",
                color = C.Muted,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(130.dp))
        }
    }

    if (editing) {
        VehicleEditSheet(
            vehicle = vehicle,
            onDismiss = { editing = false },
            onSave = {
                onSaveVehicle(it)
                editing = false
                Toast.makeText(context, "Kendaraan diperbarui", Toast.LENGTH_SHORT).show()
            },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = C.Surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Hapus semua data?", color = C.Text, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Kendaraan, komponen, dan seluruh riwayat servis dihapus dari perangkat. Kamu akan mulai dari awal.",
                    color = C.Muted,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onResetAll()
                }) { Text("Hapus semua", color = C.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Batal", color = C.Muted) }
            },
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text,
        color = C.Muted,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
    )
}

@Composable
private fun RowCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(C.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(iconColor), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = C.Text, fontSize = 17.sp, maxLines = 1)
            Text(subtitle, color = C.Muted, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
private fun HelpCard(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(C.Surface)
            .padding(20.dp),
    ) {
        Text(title, color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, color = C.Muted, fontSize = 15.sp, lineHeight = 21.sp)
    }
}
