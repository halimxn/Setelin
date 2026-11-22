package id.cekendaraan.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.data.VehicleKind
import androidx.compose.material.icons.rounded.LocalCarWash
import androidx.compose.material.icons.rounded.DirectionsCar
import id.cekendaraan.app.ui.theme.C

/* ---------------- Trip ---------------- */

@Composable
fun TripsScreen(ui: UiState) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    val soon = { Toast.makeText(context, "Pencatatan trip segera hadir", Toast.LENGTH_SHORT).show() }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
            Text(
                "Trip ${vehicle.name}",
                color = C.Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            Modifier
                .height(28.dp)
                .clip(CircleShape)
                .background(C.Surface)
                .clickable(onClick = soon)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(C.Muted))
            Spacer(Modifier.width(6.dp))
            Text("Auto-Track", color = C.Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(C.Surface)
                .clickable(onClick = soon)
                .padding(start = 22.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(C.Surface3))
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("Rekam Manual", color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("Rekam trip sekarang.", color = C.Muted, fontSize = 13.sp, maxLines = 1)
            }
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(C.BlueSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = C.Blue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Segera", color = C.Blue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(40.dp))
        EmptyState(
            icon = Icons.Rounded.Map,
            title = "Belum Ada Trip",
            message = "Trip tercatat saat Auto-Track aktif di kendaraan atau saat dipindahkan dari kendaraan lain.",
        )
    }
}

/* ---------------- Bengkel ---------------- */

private data class ShopQuery(val title: String, val subtitle: String, val query: String, val icon: ImageVector)

/** Map searches that fit the vehicle: the brand's official workshop first. */
private fun shopQueries(brand: String, kind: VehicleKind): List<ShopQuery> {
    val car = !kind.isMotor
    val official = when {
        !car && brand.equals("Honda", ignoreCase = true) -> ShopQuery("AHASS Terdekat", "Bengkel resmi Honda", "AHASS", Icons.Rounded.TwoWheeler)
        brand.isNotBlank() -> ShopQuery(
            "Bengkel Resmi $brand",
            if (car) "Servis berkala & garansi" else "Servis resmi & suku cadang asli",
            "bengkel resmi $brand" + if (car) " mobil" else " motor",
            if (car) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
        )
        else -> null
    }
    return listOfNotNull(
        official,
        if (car) {
            ShopQuery("Bengkel Mobil", "Servis umum & tune up", "bengkel mobil", Icons.Rounded.Build)
        } else {
            ShopQuery("Bengkel Motor", "Servis umum", "bengkel motor", Icons.Rounded.Build)
        },
        ShopQuery("Tambal Ban", "Tubeless & ban dalam", if (car) "tambal ban mobil" else "tambal ban", Icons.Rounded.TireRepair),
        if (car) {
            ShopQuery("Cuci Mobil", "Cuci & salon kendaraan", "cuci mobil", Icons.Rounded.LocalCarWash)
        } else {
            ShopQuery("Toko Oli & Sparepart", "Oli, aki, kampas rem", "toko sparepart motor", Icons.Rounded.Store)
        },
    )
}

