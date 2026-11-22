package id.cekendaraan.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.cekendaraan.app.R
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.ui.theme.C
import id.cekendaraan.app.ui.theme.Gradients
import id.cekendaraan.app.ui.theme.Type
import java.time.LocalDate

@Composable
fun CekKendaraanApp(vm: GarageViewModel = viewModel()) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation state: 0 = Dasbor, 1 = Jurnal, 3 = Layanan, 4 = Akun (2 is the center Scan trigger)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var homeSection by rememberSaveable { mutableIntStateOf(0) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var setupKey by rememberSaveable { mutableStateOf<String?>(null) }
    var customKind by rememberSaveable { mutableStateOf<Kind?>(null) }
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

    Box(Modifier.fillMaxSize().background(C.bg)) {
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
                // Back handler: navigating back from non-dashboard tabs always returns to Dasbor (Tab 0)
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
                            onOpenGarage = { tab = 4 },
                        )

                        1 -> HistoryScreen(
                            ui = state,
                            onLogService = { openLog(null) },
                            onDeleteEvent = {
                                vm.deleteEvent(it)
                                Toast.makeText(context, "Catatan servis dihapus", Toast.LENGTH_SHORT).show()
                            },
                        )

                        3 -> ShopsScreen(
                            ui = state,
                            onOpenGarage = { tab = 4 },
                        )

                        4 -> GarageScreen(
                            ui = state,
                            title = "Akun",
                            onBack = { tab = 0 },
                            onUpdateOdometer = { odometerOpen = true },
                            onSaveVehicle = { vm.saveVehicle(it) },
                            onResetAll = {
                                vm.resetAll()
                                tab = 0
                                Toast.makeText(context, "Semua data dihapus", Toast.LENGTH_SHORT).show()
                            },
                        )

                        else -> HomeScreen(
                            ui = state,
                            section = homeSection,
                            onSection = { homeSection = it },
                            onOpenComponent = { detailId = it },
                            onSetupTemplate = { setupKey = it },
                            onCustom = { customKind = it },
                            onServiceMany = { serviceIds = it },
                            onUpdateOdometer = { odometerOpen = true },
                            onOpenGarage = { tab = 4 },
                        )
                    }

                    FloatingNav(
                        selected = tab,
                        onSelect = { tab = it },
                        onScan = { odometerOpen = true },
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

/**
 * Modern floating navigation bar strictly adhering to DESIGN.md:
 * - Floating inset 16dp, height 66dp, radius 24dp
 * - Surface s1 background with 88% alpha
 * - 5 slots: Dasbor · Jurnal · [Scan FAB] · Layanan · Akun
 * - Center elevated Scan button (56dp, radius 20dp, accent gradient, elevated 20dp)
 * - Active tab highlighted in luminous cyan acc (#22D3EE)
 */
@Composable
private fun FloatingNav(
    selected: Int,
    onSelect: (Int) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Main floating navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(C.s1.copy(alpha = 0.88f))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1. Dasbor
            NavItem(
                label = "Dasbor",
                icon = if (selected == 0) Icons.Rounded.Home else Icons.Outlined.Home,
                active = selected == 0,
                onClick = { onSelect(0) },
                modifier = Modifier.weight(1f),
            )

            // 2. Jurnal
            NavItem(
                label = "Jurnal",
                icon = if (selected == 1) Icons.Rounded.ReceiptLong else Icons.Outlined.ReceiptLong,
                active = selected == 1,
                onClick = { onSelect(1) },
                modifier = Modifier.weight(1f),
            )

            // Center slot reserved for the floating Scan button
            Spacer(Modifier.width(64.dp))

            // 4. Layanan
            NavItem(
                label = "Layanan",
                icon = if (selected == 3) Icons.Rounded.Handyman else Icons.Outlined.Handyman,
                active = selected == 3,
                onClick = { onSelect(3) },
                modifier = Modifier.weight(1f),
            )

            // 5. Akun
            NavItem(
                label = "Akun",
                icon = if (selected == 4) Icons.Rounded.Person else Icons.Outlined.Person,
                active = selected == 4,
                onClick = { onSelect(4) },
                modifier = Modifier.weight(1f),
            )
        }

        // Center elevated Scan FAB (rises 20dp above the bar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
                .size(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Gradients.Accent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onScan,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_scan_fab),
                contentDescription = "Scan odometer",
                tint = C.ink,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(if (active) C.acc else C.t3, label = "nav_tint")

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = Type.Nav.copy(
                color = tint,
                fontWeight = if (active) FontWeight.W700 else FontWeight.W600,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(C.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark()
            Spacer(Modifier.height(18.dp))
            CircularProgressIndicator(color = C.acc, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}
