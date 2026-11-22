package id.cekendaraan.app.data

enum class Kind { COMPONENT, TREATMENT, PAPERWORK }

data class ComponentTemplate(
    val key: String,
    val name: String,
    val icon: String,
    val category: String,
    val km: Int?,
    val months: Int?,
    val important: Boolean = false,
    val kind: Kind = Kind.COMPONENT,
    /** What happens if it is ignored. Shown on the setup screen. */
    val risk: String = "",
    /** Short care tip. Shown on the component screen. */
    val tip: String = "",
)

/**
 * Default intervals follow the Honda Vario 160 / Vario Evo 160 service book and common
 * AHASS practice. They are a starting point, not a guarantee: every interval is editable.
 */
object Seed {
    val templates = listOf(
        // Komponen
        ComponentTemplate(
            "brake_disc", "Piringan Cakram", "disc", "Rem", 30_000, null, important = true,
            risk = "Piringan yang menipis atau bengkok membuat pengereman bergetar dan jarak berhenti makin jauh.",
            tip = "Cek ketebalan dan permukaan piringan setiap ganti kampas rem.",
        ),
        ComponentTemplate(
            "front_brake", "Kampas Rem", "brake", "Rem", 12_000, 12, important = true,
            risk = "Kampas habis membuat rem blong dan merusak piringan cakram.",
            tip = "Periksa sisa kampas tiap servis berkala, ganti bila di bawah batas.",
        ),
        ComponentTemplate(
            "brake_fluid", "Minyak Rem", "fluid", "Rem", 24_000, 24, important = true,
            risk = "Minyak rem menyerap air, titik didihnya turun dan rem terasa lembek.",
            tip = "Jaga level di antara garis MIN dan MAX, ganti sesuai jadwal.",
        ),
        ComponentTemplate(
            "engine_oil", "Oli Mesin", "oil", "Pelumas", 4_000, 6, important = true,
            risk = "Oli mengental dan kehilangan daya lumas, mesin kasar dan cepat aus.",
            tip = "Ganti oli sesuai interval dan cek level secara rutin.",
        ),
        ComponentTemplate(
            "oil_filter", "Saringan Oli", "filter", "Pelumas", 8_000, 12, important = true,
            risk = "Saringan tersumbat membuat kotoran ikut beredar di mesin.",
            tip = "Bersihkan saringan oli bersamaan dengan ganti oli.",
        ),
        ComponentTemplate(
            "rear_tire", "Ban", "tire", "Ban", 20_000, 48, important = true,
            risk = "Ban gundul mudah selip saat hujan dan rawan bocor.",
            tip = "Cek tekanan angin tiap minggu dan lihat indikator keausan (TWI).",
        ),
        ComponentTemplate(
            "air_filter", "Filter Udara", "air", "Mesin", 12_000, 12,
            risk = "Filter kotor membuat tarikan berat dan bensin lebih boros.",
            tip = "Ganti lebih cepat bila sering lewat jalan berdebu.",
        ),
        ComponentTemplate(
            "battery", "Aki", "battery", "Kelistrikan", null, 24,
            risk = "Aki soak membuat starter lemah dan sistem injeksi bermasalah.",
            tip = "Cek tegangan aki tiap servis, normalnya 12,5–13 V saat mesin mati.",
        ),
        ComponentTemplate(
            "coolant", "Cairan Radiator", "coolant", "Mesin", 24_000, 24,
            risk = "Cairan habis atau kotor membuat mesin overheat.",
            tip = "Pantau level di tabung reservoir saat mesin dingin.",
        ),
        ComponentTemplate(
            "cvt_belt", "V-Belt", "belt", "CVT", 24_000, 24,
            risk = "V-belt retak bisa putus di jalan dan motor tidak bisa jalan.",
            tip = "Periksa retakan dan lebar belt setiap servis CVT.",
        ),
        ComponentTemplate(
            "roller", "Roller CVT", "roller", "CVT", 24_000, 24,
            risk = "Roller peyang membuat akselerasi tersendat dan getar.",
            tip = "Ganti roller satu set agar beratnya seimbang.",
        ),
        ComponentTemplate(
            "clutch_spring", "Per CVT", "spring", "CVT", 24_000, 24,
            risk = "Per lemah membuat tarikan bawah loyo dan kopling selip.",
            tip = "Periksa bersamaan dengan kampas ganda dan mangkok kopling.",
        ),
        ComponentTemplate(
            "fuel_filter", "Filter Bensin", "fuel", "Mesin", 16_000, 24,
            risk = "Filter tersumbat membuat mesin tersendat dan sulit hidup.",
            tip = "Isi bensin di SPBU terpercaya agar filter awet.",
        ),
        ComponentTemplate(
            "spark_plug", "Busi", "plug", "Mesin", 12_000, 12,
            risk = "Busi aus membuat mesin susah hidup dan pembakaran tidak sempurna.",
            tip = "Gunakan tipe busi sesuai buku manual.",
        ),
        ComponentTemplate(
            "gear_oil", "Oli Gardan", "gear", "Pelumas", 8_000, 12,
            risk = "Gigi rasio cepat aus dan muncul bunyi dengung dari gardan.",
            tip = "Ganti setiap dua kali ganti oli mesin.",
        ),
        ComponentTemplate(
            "shock_oil", "Oli Shock", "shock", "Kaki-kaki", 20_000, 24,
            risk = "Shock bocor membuat motor mantul dan tidak stabil.",
            tip = "Perhatikan rembesan oli di tabung shock depan.",
        ),

        ComponentTemplate(
            "chain", "Rantai & Gir", "belt", "Penggerak", 15_000, 12, important = true,
            risk = "Rantai kendur atau gir aus bisa lepas saat jalan dan merusak bak mesin.",
            tip = "Lumasi rantai tiap 500 km dan jaga kekencangannya 2–3 cm.",
        ),
        ComponentTemplate(
            "clutch_plate", "Kampas Kopling", "disc", "Penggerak", 20_000, 24,
            risk = "Kopling selip membuat tenaga hilang dan motor susah menanjak.",
            tip = "Hindari menahan setengah kopling terlalu lama.",
        ),
        ComponentTemplate(
            "ac_filter", "Filter AC", "air", "Kabin", 10_000, 12,
            risk = "Filter kotor membuat AC kurang dingin dan kabin berbau.",
            tip = "Ganti lebih cepat bila sering di jalan berdebu.",
        ),
        ComponentTemplate(
            "transmission_oil", "Oli Transmisi", "gear", "Pelumas", 40_000, 24, important = true,
            risk = "Perpindahan gigi kasar dan transmisi cepat aus.",
            tip = "Gunakan spesifikasi oli sesuai jenis transmisi (MT/AT/CVT).",
        ),
        ComponentTemplate(
            "timing_belt", "Timing Belt", "belt", "Mesin", 100_000, 60, important = true,
            risk = "Timing belt putus bisa merusak klep dan piston.",
            tip = "Ganti bersama tensioner dan pompa air bila disarankan bengkel.",
        ),
        ComponentTemplate(
            "wiper", "Karet Wiper", "water", "Eksterior", null, 12,
            risk = "Kaca tergores dan pandangan buram saat hujan.",
            tip = "Bersihkan karet wiper saat mencuci mobil.",
        ),

        // Perawatan
        ComponentTemplate(
            "cvt_service", "Servis CVT", "cvt", "Perawatan", 8_000, 12, kind = Kind.TREATMENT,
            risk = "Debu kampas menumpuk, tarikan jadi bergetar dan berisik.",
            tip = "Bersihkan ruang CVT dan cek komponennya.",
        ),
        ComponentTemplate(
            "throttle_body", "Bersihkan Throttle Body", "water", "Perawatan", 10_000, 12, kind = Kind.TREATMENT,
            risk = "Kerak karbon membuat langsam naik turun dan mesin brebet.",
            tip = "Lakukan reset ECU setelah throttle body dibersihkan.",
        ),
        ComponentTemplate(
            "injector", "Bersihkan Injektor", "fuel", "Perawatan", 12_000, 12, kind = Kind.TREATMENT,
            risk = "Semprotan injektor tidak rata, bensin boros dan tenaga turun.",
            tip = "Bisa dilakukan dengan alat injector cleaner di bengkel resmi.",
        ),
        ComponentTemplate(
            "periodic_service", "Servis Berkala", "service", "Perawatan", 4_000, 6, kind = Kind.TREATMENT,
            risk = "Masalah kecil tidak terdeteksi dan bisa jadi kerusakan besar.",
            tip = "Ikuti jadwal servis berkala di buku servis.",
        ),
        ComponentTemplate(
            "wheel_bearing", "Cek Bearing Roda", "bearing", "Perawatan", 12_000, 12, kind = Kind.TREATMENT,
            risk = "Bearing aus membuat roda oblak dan berbunyi.",
            tip = "Angkat roda dan goyangkan untuk cek kelonggaran.",
        ),
        ComponentTemplate(
            "valve", "Setel Klep", "tune", "Perawatan", 8_000, 12, kind = Kind.TREATMENT,
            risk = "Celah klep tidak pas membuat mesin berisik dan tenaga turun.",
            tip = "Setel klep saat mesin dalam kondisi dingin.",
        ),
        ComponentTemplate(
            "battery_check", "Cek Aki", "battery", "Perawatan", null, 6, kind = Kind.TREATMENT,
            risk = "Aki melemah tanpa disadari dan mogok di jalan.",
            tip = "Ukur tegangan dan cek terminal dari karat.",
        ),

        // Dokumen
        ComponentTemplate(
            "stnk", "Pajak STNK", "doc", "Dokumen", null, 12, kind = Kind.PAPERWORK,
            risk = "Telat bayar pajak kena denda dan bisa ditilang.",
            tip = "Pajak tahunan bisa dibayar lewat aplikasi Samsat daerah.",
        ),
        ComponentTemplate(
            "plate", "Plat Nomor", "plate", "Dokumen", null, 60, kind = Kind.PAPERWORK,
            risk = "Plat nomor kedaluwarsa dianggap tidak sah di jalan.",
            tip = "Ganti plat bersamaan dengan pajak 5 tahunan.",
        ),
        ComponentTemplate(
            "sim", "SIM", "license", "Dokumen", null, 60, kind = Kind.PAPERWORK,
            risk = "SIM mati harus membuat baru, bukan perpanjangan.",
            tip = "Perpanjang SIM sebelum tanggal berlakunya habis.",
        ),
        ComponentTemplate(
            "insurance", "Asuransi Kendaraan", "shield", "Dokumen", null, 12, kind = Kind.PAPERWORK,
            risk = "Kendaraan tidak terlindungi saat terjadi kehilangan atau kecelakaan.",
            tip = "Cek masa berlaku polis setiap tahun.",
        ),
        ComponentTemplate(
            "emission", "Uji Emisi", "emission", "Dokumen", null, 12, kind = Kind.PAPERWORK,
            risk = "Di beberapa daerah kendaraan tanpa uji emisi bisa ditilang.",
            tip = "Uji emisi tersedia di bengkel resmi dan beberapa SPBU.",
        ),
    )

