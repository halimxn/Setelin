package id.cekendaraan.app.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "Honda",
    val model: String = "Vario Evo 160",
    val year: Int = 2026,
    val plate: String = "",
    val odometerKm: Int,
    val monthlyKm: Int = 1000,
    /** [VehicleKind] name. */
    @ColumnInfo(defaultValue = "MATIC") val kind: String = "MATIC",
)

@Entity(
    tableName = "components",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("vehicleId")],
)
data class Component(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val templateKey: String,
    val name: String,
    val icon: String,
    val category: String,
    val intervalKm: Int? = null,
    val intervalMonths: Int? = null,
    val lastServiceOn: Long? = null,
    val lastServiceOdo: Int? = null,
    val important: Boolean = false,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "service_events",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("vehicleId"), Index("servicedOn")],
)
data class ServiceEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val servicedOn: Long,
    val odometerKm: Int,
    val cost: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "service_items",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEvent::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("eventId"), Index("componentId")],
)
data class ServiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val componentId: Long,
    val componentName: String,
    val icon: String,
)

data class ServiceEventWithItems(
    @Embedded val event: ServiceEvent,
    @Relation(parentColumn = "id", entityColumn = "eventId")
    val items: List<ServiceItem>,
)

data class LastService(
    val servicedOn: Long,
    val odometerKm: Int,
)

@Dao
interface GarageDao {
    @Query("SELECT * FROM vehicles ORDER BY id LIMIT 1")
    fun vehicle(): Flow<Vehicle?>

    @Query("SELECT * FROM vehicles ORDER BY id LIMIT 1")
    suspend fun vehicleOnce(): Vehicle?

    @Query("SELECT * FROM components ORDER BY sortOrder, id")
    fun components(): Flow<List<Component>>

    @Query("SELECT * FROM components ORDER BY sortOrder, id")
    suspend fun componentsOnce(): List<Component>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun componentOnce(id: Long): Component?

    @Transaction
    @Query("SELECT * FROM service_events ORDER BY servicedOn DESC, id DESC")
    fun events(): Flow<List<ServiceEventWithItems>>

    @Query("SELECT * FROM service_items WHERE eventId = :eventId")
    suspend fun itemsForEvent(eventId: Long): List<ServiceItem>

    @Query(
        """
        SELECT e.servicedOn AS servicedOn, e.odometerKm AS odometerKm
        FROM service_events e
        INNER JOIN service_items i ON i.eventId = e.id
        WHERE i.componentId = :componentId
        ORDER BY e.servicedOn DESC, e.id DESC
        LIMIT 1
        """,
    )
    suspend fun lastServiceFor(componentId: Long): LastService?

    @Insert
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Insert
    suspend fun insertComponents(components: List<Component>)

    @Insert
    suspend fun insertComponent(component: Component): Long

    @Insert
    suspend fun insertEvent(event: ServiceEvent): Long

    @Insert
    suspend fun insertItems(items: List<ServiceItem>)

    @Update
    suspend fun updateComponent(component: Component)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET odometerKm = :odometerKm WHERE id = :id")
    suspend fun updateOdometer(id: Long, odometerKm: Int)

    @Query("DELETE FROM components WHERE id = :id")
    suspend fun deleteComponent(id: Long)

    @Query("DELETE FROM service_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM service_events")
    suspend fun clearEvents()

    @Query("DELETE FROM components")
    suspend fun clearComponents()

    @Query("DELETE FROM vehicles")
    suspend fun clearVehicles()
}

