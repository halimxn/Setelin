package id.cekendaraan.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.cekendaraan.app.data.AppDatabase
import id.cekendaraan.app.data.Component
import id.cekendaraan.app.data.ComponentTemplate
import id.cekendaraan.app.data.GarageRepository
import id.cekendaraan.app.data.Kind
import id.cekendaraan.app.data.Seed
import id.cekendaraan.app.data.VehicleKind
import id.cekendaraan.app.data.ServiceEventWithItems
import id.cekendaraan.app.data.Vehicle
import id.cekendaraan.app.domain.DueInfo
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.domain.dueInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ComponentUi(
    val component: Component,
    val due: DueInfo,
)

data class UiState(
    val loaded: Boolean = false,
    val vehicle: Vehicle? = null,
    val components: List<ComponentUi> = emptyList(),
    val events: List<ServiceEventWithItems> = emptyList(),
) {
    val dueCount: Int get() = components.count { it.due.status == DueStatus.DUE }
    val soonCount: Int get() = components.count { it.due.status == DueStatus.SOON }
    val okCount: Int get() = components.count { it.due.status == DueStatus.OK }
    val unknownCount: Int get() = components.count { it.due.status == DueStatus.UNKNOWN }
    val attentionCount: Int get() = dueCount + soonCount
    val totalCost: Long get() = events.sumOf { it.event.cost ?: 0L }

    val monthCost: Long
        get() {
            val month = YearMonth.now()
            return events.sumOf { event ->
                val day = LocalDate.ofEpochDay(event.event.servicedOn)
                if (YearMonth.from(day) == month) event.event.cost ?: 0L else 0L
            }
        }

    val vehicleKind: VehicleKind get() = VehicleKind.of(vehicle?.kind)

    fun component(id: Long): ComponentUi? = components.firstOrNull { it.component.id == id }

    fun ofKind(kind: Kind): List<ComponentUi> =
        components.filter { Seed.kindOf(it.component.templateKey, it.component.category) == kind }

    fun eventsFor(componentId: Long): List<ServiceEventWithItems> =
        events.filter { e -> e.items.any { it.componentId == componentId } }
}

class GarageViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GarageRepository(AppDatabase.get(application))

    val ui: StateFlow<UiState> = combine(
        repo.vehicle,
        repo.components,
        repo.events,
    ) { vehicle, components, events ->
        val avgDaily = (vehicle?.monthlyKm ?: 1000).coerceAtLeast(100) / 30.0
        val severity = mapOf(
            DueStatus.DUE to 0,
            DueStatus.SOON to 1,
            DueStatus.UNKNOWN to 2,
            DueStatus.OK to 3,
            DueStatus.NONE to 4,
        )
        val items = components
            .map { ComponentUi(it, it.dueInfo(vehicle?.odometerKm ?: 0, avgDaily)) }
            .sortedWith(
                compareBy<ComponentUi> { severity[it.due.status] ?: 9 }
                    .thenBy { it.component.sortOrder },
            )
        UiState(loaded = true, vehicle = vehicle, components = items, events = events)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(),
    )

    fun setup(
        name: String,
        brand: String,
        model: String,
        kind: VehicleKind,
        odometerKm: Int,
        monthlyKm: Int = if (kind == VehicleKind.CAR) 1200 else 1000,
    ) {
        viewModelScope.launch {
            repo.createVehicle(
                name = name.trim().ifBlank { model.ifBlank { "Kendaraanku" } },
                brand = brand.trim(),
                model = model.trim(),
                kind = kind,
                odometerKm = odometerKm.coerceAtLeast(0),
                monthlyKm = monthlyKm.coerceAtLeast(100),
            )
        }
    }

    fun updateOdometer(vehicle: Vehicle, odometerKm: Int) {
        viewModelScope.launch { repo.updateOdometer(vehicle.id, odometerKm.coerceAtLeast(0)) }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repo.updateVehicle(
                vehicle.copy(
                    name = vehicle.name.ifBlank { "Honda Vario Evo 160" },
                    odometerKm = vehicle.odometerKm.coerceAtLeast(0),
                    monthlyKm = vehicle.monthlyKm.coerceAtLeast(100),
                ),
            )
        }
    }

    /** Starts tracking [template]; [startDay] defaults to today (paperwork may pass its own). */
    fun addTemplate(template: ComponentTemplate, startDay: LocalDate = LocalDate.now()) {
        val vehicle = ui.value.vehicle ?: return
        viewModelScope.launch {
            repo.addTemplate(vehicle.id, template, startDay.toEpochDay(), vehicle.odometerKm)
        }
    }

    fun saveComponent(component: Component) {
        viewModelScope.launch { repo.updateComponent(component) }
    }

    fun deleteComponent(id: Long) {
        viewModelScope.launch { repo.deleteComponent(id) }
    }

    fun logService(
        componentIds: List<Long>,
        date: LocalDate,
        odometerKm: Int,
        cost: Long?,
        note: String,
    ) {
        if (componentIds.isEmpty()) return
        viewModelScope.launch {
            repo.logService(componentIds, date, odometerKm, cost, note)
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch { repo.deleteEvent(id) }
    }

    fun resetAll() {
        viewModelScope.launch { repo.reset() }
    }
}
