package id.cekendaraan.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricMoped
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.cekendaraan.app.R
import androidx.compose.runtime.remember
import androidx.compose.material.icons.rounded.EvStation
import id.cekendaraan.app.data.VehicleKind
import id.cekendaraan.app.data.Catalog
import id.cekendaraan.app.ui.theme.C

private val nameIdeas = listOf("Si Biru", "Kuda Besi", "Sahabat Harian", "Si Gesit", "Tunggangan Setia", "Si Kencang")

/** Answers on the type step -> vehicle kind, or null while unanswered. */
private fun kindOf(type: Int?, fuel: Int?, gear: Int?): VehicleKind? = when (type) {
    0 -> if (fuel != null && gear != null) VehicleKind.CAR else null
    1 -> when {
        fuel == 1 -> VehicleKind.ELECTRIC
        fuel == 0 && gear == 0 -> VehicleKind.MANUAL
        fuel == 0 && gear == 1 -> VehicleKind.MATIC
        else -> null
    }
    else -> null
}

@Composable
fun OnboardingScreen(onStart: (name: String, brand: String, model: String, kind: VehicleKind, odometerKm: Int) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var type by rememberSaveable { mutableStateOf<Int?>(null) }
    var fuel by rememberSaveable { mutableStateOf<Int?>(null) }
    var gear by rememberSaveable { mutableStateOf<Int?>(null) }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var odoText by rememberSaveable { mutableStateOf("") }
    val kind = kindOf(type, fuel, gear)

    if (step > 0) BackHandler { step-- }

    Box(Modifier.fillMaxSize().background(C.Bg)) {
        Crossfade(targetState = step, label = "onboarding") { current ->
            when (current) {
                0 -> WelcomeStep { step = 1 }
                1 -> TypeStep(
                    type = type,
                    fuel = fuel,
                    gear = gear,
                    kind = kind,
                    onType = {
                        if (it != type) {
                            fuel = null
                            gear = null
                            brand = ""
                            model = ""
                        }
                        type = it
                    },
                    onFuel = {
                        fuel = it
                        gear = null
                    },
                    onGear = { gear = it },
                    onBack = { step = 0 },
                    onNext = { step = 2 },
                )
                2 -> BrandModelStep(
                    kind = kind ?: VehicleKind.MATIC,
                    brand = brand,
                    model = model,
                    onBrand = {
                        if (it != brand) model = ""
                        brand = it
                    },
                    onModel = {
                        model = it
                        name = it
                    },
                    onBack = { step = 1 },
                    onNext = { step = 3 },
                )
                3 -> NameStep(
                    name = name,
                    brand = brand,
                    model = model,
                    kind = kind ?: VehicleKind.MATIC,
                    onName = { name = it },
                    onBack = { step = 2 },
                    onNext = { step = 4 },
                )
                else -> OdometerStep(
                    value = odoText,
                    onValue = { odoText = it.filter(Char::isDigit).take(7) },
                    onBack = { step = 3 },
                    onDone = { onStart(name, brand, model, kind ?: VehicleKind.MATIC, odoText.toIntOrNull() ?: 0) },
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(100.dp).clip(CircleShape).background(C.BlueDeep),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.TwoWheeler, contentDescription = null, tint = C.Blue, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(30.dp))
        Text(
            "Selamat Datang di Cek Kendaraan",
            color = C.Text,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 29.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Pantau kesehatan, jarak tempuh, dan riwayat servis kendaraanmu di satu tempat. Tambahkan kendaraan pertamamu untuk mulai.",
            color = C.Muted,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Tambah Kendaraan Pertama", onClick = onNext)
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = C.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = C.Muted, fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
    }
}

private fun heroCopy(kind: VehicleKind) = when (kind) {
    VehicleKind.MATIC -> "Skuter Matic" to "Teman setia harian"
    VehicleKind.MANUAL, VehicleKind.SPORT -> "Motor Manual" to "Bebek & sport, siap diajak jauh"
    VehicleKind.ELECTRIC -> "Motor Listrik" to "Senyap dan hemat"
    VehicleKind.CAR -> "Mobil" to "Untuk perjalanan bersama"
}

@Composable
private fun TypeStep(
    type: Int?,
    fuel: Int?,
    gear: Int?,
    kind: VehicleKind?,
    onType: (Int) -> Unit,
    onFuel: (Int) -> Unit,
    onGear: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val ready = kind != null
    val car = type == 0
    val needed = if (type == 1 && fuel == 1) 2 else 3
    val picked = listOfNotNull(type, fuel, gear).size

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        BackCircle(onBack)
        if (!ready) {
            Spacer(Modifier.height(24.dp))
            StepTitle("Atur Kendaraanmu", "Ceritakan kendaraanmu supaya jadwal perawatannya pas.")
        }
        Crossfade(
            targetState = kind,
            animationSpec = androidx.compose.animation.core.tween(700),
            label = "reveal",
            modifier = Modifier.fillMaxWidth().animateContentSize(),
        ) { revealed ->
            Box(Modifier.fillMaxWidth().height(if (revealed != null) 300.dp else 250.dp), contentAlignment = Alignment.Center) {
                if (revealed != null) {
                    val (title, sub) = heroCopy(revealed)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VehicleHero(glow = true, height = 210, kind = revealed)
                        Text(title, color = C.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(sub, color = C.Muted, fontSize = 16.sp)
                    }
                } else {
                    ParticleRing(Modifier.fillMaxWidth().height(220.dp), progress = picked / needed.toFloat())
                }
            }
        }
        if (!ready) {
            Text(
                if (picked == 0) "Sesuatu mulai terbentuk…" else "Lanjutkan, sedikit lagi…",
                color = C.Muted,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.height(28.dp))

        Question("Jenis kendaraan?")
        ChoiceRow(
            left = Choice("Mobil", Icons.Rounded.DirectionsCar),
            right = Choice("Motor", Icons.Rounded.TwoWheeler),
            selected = type,
            onSelect = onType,
        )
        AnimatedVisibility(type != null) {
            Column {
                Spacer(Modifier.height(26.dp))
                Question(if (car) "Jenis mobil?" else "Jenis motor?")
                ChoiceRow(
                    left = Choice("Bensin", Icons.Rounded.LocalGasStation),
                    right = Choice("Listrik", if (car) Icons.Rounded.EvStation else Icons.Rounded.ElectricMoped),
                    selected = fuel,
                    onSelect = onFuel,
                )
            }
        }
        AnimatedVisibility((type == 1 && fuel == 0) || (car && fuel != null)) {
            Column {
                Spacer(Modifier.height(26.dp))
                Question("Tipe transmisi")
                ChoiceRow(
                    left = Choice("Manual", Icons.Rounded.ViewModule),
                    right = Choice(if (car) "Otomatis" else "Matic", Icons.Rounded.Speed),
                    selected = gear,
                    onSelect = onGear,
                )
            }
        }
        AnimatedVisibility(ready) {
            Column {
                Spacer(Modifier.height(28.dp))
                PrimaryButton(text = "Lanjut", onClick = onNext)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun BrandModelStep(
    kind: VehicleKind,
    brand: String,
    model: String,
    onBrand: (String) -> Unit,
    onModel: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val brands = remember(kind) { Catalog.brandsFor(kind) }
    val current = brands.firstOrNull { it.name == brand }
    var custom by rememberSaveable(brand) { mutableStateOf(current?.models?.none { it.name == model } == true && model.isNotEmpty()) }
    var otherBrand by rememberSaveable { mutableStateOf(brand.isNotEmpty() && current == null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            BackCircle(onBack)
            Spacer(Modifier.height(24.dp))
            StepTitle("Merek & Model", "Pilih pabrikan dan produk kendaraanmu.")
            Spacer(Modifier.height(24.dp))
            Question("Merek")
            val chips = brands.map { it.name } + "Lainnya"
            chips.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                    row.forEach { label ->
                        val on = if (label == "Lainnya") otherBrand else (!otherBrand && label == brand)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (on) C.BlueDeep else C.Surface)
                                .border(if (on) 2.dp else 1.dp, if (on) C.Blue else C.LineStrong, RoundedCornerShape(16.dp))
                                .clickable {
                                    if (label == "Lainnya") {
                                        otherBrand = true
                                        onBrand("")
                                    } else {
                                        otherBrand = false
                                        onBrand(label)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, color = if (on) C.Blue else C.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            if (otherBrand) {
                Spacer(Modifier.height(14.dp))
                Question("Nama merek")
                PillField(value = brand, onValueChange = { onBrand(it.take(24)) }, placeholder = "Contoh: Benelli")
                Spacer(Modifier.height(18.dp))
                Question("Model")
                PillField(value = model, onValueChange = { onModel(it.take(30)) }, placeholder = "Contoh: Panarea")
            } else if (current != null) {
                Spacer(Modifier.height(14.dp))
                Question("Model ${current.name}")
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(C.Surface),
                ) {
                    current.models.forEachIndexed { i, m ->
                        val on = !custom && m.name == model
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    custom = false
                                    onModel(m.name)
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(m.name, color = if (on) C.Blue else C.Text, fontSize = 16.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.weight(1f))
                            if (m.kind == VehicleKind.SPORT) {
                                Text("Sport", color = C.Muted, fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
                            }
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (on) C.Blue else C.Surface3, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(if (on) C.Blue else Color.Transparent),
                            )
                        }
                        if (i < current.models.lastIndex) {
                            Box(Modifier.padding(start = 18.dp).fillMaxWidth().height(1.dp).background(C.Line))
                        }
                    }
                    Box(Modifier.padding(start = 18.dp).fillMaxWidth().height(1.dp).background(C.Line))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                custom = true
                                onModel("")
                            }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Model lain…", color = if (custom) C.Blue else C.Muted, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    }
                }
                if (custom) {
                    Spacer(Modifier.height(12.dp))
                    PillField(value = model, onValueChange = { onModel(it.take(30)) }, placeholder = "Ketik nama model")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(text = "Lanjut", enabled = brand.isNotBlank() && model.isNotBlank(), onClick = onNext)
        Spacer(Modifier.height(16.dp))
    }
}

private data class Choice(val label: String, val icon: ImageVector)

@Composable
private fun Question(text: String) {
    Text(text, color = C.Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ChoiceRow(left: Choice, right: Choice, selected: Int?, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(left, right).forEachIndexed { index, choice ->
            val on = selected == index
            Column(
                Modifier
                    .weight(1f)
                    .height(86.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (on) C.BlueDeep else C.Surface)
                    .border(if (on) 2.dp else 1.dp, if (on) C.Blue else C.LineStrong, RoundedCornerShape(22.dp))
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(choice.icon, contentDescription = null, tint = if (on) C.Blue else C.Muted, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(8.dp))
                Text(choice.label, color = if (on) C.Blue else C.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NameStep(
    name: String,
    brand: String,
    model: String,
    kind: VehicleKind,
    onName: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            BackCircle(onBack)
            Spacer(Modifier.height(24.dp))
            StepTitle("Hampir Selesai", "Beri nama kendaraanmu.")
            Spacer(Modifier.height(12.dp))
            VehicleHero(glow = true, height = 230, brand = brand, model = model, kind = kind)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(C.Surface)
                    .border(1.dp, C.LineStrong, RoundedCornerShape(26.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(C.BlueSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (kind.isMotor) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = C.Blue,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(kind.label, color = C.Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("$brand $model", color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Nama Kendaraan", color = C.Muted, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            PillField(
                value = name,
                onValueChange = { onName(it.take(30)) },
                placeholder = model.ifBlank { "Contoh: Si Biru" },
                trailing = {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(C.BlueSoft)
                            .clickable { onName(nameIdeas.filter { it != name }.random()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Casino, contentDescription = "Acak nama", tint = C.Blue, modifier = Modifier.size(22.dp))
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            Text("Nama ini yang akan tampil di beranda.", color = C.Muted, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton(text = "Lanjut", enabled = name.isNotBlank(), onClick = onNext)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OdometerStep(value: String, onValue: (String) -> Unit, onBack: () -> Unit, onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(330.dp)) {
                OdometerArt(Modifier.fillMaxSize())
                BackCircle(onBack, Modifier.statusBarsPadding().padding(start = 24.dp, top = 16.dp))
            }
            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(28.dp))
                StepTitle("Odometer Saat Ini", "Masukkan angka odometer sekarang. Bisa dilewati dan diatur nanti.")
                Spacer(Modifier.height(28.dp))
                Text("Odometer Saat Ini", color = C.Muted, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                PillField(
                    value = value,
                    onValueChange = onValue,
                    placeholder = "contoh: 15000 (opsional)",
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(10.dp))
                Text("Kosongkan untuk mengatur nanti.", color = C.Muted, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
                Spacer(Modifier.height(20.dp))
            }
        }
        Box(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            PrimaryButton(text = "Tambah Kendaraan", onClick = onDone)
        }
    }
}

/** Line-art dashboard illustration for the odometer step, fading into the page at the bottom. */
@Composable
private fun OdometerArt(modifier: Modifier) {
    Box(modifier.background(C.bg)) {
        Image(
            painter = painterResource(R.drawable.odometer_art),
            contentDescription = "Ilustrasi odometer",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to C.Bg)),
        )
    }
}