@Composable
fun ShopsScreen(ui: UiState, onOpenGarage: () -> Unit) {
    val vehicle = ui.vehicle ?: return
    val context = LocalContext.current
    fun openMaps(query: String) {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode("$query terdekat"))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        runCatching { context.startActivity(intent) }.onFailure {
            Toast.makeText(context, "Aplikasi peta tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
    val last = ui.events.firstOrNull()
    val shops = shopQueries(vehicle.brand, ui.vehicleKind)
    val workshop = if (ui.vehicleKind.isMotor) "bengkel motor" else "bengkel mobil"

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(
                Modifier
                    .height(60.dp)
                    .clickable(onClick = onOpenGarage),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (ui.vehicleKind.isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = C.Text,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(vehicle.name, color = C.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = C.Text, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(C.s2)
                    .clickable { openMaps(workshop) },
            ) {
                MapSketch(Modifier.fillMaxSize())
                Row(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(C.s3)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null, tint = C.Text, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ketuk untuk buka peta", color = C.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(C.t2),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            SectionHeader("Servis Terakhir", onSeeAll = { openMaps(workshop) })
            if (last == null) {
                Text(
                    "Belum ada servis di bengkel yang tercatat.",
                    color = C.Muted,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(C.Surface)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(last.items.joinToString(", ") { it.componentName }, color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${Fmt.date(last.event.servicedOn)} · ${Fmt.number(last.event.odometerKm)} km", color = C.Muted, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            SectionHeader("Bengkel Terdekat", onSeeAll = { openMaps(workshop) })
            Spacer(Modifier.height(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(C.Surface)
                    .border(1.dp, C.LineStrong, RoundedCornerShape(22.dp)),
            ) {
                shops.forEachIndexed { index, shop ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { openMaps(shop.query) }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(C.RedSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(shop.icon, contentDescription = null, tint = C.Red, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(shop.title, color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                Spacer(Modifier.width(5.dp))
                                Icon(Icons.Rounded.Verified, contentDescription = null, tint = C.Blue, modifier = Modifier.size(15.dp))
                            }
                            Text(shop.subtitle, color = C.Muted, fontSize = 13.sp, maxLines = 1)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(C.Bg)
                                .border(1.dp, C.Line, RoundedCornerShape(9.dp))
                                .padding(horizontal = 11.dp, vertical = 5.dp),
                        ) {
                            Text("Rute", color = C.Blue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (index < shops.lastIndex) {
                        Box(
                            Modifier
                                .padding(start = 70.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(C.LineStrong),
                        )
                    }
                }
            }
            Spacer(Modifier.height(130.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            "Lihat Semua",
            color = C.Blue,
            fontSize = 15.sp,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onSeeAll).padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}

/** Simplified dark map of Java and its neighbours with a few pins. Decorative only. */
@Composable
private fun MapSketch(modifier: Modifier) {
    val land = C.s2
    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(93, 115, 133)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // view window: lon 103.5..117.5 E, lat 1.5..9.5 S
        fun pt(lon: Float, lat: Float) = Offset((lon - 103.5f) / 14f * w, (lat - 1.5f) / 8f * h)
        fun shape(vararg c: Float): Path = Path().apply {
            for (i in c.indices step 2) {
                val o = pt(c[i], c[i + 1])
                if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
            }
            close()
        }
        // Sumatra (south tip), Bangka, Kalimantan (south coast), Java, Madura, Bali
        drawPath(shape(103.5f, 1.5f, 104.6f, 1.6f, 105.8f, 2.4f, 106.1f, 3.3f, 105.9f, 4.3f, 105.85f, 5.8f, 105.2f, 5.8f, 104.6f, 5.5f, 103.5f, 4.9f), land)
        drawPath(shape(105.2f, 1.6f, 106.0f, 1.9f, 106.8f, 2.9f, 106.3f, 3.1f, 105.6f, 2.4f), land)
        drawPath(shape(109.0f, 1.5f, 110.1f, 2.9f, 111.7f, 3.0f, 113.0f, 3.3f, 114.6f, 3.75f, 116.0f, 3.9f, 116.4f, 2.6f, 117.5f, 1.8f, 117.5f, 1.5f), land)
        drawPath(
            shape(
                105.2f, 6.8f, 105.6f, 6.0f, 106.1f, 5.95f, 106.8f, 6.1f, 107.3f, 5.95f, 108.2f, 6.3f, 108.6f, 6.7f,
                109.1f, 6.85f, 109.7f, 6.85f, 110.4f, 6.95f, 110.9f, 6.4f, 111.4f, 6.7f, 112.1f, 6.9f, 112.6f, 6.9f,
                112.75f, 7.25f, 113.0f, 7.6f, 113.8f, 7.7f, 114.4f, 7.75f, 114.4f, 8.6f, 113.5f, 8.4f, 112.5f, 8.35f,
                111.5f, 8.25f, 110.4f, 8.05f, 109.5f, 7.75f, 108.5f, 7.8f, 107.5f, 7.55f, 106.5f, 7.35f, 105.8f, 6.85f,
            ),
            land,
        )
        drawPath(shape(112.7f, 6.95f, 113.3f, 6.88f, 114.1f, 6.98f, 114.0f, 7.15f, 113.0f, 7.2f, 112.75f, 7.12f), land)
        drawPath(shape(114.45f, 8.15f, 115.1f, 8.05f, 115.6f, 8.4f, 115.2f, 8.8f, 114.6f, 8.5f), land)

        labelPaint.textSize = 11.sp.toPx()
        listOf(
            "Palembang" to pt(104.3f, 2.85f), "Banjarmasin" to pt(114.2f, 3.15f), "Jakarta" to pt(106.2f, 6.45f),
            "Semarang" to pt(109.9f, 7.25f), "Surabaya" to pt(112.0f, 7.6f),
        ).forEach { (name, o) -> drawContext.canvas.nativeCanvas.drawText(name, o.x, o.y, labelPaint) }

        listOf(pt(106.85f, 6.2f), pt(107.35f, 6.75f), pt(107.62f, 6.92f), pt(107.75f, 7.0f)).forEach {
            drawCircle(Color.White, radius = 7.dp.toPx(), center = it)
            drawCircle(C.Blue, radius = 5.5.dp.toPx(), center = it)
        }
        val sby = pt(112.75f, 7.28f)
        drawCircle(Color.White, radius = 5.5.dp.toPx(), center = sby)
        drawCircle(C.t2, radius = 4.dp.toPx(), center = sby)
    }
}

/* ---------------- Cari ---------------- */

@Composable
fun SearchScreen(
    ui: UiState,
    onBack: () -> Unit,
    onOpenComponent: (Long) -> Unit,
    onSetupTemplate: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim().lowercase()
    val active = ui.components.filter { q.isEmpty() || it.component.name.lowercase().contains(q) }
    val tracked = ui.components.map { it.component.templateKey }.toSet()
    val templates = Seed.templates.filter {
        it.key !in tracked && Seed.fits(it, ui.vehicleKind) && (q.isEmpty() || it.name.lowercase().contains(q))
    }

    Column(Modifier.fillMaxSize().background(C.Bg).statusBarsPadding()) {
        Row(
            Modifier.padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = C.Text) }
            Spacer(Modifier.width(4.dp))
            PillField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Cari komponen, dokumen…",
                trailing = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = C.Muted, modifier = Modifier.padding(end = 14.dp))
                },
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (active.isNotEmpty()) {
                item { SearchLabel("Dipantau") }
                items(active, key = { "c${it.component.id}" }) { item ->
                    SearchRow(
                        icon = item.component.icon,
                        iconColor = statusColor(item.due.status),
                        title = item.component.name,
                        subtitle = Fmt.tileHeadline(item.due),
                    ) { onOpenComponent(item.component.id) }
                }
            }
            if (templates.isNotEmpty()) {
                item { SearchLabel("Tambah baru") }
                items(templates, key = { "t${it.key}" }) { template ->
                    SearchRow(
                        icon = template.icon,
                        iconColor = C.Muted,
                        title = template.name,
                        subtitle = Fmt.interval(template.km, template.months),
                    ) { onSetupTemplate(template.key) }
                }
            }
            if (active.isEmpty() && templates.isEmpty()) {
                item {
                    Spacer(Modifier.height(60.dp))
                    EmptyState(Icons.Rounded.Search, "Tidak ditemukan", "Coba kata lain, misalnya “oli” atau “rem”.")
                }
            }
        }
    }
}

@Composable
private fun SearchLabel(text: String) {
    Text(text, color = C.Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp, top = 8.dp))
}

@Composable
private fun SearchRow(icon: String, iconColor: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(C.Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconWell(icon, iconColor, size = 44, iconSize = 24)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = C.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = C.Muted, fontSize = 14.sp)
        }
    }
}
