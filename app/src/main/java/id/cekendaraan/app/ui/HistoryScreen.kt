package id.cekendaraan.app.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.data.ServiceEventWithItems
import id.cekendaraan.app.ui.theme.C
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HistoryScreen(
    ui: UiState,
    onLogService: () -> Unit,
    onDeleteEvent: (Long) -> Unit,
) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    var open by remember { mutableStateOf<ServiceEventWithItems?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    fun share() {
        val text = buildString {
            appendLine("Riwayat servis ${vehicle.name}")
            appendLine()
            ui.events.forEach { e ->
                appendLine("${Fmt.date(e.event.servicedOn)} · ${Fmt.number(e.event.odometerKm)} km")
                e.items.forEach { appendLine("  - ${it.componentName}") }
                e.event.cost?.let { appendLine("  Biaya: ${Fmt.money(it)}") }
                if (e.event.note.isNotBlank()) appendLine("  Catatan: ${e.event.note}")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Bagikan riwayat"))
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
        ) {
            Text(
                "Riwayat ${vehicle.name}",
                color = C.Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 100.dp),
            )
            Row(Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = ::share, enabled = ui.events.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.IosShare,
                        contentDescription = "Bagikan",
                        tint = if (ui.events.isNotEmpty()) C.Blue else C.Blue.copy(alpha = 0.35f),
                    )
                }
                IconButton(onClick = onLogService) {
                    Icon(Icons.Rounded.Add, contentDescription = "Tambah servis lama", tint = C.Blue, modifier = Modifier.size(30.dp))
                }
            }
        }

        if (ui.events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(bottom = 140.dp), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Rounded.Schedule,
                    title = "Belum Ada Riwayat Servis",
                    message = "Kamu belum menservis komponen apa pun.",
                    actionLabel = "Tambah Servis Lama",
                    onAction = onLogService,
                )
            }
        } else {
            val grouped = ui.events.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.event.servicedOn)) }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                grouped.forEach { (month, events) ->
                    item(key = month.toString()) {
                        Text(
                            Fmt.monthYear(month.atDay(1).toEpochDay()),
                            color = C.Muted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 6.dp, top = 8.dp),
                        )
                    }
                    items(events, key = { it.event.id }) { record ->
                        EventCard(record) { open = record }
                    }
                }
            }
        }
    }

    open?.let { record ->
        AlertDialog(
            onDismissRequest = { open = null },
            containerColor = C.Surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text(Fmt.date(record.event.servicedOn), color = C.Text, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("${Fmt.number(record.event.odometerKm)} km", color = C.Muted, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    record.items.forEach {
                        Text("- ${it.componentName}", color = C.Text, fontSize = 16.sp)
                    }
                    record.event.cost?.let {
                        Spacer(Modifier.height(12.dp))
                        Text("Biaya: ${Fmt.money(it)}", color = C.Text, fontSize = 15.sp)
                    }
                    if (record.event.note.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(record.event.note, color = C.Muted, fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = null }) { Text("Tutup", color = C.Blue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = true }) { Text("Hapus", color = C.Red) }
            },
        )
    }

    if (confirmDelete) {
        val record = open
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = C.Surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Hapus catatan servis?", color = C.Text, fontWeight = FontWeight.Bold) },
            text = { Text("Hitungan komponen akan kembali ke servis sebelumnya.", color = C.Muted) },
            confirmButton = {
                TextButton(onClick = {
                    record?.let { onDeleteEvent(it.event.id) }
                    confirmDelete = false
                    open = null
                }) { Text("Hapus", color = C.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Batal", color = C.Muted) }
            },
        )
    }
}

@Composable
private fun EventCard(record: ServiceEventWithItems, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(C.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(C.Surface2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(Fmt.day(record.event.servicedOn), color = C.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(Fmt.month(record.event.servicedOn), color = C.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                record.items.joinToString(", ") { it.componentName },
                color = C.Text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Speed, contentDescription = null, tint = C.Muted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("${Fmt.number(record.event.odometerKm)} km", color = C.Muted, fontSize = 14.sp)
                record.event.cost?.let { Text("  ·  ${Fmt.money(it)}", color = C.Muted, fontSize = 14.sp) }
            }
        }
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(C.GreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text("${record.items.size}", color = C.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