/** v2 adds the vehicle kind; existing vehicles were all matic scooters. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicles ADD COLUMN kind TEXT NOT NULL DEFAULT 'MATIC'")
    }
}

@Database(
    entities = [Vehicle::class, Component::class, ServiceEvent::class, ServiceItem::class],
    version = 2,
    exportSchema = false,
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): GarageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "cekendaraan.db")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

class GarageRepository(private val db: AppDatabase) {
    private val dao = db.dao()

    val vehicle: Flow<Vehicle?> = dao.vehicle()
    val components: Flow<List<Component>> = dao.components()
    val events: Flow<List<ServiceEventWithItems>> = dao.events()

    suspend fun createVehicle(
        name: String,
        brand: String,
        model: String,
        kind: VehicleKind,
        odometerKm: Int,
        monthlyKm: Int,
    ): Long =
        dao.insertVehicle(
            Vehicle(
                name = name,
                brand = brand,
                model = model,
                kind = kind.name,
                odometerKm = odometerKm,
                monthlyKm = monthlyKm,
            ),
        )

    suspend fun updateVehicle(vehicle: Vehicle) = dao.updateVehicle(vehicle)

    suspend fun updateOdometer(id: Long, odometerKm: Int) = dao.updateOdometer(id, odometerKm)

    /**
     * Starts tracking a template. The countdown begins now: today and the current odometer
     * become the baseline, so a freshly added component reads 100%.
     */
    suspend fun addTemplate(
        vehicleId: Long,
        template: ComponentTemplate,
        startDay: Long,
        startOdo: Int,
    ): Long {
        val nextOrder = (dao.componentsOnce().maxOfOrNull { it.sortOrder } ?: 0) + 1
        return dao.insertComponent(
            Component(
                vehicleId = vehicleId,
                templateKey = template.key,
                name = template.name,
                icon = template.icon,
                category = template.category,
                intervalKm = template.km,
                intervalMonths = template.months,
                lastServiceOn = startDay,
                lastServiceOdo = startOdo,
                important = template.important,
                sortOrder = nextOrder,
            ),
        )
    }

    suspend fun updateComponent(component: Component) = dao.updateComponent(component)

    suspend fun deleteComponent(id: Long) = dao.deleteComponent(id)

    suspend fun logService(
        componentIds: List<Long>,
        date: java.time.LocalDate,
        odometerKm: Int,
        cost: Long?,
        note: String,
    ) = db.withTransaction {
        val selected = dao.componentsOnce().filter { it.id in componentIds }
        if (selected.isEmpty()) return@withTransaction

        val eventId = dao.insertEvent(
            ServiceEvent(
                vehicleId = selected.first().vehicleId,
                servicedOn = date.toEpochDay(),
                odometerKm = odometerKm,
                cost = cost?.takeIf { it > 0 },
                note = note.trim(),
            ),
        )
        dao.insertItems(
            selected.map {
                ServiceItem(
                    eventId = eventId,
                    componentId = it.id,
                    componentName = it.name,
                    icon = it.icon,
                )
            },
        )

        val day = date.toEpochDay()
        for (component in selected) {
            val currentDay = component.lastServiceOn
            val isNewer = currentDay == null ||
                day > currentDay ||
                (day == currentDay && odometerKm >= (component.lastServiceOdo ?: Int.MIN_VALUE))
            if (isNewer) {
                dao.updateComponent(
                    component.copy(lastServiceOn = day, lastServiceOdo = odometerKm),
                )
            }
        }

        val vehicle = dao.vehicleOnce()
        if (vehicle != null && odometerKm > vehicle.odometerKm) {
            dao.updateOdometer(vehicle.id, odometerKm)
        }
    }

    suspend fun deleteEvent(eventId: Long) = db.withTransaction {
        val touched = dao.itemsForEvent(eventId).map { it.componentId }.distinct()
        dao.deleteEvent(eventId)
        for (componentId in touched) {
            val component = dao.componentOnce(componentId) ?: continue
            val latest = dao.lastServiceFor(componentId)
            dao.updateComponent(
                component.copy(
                    lastServiceOn = latest?.servicedOn,
                    lastServiceOdo = latest?.odometerKm,
                ),
            )
        }
    }

    suspend fun reset() = db.withTransaction {
        dao.clearEvents()
        dao.clearComponents()
        dao.clearVehicles()
    }
}
