package id.cekendaraan.app.data

/** What kind of vehicle it is; drives the hero picture and which templates make sense. */
enum class VehicleKind(val label: String) {
    MATIC("Motor · Matic"),
    MANUAL("Motor · Manual"),
    SPORT("Motor · Sport"),
    ELECTRIC("Motor · Listrik"),
    CAR("Mobil"),
    ;

    val isMotor: Boolean get() = this != CAR

    companion object {
        fun of(value: String?): VehicleKind = entries.firstOrNull { it.name == value } ?: MATIC
    }
}

data class CatalogModel(val brand: String, val name: String, val kind: VehicleKind) {
    /** Resource-safe key, also the hero picture name: vehicle_<key>.webp */
    val key: String get() = slug(brand, name)
}

data class CatalogBrand(val name: String, val models: List<CatalogModel>)

fun slug(brand: String, model: String): String =
    "${brand}_$model".lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

/**
 * Popular brands and models on Indonesian roads (2024–2026). Not exhaustive: the rider can
 * always type another model.
 */
object Catalog {
    private fun brand(name: String, vararg models: Pair<String, VehicleKind>) =
        CatalogBrand(name, models.map { CatalogModel(name, it.first, it.second) })

    private val M = VehicleKind.MATIC
    private val B = VehicleKind.MANUAL
    private val S = VehicleKind.SPORT
    private val E = VehicleKind.ELECTRIC
    private val C = VehicleKind.CAR

    val motor: List<CatalogBrand> = listOf(
        brand(
            "Honda",
            "BeAT" to M, "Scoopy" to M, "Genio" to M, "Vario 125" to M, "Vario 160" to M, "Stylo 160" to M,
            "PCX 160" to M, "ADV 160" to M, "Supra X 125" to B, "Revo" to B, "Supra GTR 150" to B,
            "CB150 Verza" to B, "CRF150L" to B, "CB150R Streetfire" to S, "CBR150R" to S, "CBR250RR" to S,
            "EM1 e:" to E, "ICON e:" to E,
        ),
        brand(
            "Yamaha",
            "Mio M3" to M, "Gear 125" to M, "Fazzio" to M, "Grand Filano" to M, "FreeGo" to M, "Lexi LX 155" to M,
            "Aerox 155" to M, "NMAX 155" to M, "NMAX Turbo" to M, "XMAX" to M, "Jupiter Z1" to B, "Vega Force" to B,
            "MX King 150" to B, "WR155R" to B, "Vixion" to S, "R15" to S, "MT-15" to S, "XSR 155" to S, "R25" to S,
        ),
        brand(
            "Suzuki",
            "Nex II" to M, "Address" to M, "Burgman Street 125EX" to M, "Satria F150" to B, "GSX-R150" to S, "GSX-S150" to S,
        ),
        brand("Kawasaki", "W175" to B, "KLX 150" to B, "Ninja 250" to S, "Ninja ZX-25R" to S, "Z250" to S),
        brand("Vespa", "Sprint" to M, "Primavera" to M, "LX 125" to M, "GTS" to M),
        brand("TVS", "Callisto" to M, "Ntorq" to M, "Apache RTR 200" to S),
        brand("Alva", "One" to E, "Cervo" to E),
        brand("Gesits", "G1" to E, "Raya" to E),
        brand("Polytron", "Fox-R" to E, "Fox-S" to E),
        brand("United", "T1800" to E, "MX1200" to E),
        brand("Viar", "Q1" to E, "N1" to E),
    )

    val car: List<CatalogBrand> = listOf(
        brand(
            "Toyota",
            "Avanza" to C, "Veloz" to C, "Kijang Innova Zenix" to C, "Kijang Innova Reborn" to C, "Calya" to C,
            "Agya" to C, "Rush" to C, "Raize" to C, "Fortuner" to C, "Yaris Cross" to C, "Hilux" to C,
        ),
        brand("Daihatsu", "Sigra" to C, "Xenia" to C, "Ayla" to C, "Terios" to C, "Rocky" to C, "Gran Max" to C, "Luxio" to C),
        brand("Honda", "Brio" to C, "HR-V" to C, "WR-V" to C, "BR-V" to C, "City Hatchback" to C, "CR-V" to C, "Civic" to C),
        brand("Mitsubishi", "Xpander" to C, "Xpander Cross" to C, "Xforce" to C, "Pajero Sport" to C, "L300" to C, "Triton" to C),
        brand("Suzuki", "Ertiga" to C, "XL7" to C, "Carry Pick Up" to C, "Ignis" to C, "S-Presso" to C, "APV" to C),
        brand("Hyundai", "Creta" to C, "Stargazer" to C, "Ioniq 5" to C),
        brand("Wuling", "Air ev" to C, "BinguoEV" to C, "Almaz" to C, "Confero" to C),
        brand("BYD", "Atto 3" to C, "Dolphin" to C, "Seal" to C, "M6" to C),
        brand("Nissan", "Livina" to C, "Magnite" to C, "Kicks e-Power" to C),
        brand("Chery", "Tiggo 5X" to C, "Omoda 5" to C),
    )

    /** Brands that sell this kind; manual riders also see sport models. */
    fun brandsFor(kind: VehicleKind): List<CatalogBrand> {
        val source = if (kind == VehicleKind.CAR) car else motor
        return source.mapNotNull { b ->
            val models = b.models.filter { fits(it.kind, kind) }
            if (models.isEmpty()) null else b.copy(models = models)
        }
    }

    private fun fits(model: VehicleKind, wanted: VehicleKind) =
        model == wanted || (wanted == VehicleKind.MANUAL && model == VehicleKind.SPORT)

    fun find(brand: String, model: String): CatalogModel? =
        (motor + car).asSequence()
            .filter { it.name.equals(brand, ignoreCase = true) }
            .flatMap { it.models.asSequence() }
            .firstOrNull { it.name.equals(model, ignoreCase = true) }
}
