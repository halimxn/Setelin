package id.cekendaraan.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.ui.theme.C
import java.time.LocalDate

@Composable
fun CekKendaraanApp(vm: GarageViewModel = viewModel()) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var homeSection by rememberSaveable { mutableIntStateOf(0) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var setupKey by rememberSaveable { mutableStateOf<String?>(null) }
    var customKind by rememberSaveable { mutableStateOf<Kind?>(null) }
    var garageOpen by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var logOpen by rememberSaveable { mutableStateOf(false) }
    var logPreselect by rememberSaveable { mutableStateOf<Long?>(null) }
    var odometerOpen by rememberSaveable { mutableStateOf(false) }
    var serviceIds by remember { mutableStateOf<List<Long>?>(null) }

    fun openLog(componentId: Long?) {
        logPreselect = componentId
        logOpen = true
    }

    val vehicle = state.vehicle
    val detail = detailId?.let { state.component(it) }
    val setupTemplate = setupKey?.let { Seed.template(it) }

    Box(Modifier.fillMaxSize().background(C.Bg)) {
        when {
            !state.loaded -> LoadingScreen()

            vehicle == null -> OnboardingScreen { name, brand, model, kind, odometer ->
                vm.setup(name, brand, model, kind, odometer)
            }

            setupTemplate != null || customKind != null -> {
                val close = {
                    setupKey = null
                    customKind = null
                }
                BackHandler(onBack = close)
                ComponentSetupScreen(
                    template = setupTemplate,
                    customKind = customKind ?: Kind.COMPONENT,
                    onBack = close,
                    onSave = { template, startDay ->
                        vm.addTemplate(template, startDay)
                        close()
                    },
                )
            }

            detail != null -> {
                BackHandler { detailId = null }
                ComponentDetailScreen(
                    item = detail,
                    ui = state,
                    onBack = { detailId = null },
                    onSave = { vm.saveComponent(it) },
                    onDelete = {
                        vm.deleteComponent(detail.component.id)
                        detailId = null
                        Toast.makeText(context, "Komponen dihapus", Toast.LENGTH_SHORT).show()
                    },
                    onService = { serviceIds = listOf(detail.component.id) },
                    onAddPastService = { openLog(detail.component.id) },
                )
            }

            garageOpen -> {
                BackHandler { garageOpen = false }
                GarageScreen(
                    ui = state,
                    onBack = { garageOpen = false },
                    onUpdateOdometer = { odometerOpen = true },
                    onSaveVehicle = { vm.saveVehicle(it) },
                    onResetAll = {
                        vm.resetAll()
                        garageOpen = false
                        tab = 0
                        Toast.makeText(context, "Semua data dihapus", Toast.LENGTH_SHORT).show()
                    },
                )
            }

            searchOpen -> {
                BackHandler { searchOpen = false }
                SearchScreen(
                    ui = state,
                    onBack = { searchOpen = false },
                    onOpenComponent = {
                        searchOpen = false
                        detailId = it
                    },
                    onSetupTemplate = {
                        searchOpen = false
                        setupKey = it
                    },
                )
            }

            else -> {
                if (tab != 0) BackHandler { tab = 0 }
                Box(Modifier.fillMaxSize()) {
                    when (tab) {
                        0 -> HomeScreen(
                            ui = state,
                            section = homeSection,
                            onSection = { homeSection = it },
                            onOpenComponent = { detailId = it },
                            onSetupTemplate = { setupKey = it },
                            onCustom = { customKind = it },
                            onServiceMany = { serviceIds = it },
                            onUpdateOdometer = { odometerOpen = true },
                            onOpenGarage = { garageOpen = true },
                        )

                        1 -> TripsScreen(ui = state)

                        2 -> HistoryScreen(
                            ui = state,
                            onLogService = { openLog(null) },
                            onDeleteEvent = {
                                vm.deleteEvent(it)
                                Toast.makeText(context, "Catatan servis dihapus", Toast.LENGTH_SHORT).show()
                            },
                        )

                        else -> ShopsScreen(ui = state, onOpenGarage = { garageOpen = true })
                    }
                    FloatingNav(
                        car = !state.vehicleKind.isMotor,
                        selected = tab,
                        onSelect = { tab = it },
                        onSearch = { searchOpen = true },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }

    serviceIds?.let { ids ->
        val names = ids.mapNotNull { state.component(it)?.component?.name }
        SlideToServiceDialog(
            names = names,
            onDismiss = { serviceIds = null },
            onConfirm = {
                val odo = vehicle?.odometerKm ?: 0
                vm.logService(ids, LocalDate.now(), odo, null, "")
                serviceIds = null
                Toast.makeText(context, "Servis tercatat", Toast.LENGTH_SHORT).show()
            },
        )
    }

    if (logOpen && vehicle != null) {
        LogServiceSheet(
            ui = state,
            preselectId = logPreselect,
            onDismiss = {
                logOpen = false
                logPreselect = null
            },
            onSave = { ids, date, odo, cost, note ->
                vm.logService(ids, date, odo, cost, note)
                logOpen = false
                logPreselect = null
                Toast.makeText(context, "Servis tersimpan", Toast.LENGTH_SHORT).show()
            },
        )
    }

    if (odometerOpen && vehicle != null) {
        OdometerDialog(
            vehicle = vehicle,
            onDismiss = { odometerOpen = false },
            onSave = { value ->
                vm.updateOdometer(vehicle, value)
                odometerOpen = false
                Toast.makeText(context, "Odometer diperbarui", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

private data class NavEntry(val label: String, val icon: ImageVector, val activeIcon: ImageVector)

private fun navEntries(car: Boolean) = listOf(
    if (car) {
        NavEntry("Mobil", Icons.Rounded.DirectionsCar, Icons.Rounded.DirectionsCar)
    } else {
        NavEntry("Motor", Icons.Rounded.TwoWheeler, Icons.Rounded.TwoWheeler)
    },
    NavEntry("Trip", Icons.Outlined.Map, Icons.Rounded.Map),
    NavEntry("Riwayat", Icons.Outlined.Schedule, Icons.Rounded.Schedule),
    NavEntry("Bengkel", Icons.Outlined.Build, Icons.Rounded.Build),
)

/** Floating pill tab bar with a separate round search button. */
@Composable
private fun FloatingNav(
    car: Boolean,
    selected: Int,
    onSelect: (Int) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(58.dp)
                .clip(CircleShape)
                .background(C.s1.copy(alpha = 0.9f))
                .border(1.dp, C.Line, CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navEntries(car).forEachIndexed { index, entry ->
                val active = index == selected
                val bg by animateColorAsState(if (active) C.BlueSoft else Color.Transparent, label = "nav")
                val tint = if (active) C.Blue else C.Muted
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(bg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        if (active) entry.activeIcon else entry.icon,
                        contentDescription = entry.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        entry.label,
                        color = tint,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(C.s1.copy(alpha = 0.9f))
                .border(1.dp, C.Line, CircleShape)
                .clickable(onClick = onSearch),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Search, contentDescription = "Cari", tint = C.Muted, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(C.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark()
            Spacer(Modifier.height(18.dp))
            CircularProgressIndicator(color = C.Blue, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}