    private val byKey = templates.associateBy { it.key }

    private val matic = setOf(VehicleKind.MATIC)
    private val geared = setOf(VehicleKind.MANUAL, VehicleKind.SPORT)
    private val combustion = setOf(VehicleKind.MATIC, VehicleKind.MANUAL, VehicleKind.SPORT, VehicleKind.CAR)
    private val motor = setOf(VehicleKind.MATIC, VehicleKind.MANUAL, VehicleKind.SPORT, VehicleKind.ELECTRIC)
    private val car = setOf(VehicleKind.CAR)

    /** Which vehicle kinds each template makes sense for; missing keys fit every kind. */
    private val fitsOnly: Map<String, Set<VehicleKind>> = mapOf(
        "cvt_belt" to matic, "roller" to matic, "clutch_spring" to matic, "cvt_service" to matic, "gear_oil" to matic,
        "chain" to geared, "clutch_plate" to geared,
        "engine_oil" to combustion, "oil_filter" to combustion, "air_filter" to combustion, "fuel_filter" to combustion,
        "spark_plug" to combustion, "coolant" to combustion, "throttle_body" to combustion, "injector" to combustion,
        "valve" to combustion,
        "shock_oil" to motor, "sim" to setOf(*VehicleKind.entries.toTypedArray()),
        "ac_filter" to car, "transmission_oil" to car, "timing_belt" to car, "wiper" to car,
    )

    fun fits(template: ComponentTemplate, vehicle: VehicleKind): Boolean =
        fitsOnly[template.key]?.contains(vehicle) ?: true

    fun template(key: String): ComponentTemplate? = byKey[key]

    fun kindOf(templateKey: String, category: String): Kind =
        byKey[templateKey]?.kind ?: when (category) {
            "Perawatan" -> Kind.TREATMENT
            "Dokumen" -> Kind.PAPERWORK
            else -> Kind.COMPONENT
        }
}
