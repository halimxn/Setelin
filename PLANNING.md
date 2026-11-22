# Planning — Cek Kendaraan (Android)

Dokumen ini merencanakan aplikasi Android **Cek Kendaraan**: pelacak perawatan mobil & motor dengan pengalaman yang sama seperti **RideCheck** (Play Store v4.5.1, Nov 2026), dibangun ulang dengan brand dan aset sendiri.

- Status: draft untuk disetujui
- Tanggal: 21 November 2026
- Target rilis: MVP internal ±3 minggu, parity penuh ±11–13 minggu (1 developer)
- Platform: Android native (Kotlin + Jetpack Compose), offline-first

---

## 1. Ringkasan produk

Pengguna mencatat kendaraan dan komponennya (oli, ban, rem, aki, filter, dll.). Setiap komponen punya interval **jarak (km)** dan/atau **waktu (bulan)**. Aplikasi menghitung sisa pemakaian, menampilkan status komponen, mengingatkan saat jatuh tempo, dan menyimpan riwayat servis. Perjalanan harian bisa dicatat otomatis/manual untuk memperbarui odometer. Ada direktori bengkel, QR servis, widget, dan paket Pro.

Yang membuat RideCheck terasa berbeda dari aplikasi catatan servis biasa:

1. **Tampilan kendaraan 3D interaktif** dengan hotspot komponen.
2. **Hitung mundur ganda** (km dan waktu) per komponen, bukan sekadar pengingat tanggal.
3. **Odometer ikut bertambah dari perjalanan** yang direkam GPS.
4. **Riwayat servis rapi per tanggal + odometer**, bisa diekspor PDF.
5. **Pengingat dokumen** (pajak & STNK).
6. **Bengkel mitra + scan QR** untuk menulis servis ke riwayat.
7. **Widget** yang menampilkan komponen paling mendesak.

## 2. Asumsi & batasan

- Bahasa Indonesia dulu, struktur siap untuk Inggris.
- Satuan km (miles opsional di pengaturan).
- Fokus mobil & motor pribadi. Armada/bisnis, iOS, OBD, dan pencatat bensin **di luar scope** (RideCheck juga tidak mencatat bensin).
- Prioritas offline: semua data inti di Room; akun dan sinkronisasi opsional.
- Tidak ada backend pada v1. Backup lewat file/Google Drive, bengkel dari data seed + Google Maps, QR berisi deep link.
- **Tidak menyalin** nama "RideCheck", logo, screenshot, model 3D, atau teks marketing. Yang disamakan adalah fungsi dan pola UX. Ini penting untuk rilis Play Store.

## 3. Matriks fitur (parity RideCheck)

| Fitur | Perilaku RideCheck | Rencana Cek Kendaraan | Fase | Prioritas |
|---|---|---|---|---|
| Multi kendaraan | Mobil/motor, Pro untuk >1 | CRUD kendaraan + foto, gratis 1, Pro unlimited | 1 | P0 |
| Template komponen | Oli, filter, rem, aki, busi, timing belt, ban, cairan, dll. | Seed JSON per jenis: bensin, diesel, EV, motor | 1 | P0 |
| Komponen custom | Tambah sendiri | Ya, nama + ikon + interval | 1 | P0 |
| Interval km / bulan / keduanya | Hitung mana yang lebih dulu | Sama; validasi minimal satu interval | 1 | P0 |
| Status komponen | Aman / Periksa segera / Perlu diganti | Ambang 80% dan 100% dari rasio terburuk | 1 | P0 |
| Catat servis | Tanggal + odometer, reset hitungan | Bottom sheet, bisa banyak komponen sekaligus | 1 | P0 |
| Riwayat servis | Timeline per tanggal, detail, PDF | Timeline + detail + tambah servis lama | 1–2 | P0 |
| Edit/hapus/undo riwayat | Ada | Ada; baseline komponen dihitung ulang | 2 | P1 |
| Pengingat servis | Notifikasi mendekati jatuh tempo | WorkManager harian + deep link | 2 | P1 |
| Pengingat pajak/STNK | Ada | Dokumen + tanggal + H-30/H-7 | 2 | P1 |
| Widget | Status komponen + mulai trip | Glance: 3 komponen teratas + odometer | 2 | P1 |
| Ekspor PDF | Riwayat servis | PdfDocument + FileProvider/share | 2 | P1 |
| Pencatatan perjalanan | Auto & manual, rute, statistik | Manual dulu, auto-track fase 3 | 3 | P1 |
| Peta rute | Google Maps, garis rute | MapLibre/OSM atau Google Maps | 3 | P1 |
| Bagikan perjalanan | Kartu stiker / card only | Render gambar Compose → share | 3 | P2 |
| Bengkel terdekat | Peta, telepon, mitra | Seed + buka Google Maps; mitra via backend nanti | 4 | P2 |
| Scan QR servis | Mekanik menulis riwayat | QR deep link membuka form servis | 4 | P2 |
| Rating mekanik | Ada | Lokal (butuh backend untuk publik) | 4 | P3 |
| Tampilan 3D | Model 3D + hotspot + orbit | 2D interaktif dulu, 3D di fase 5 | 1 / 5 | P2 |
| Akun & backup | Google Sign-In | Tamu dulu; export/import; Drive opsional | 4 | P2 |
| Pro (langganan/lifetime) | 6 bulan / lifetime | Play Billing, 2 produk | 4 | P2 |
| Bahasa | 9 bahasa | ID + EN | 2 | P2 |
| Mode gelap | Default gelap | Default gelap, opsi terang | 1 | P1 |
| Lipat & tablet | Didukung | Layout adaptif dasar | 5 | P3 |

## 4. Peta layar

Bottom navigation 5 tab: **Kendaraan · Perjalanan · Riwayat · Bengkel · Akun**.

### 4.1 Onboarding
- 3 slide nilai jual (lupa jadwal servis → hitung otomatis → semua di satu tempat).
- Pilih bahasa, lalu "Mulai sebagai tamu" atau Google Sign-In (opsional).
- Tambah kendaraan pertama: jenis (mobil/motor), merek, model, tahun, bahan bakar, odometer, rata-rata km/bulan, foto (opsional).
- Pilih komponen dari template; isi "terakhir servis" bila tahu, atau tandai "belum tahu" (baseline hari ini + badge perkiraan).
- Permission notification diminta setelah pengguna menyimpan komponen, bukan di awal.

### 4.2 Kendaraan (home)
- Top bar: pemilih kendaraan, odometer (tap untuk update), tombol pengaturan.
- Hero kendaraan: 2D/3D interaktif, pinch/pan/orbit; hotspot berwarna sesuai status.
- Segmented tab: **Komponen · Perawatan · Info**.
  - Komponen: kartu per komponen berisi ikon, nama, "Sisa 1.200 km · 23 hari", pill status, progress bar km/waktu.
  - Perawatan: daftar komponen + interval + servis terakhir, aksi edit/hapus/urutkan.
  - Info: plat, tahun, bahan bakar, rata-rata km, catatan.
- Filter: Semua / Perlu tindakan / Penting.
- Tombol utama "Tambah perawatan" → bottom sheet catat servis.
- Empty state: ilustrasi + CTA pilih komponen.
- Kartu Pro di bawah untuk kendaraan kedua / auto-track / pengingat.

### 4.3 Tambah/edit komponen
- Pilih dari template (grup: Mesin, Cairan, Kaki-kaki, Kelistrikan, Eksterior; khusus diesel: glow plug, injektor, EGR/DPF, water separator; EV tanpa oli).
- Form: nama, ikon, interval km, interval bulan, penting?, pengingat?, servis terakhir (tanggal + km), catatan.
- Validasi: minimal satu interval; km tidak boleh 0; tanggal tidak di masa depan; km servis ≤ odometer.
- Hapus komponen: konfirmasi, riwayat servisnya tetap ada (ditandai komponen dihapus) agar history tidak hilang.

### 4.4 Catat servis
- Pilih satu/banyak komponen (default: yang sedang due).
- Tanggal, odometer, biaya (opsional), catatan, foto nota (opsional).
- Simpan → update baseline komponen dan buat satu `service_event`.
- Kalau odometer lebih besar dari odometer kendaraan, tawarkan update odometer kendaraan.

### 4.5 Riwayat
- Grup per bulan/tanggal; tiap baris: tanggal, odometer, daftar item, biaya total.
- Detail: komponen, catatan, foto, sumber (manual/QR).
- Aksi: edit, hapus (dengan undo), tambah servis lama, ekspor PDF rentang tanggal.
- Filter per kendaraan dan komponen.

### 4.6 Perjalanan
- Peta besar dengan rute terakhir + tombol mulai/stop.
- Mode Auto-Track (Pro) dan Manual.
- Kartu perjalanan: jarak, durasi, kecepatan tertinggi, rata-rata, elevasi.
- Detail: peta, statistik, data mengemudi sederhana (pengereman/akselerasi cepat), catatan GPS.
- Bagikan: pilihan **Stiker** (foto pengguna + overlay) atau **Card only**.
- Hapus perjalanan → tawarkan koreksi odometer (karena jarak sudah ditambahkan).

### 4.7 Bengkel
- Peta + list bengkel terdekat, jarak, alamat, telepon, "Rute".
- Detail bengkel: jam, layanan, tombol telepon/rute, scan QR.
- QR scanner: format `cekendaraan://service?vehicleId=…&workshop=…` → form servis terisi.
- Label "Mitra" hanya untuk data yang memang mitra; rating publik ditunda sampai ada backend.

### 4.8 Akun & pengaturan
- Tamu / login Google; status Pro.
- Tab: **Kelola · Pengaturan · Statistik**.
- Kelola: daftar kendaraan, tambah/edit/hapus, reset aplikasi.
- Pengaturan: satuan, bahasa, tema, notifikasi, pengingat dokumen, backup/restore, kebijakan privasi.
- Statistik: total servis, biaya per bulan, km per bulan, komponen paling sering.
- Paywall Pro; tombol "Pulihkan pembelian".

### 4.9 Dokumen & pengingat
- Pajak, STNK, asuransi: tanggal jatuh tempo, H-30/H-7, notifikasi.
- Tampil di kartu kendaraan dan widget bila mendekati.

### 4.10 Widget
- 2 ukuran: kecil (1 komponen teratas) dan sedang (3 komponen + odometer).
- Tombol "Mulai trip" membuka activity (bukan langsung service, karena batasan Android 12+).
- Update saat data berubah + sekali sehari.

## 5. Model data (Room)

```text
vehicles
  id (UUID, PK), name, kind (CAR|MOTORCYCLE|SCOOTER), brand, model, year,
  fuel (PETROL|DIESEL|EV|HYBRID), odometer_km, monthly_km,
  photo_uri, note, created_at, is_primary

components
  id (UUID, PK), vehicle_id (FK, index), template_key, name, icon_key,
  interval_km (nullable), interval_months (nullable),
  last_service_on (epochDay, nullable), last_service_odometer_km (nullable),
  important, reminder_enabled, sort_order, deleted_at (nullable)

service_events
  id (UUID, PK), vehicle_id (FK, index), serviced_on (epochDay), odometer_km,
  cost (nullable), note, photo_uri, source (MANUAL|QR), created_at, deleted_at

service_items
  id (UUID, PK), event_id (FK, index), component_id (FK, index),
  component_name_snapshot

documents
  id (UUID, PK), vehicle_id (FK), kind (TAX|STNK|INSURANCE),
  due_on (epochDay), notify_days_before, note

trips
  id (UUID, PK), vehicle_id (FK, index), started_at, ended_at,
  distance_km, duration_sec, max_speed_kph, avg_speed_kph, elevation_gain_m,
  polyline (encoded), auto_tracked, created_at

trips.raw points tidak disimpan permanen — hanya polyline terenkode.
workshop = data seed/remote read-only; tidak perlu tabel lokal untuk v1.
settings = DataStore (bahasa, satuan, tema, pro, akun).
```

Catatan:
- `epochDay` untuk tanggal servis/dokumen agar bebas zona waktu; timestamp milidetik untuk trip.
- Index: `components(vehicle_id)`, `service_events(vehicle_id, serviced_on)`, `service_items(component_id)`.
- Baseline komponen di-cache di `components` untuk query cepat; setiap edit/hapus `service_event` memicu rekalkulasi baseline dari event terbaru komponen tersebut.
- Hapus kendaraan = soft delete + konfirmasi; data servis tetap tersimpan 30 hari untuk undo.

## 6. Logika status & pengingat

### 6.1 Rasio pemakaian

```text
ratioKm   = (odometer - lastServiceOdometer) / intervalKm        (jika intervalKm diisi)
ratioTime = hariSejak(lastServiceDate) / hariDalamIntervalBulan   (jika intervalMonths diisi)
ratio     = max(ratioKm, ratioTime)
```

- `ratio < 0.80` → **Aman**
- `0.80 ≤ ratio < 1.00` → **Periksa segera**
- `ratio ≥ 1.00` → **Perlu diganti**

Jika `lastService` belum diketahui: baseline = odometer kendaraan saat komponen ditambahkan + tanggal hari ini, status diberi badge "perkiraan".

### 6.2 Sisa waktu

- Sisa km = `intervalKm - (odometer - lastServiceOdometer)`.
- Perkiraan tanggal due = hari ini + `sisaKm / rata-rata km per hari`.
- Rata-rata km/hari: dari trip 90 hari terakhir; fallback `monthlyKm / 30`; jika tidak ada keduanya, sembunyikan tanggal dan tampilkan km saja.
- Interval bulan memakai `LocalDate.plusMonths` (bukan 30 hari), agar 31 Jan + 1 bulan = 28/29 Feb.

### 6.3 Pengingat

- WorkManager harian ±08:00 zona lokal; cek komponen ratio ≥ 0.80 dan dokumen H-30/H-7.
- Satu notifikasi per komponen per 7 hari (dedupe via timestamp di DataStore).
- Channel terpisah: "Servis", "Dokumen", "Perjalanan".
- Deep link ke komponen/dokumen terkait.
- Hormati toggle per komponen, jangan kirim 21:00–08:00.
- Android 13+: izin `POST_NOTIFICATIONS`; OEM battery saver diberi panduan singkat.

### 6.4 Odometer

- Hanya boleh naik. Koreksi turun memerlukan konfirmasi dan menawarkan penyesuaian baseline komponen.
- Trip selesai menambah `round(distanceKm)` ke odometer kendaraan.
- Menghapus trip mengurangi jarak yang pernah ditambahkan (simpan `odometer_added` di trip).

## 7. Arsitektur teknis

### 7.1 Stack

Mengikuti pola proyek Android yang sudah ada (`kantong-android`): satu modul, Compose, Room, tanpa framework DI berat.

| Area | Pilihan |
|---|---|
| Bahasa | Kotlin 2.x, JVM 17 |
| UI | Jetpack Compose + Material 3, Navigation Compose |
| State | ViewModel + `StateFlow`, UiState immutable |
| DB | Room + KSP, Flow |
| Preferensi | DataStore Preferences |
| Background | WorkManager (pengingat), Foreground Service (trip) |
| Lokasi | FusedLocationProvider; Activity Recognition untuk auto-track |
| Peta | MapLibre + OSM (tanpa billing) — alternatif Google Maps jika alamat Indonesia lebih penting |
| Widget | Glance |
| 3D | Fase 5: SceneView + Filament (GLB); fase 1: vector 2D + hotspot |
| QR | ML Kit Barcode Scanning |
| PDF | Android `PdfDocument` + FileProvider |
| Share card | Compose render ke Bitmap |
| Billing | Play Billing 7 (subs 6 bulan + lifetime) |
| Gambar | Coil |
| Tes | JUnit, Truth, Turbine, Room testing, Compose UI test |

### 7.2 Struktur paket

```text
id.cekendaraan.app
├─ MainActivity.kt
├─ CekKendaraanApp.kt          // Application + AppContainer manual
├─ data/
│  ├─ local/ (AppDatabase, DAO, Converters)
│  ├─ prefs/ (SettingsStore, BackupStore)
│  ├─ repo/ (VehicleRepo, ServiceRepo, TripRepo, DocumentRepo, WorkshopRepo)
│  └─ seed/ (components_car.json, components_motor.json, workshops.json)
├─ domain/
│  ├─ DueCalculator.kt
│  ├─ OdometerRules.kt
│  ├─ TripStats.kt
│  └─ model/ (Vehicle, Component, ServiceEvent, Trip, DueStatus)
├─ ui/
│  ├─ theme/ (Token, Color, Type, Theme)
│  ├─ components/ (StatusPill, ComponentCard, VehicleHero, EmptyState, …)
│  └─ screens/ (onboarding, vehicle, component, service, history, trip, workshop, account, paywall)
├─ service/ (TripTrackingService, ReminderWorker, BootReceiver)
├─ widget/ (DueWidget, DueWidgetReceiver)
└─ billing/ (ProManager)
```

- Navigasi: 5 route top-level + nested graph; deep link `cekendaraan://`.
- `AppContainer` dibuat di `Application`; ViewModel mengambil repo lewat factory sederhana.
- Semua query berat di `Dispatchers.IO`; UI mengoleksi Flow dengan `stateIn`.
- Tidak ada multi-module sampai build > ~2 menit.

### 7.3 Trip tracking

- **Manual (fase 3):** mulai → foreground service tipe `location` + notifikasi progres; lokasi tiap 5–10 detik / 10 m; filter akurasi > 50 m dan kecepatan < 3 km/jam; stop → hitung jarak haversine, simpan polyline, tambah odometer.
- **Auto (fase 3b):** Activity Recognition Transition API mendeteksi `IN_VEHICLE`; mulai setelah 60 detik, stop setelah 2 menit diam. Butuh izin background location + activity recognition. Tampilkan edukasi izin dan opsi matikan.
- Batas realistis: kecepatan > 200 km/jam diabaikan; trip < 0,5 km tidak disimpan.
- Target baterai: < 5% per jam tracking; uji di 3 perangkat.
- Pemulihan crash: simpan titik terakhir tiap 60 detik ke file sementara.

### 7.4 2D → 3D

- Fase 1: satu vector drawable per jenis kendaraan (car, scooter, diesel SUV, EV), hotspot diposisikan lewat JSON relatif terhadap gambar. Status komponen mewarnai hotspot. Cukup untuk nuansa "3D" tanpa biaya seniman 3D.
- Fase 5: 2 model GLB (mobil, skuter) dengan node bernama sesuai `templateKey`. SceneView menangani orbit/zoom; tap node → komponen. Perlu lisensi model/artis; jangan pakai model RideCheck.
- Fallback: jika perangkat tidak mendukung Filament, tampilkan 2D.

## 8. Desain & UX

- **Default gelap**, near-black `#0B0B0D`, surface `#16161A`, aksen lime `#C8F560` untuk status aman, amber untuk periksa, merah untuk due. Warna bukan satu-satunya penanda status (ikon + teks).
- Tipografi: Plus Jakarta Sans / Inter; angka odometer tabular.
- Kartu radius 20 dp, spacing 4/8/12/16/24, target sentuh ≥ 48 dp.
- Hero kendaraan adalah fokus visual; sisanya tenang agar status mudah dipindai.
- Motion: transisi tab 200 ms, progress bar animasi saat status berubah; hormati "reduce motion".
- Salinan bahasa Indonesia santai tapi jelas: "Sisa 1.200 km", "Perlu diganti", bukan istilah bengkel yang rumit.
- Aksesibilitas: label TalkBack untuk hotspot ("Oli mesin, perlu diganti"), kontras ≥ 4.5:1, ukuran font mengikuti sistem.
- Empty/error/loading state untuk setiap layar; tidak ada layar kosong tanpa CTA.
- Onboarding izin: jelaskan manfaat sebelum dialog sistem (lokasi latar, notifikasi).

## 9. Fase pengerjaan

Estimasi 1 developer full-time, termasuk desain dan QA. Hari kerja.

| Fase | Isi | Output | Estimasi |
|---|---|---|---|
| **0. Fondasi** | Scaffold Compose, tema, navigasi, Room, seed template, AppContainer, CI build | APK jalan dengan data dummy | 3 hari |
| **1. MVP inti** | Onboarding, CRUD kendaraan, template & CRUD komponen, DueCalculator, catat servis, riwayat dasar, hero 2D, pengaturan satuan, tes unit kalkulator | Bisa dipakai harian secara offline | 12–15 hari |
| **2. Riwayat & pengingat** | Edit/hapus/undo, servis lama, PDF, dokumen pajak/STNK, WorkManager + notifikasi, widget, bahasa EN, statistik | Fitur "catatan servis lengkap" | 8–10 hari |
| **3. Perjalanan** | Manual tracking + foreground service, peta, statistik, share card, hapus/koreksi; auto-track + activity recognition | Odometer otomatis | 12–15 hari |
| **4. Bengkel & monetisasi** | Peta bengkel, telepon/rute, QR scanner, paywall, Play Billing, backup file, login Google + Drive opsional | Siap monetisasi | 10–12 hari |
| **5. Poles & rilis** | 3D SceneView + 2 model GLB, widget lipat, aksesibilitas, performa, screenshot store, privacy policy, closed testing, perbaikan bug | Produksi | 8–12 hari |
| | | **Total** | **±55–67 hari** |

Urutan bisa dipangkas: jika target hanya "cek kendaraan", fase 3–5 bisa ditunda dan aplikasi sudah berguna setelah fase 2.

### Definition of Done MVP (fase 1)
- Tambah motor/mobil + 6 komponen, isi interval km/bulan, catat servis, status berubah benar.
- Perhitungan km, bulan, dan kombinasi lulus unit test (termasuk akhir bulan dan servis di masa lalu).
- Data tetap ada setelah restart dan mode pesawat.
- Riwayat menampilkan servis terbaru di atas, dengan odometer.
- Tidak ada crash di alur utama; APK release ter-sign dan bisa di-install.
- Layar kosong, error, dan validasi form tertangani.

## 10. Rencana pengujian

**Unit (wajib):**
- `DueCalculator`: hanya km, hanya bulan, keduanya, overdue, baseline null, interval 0/invalid, 31 Jan + 1 bulan, servis di masa depan (ditolak).
- `OdometerRules`: trip menambah, hapus trip mengurangi, koreksi turun, servis dengan odo > kendaraan.
- Rekalkulasi baseline setelah edit/hapus event.
- Import/export JSON round-trip + versi skema.
- Filter noise trip (akurasi buruk, lompatan GPS, kecepatan mustahil).

**Instrumented:**
- DAO query + migrasi Room (v1 → v2).
- Compose UI: onboarding → tambah kendaraan → catat servis → status berubah.
- Widget render (screenshot test opsional).

**Manual:**
- Android 8, 10, 13, 15/16; layar kecil, lipat, tablet.
- Izin notifikasi ditolak/diizinkan; background location "allow all the time" vs "while using".
- Battery saver, reboot (pengingat & service pulih), zona waktu berubah, tanggal mundur.
- GPS mock: jalan kaki, motor, mobil; terowongan (titik hilang).
- Uji baterai 1 jam tracking; uji offline penuh.
- TalkBack + font 200%.

## 11. Rilis Play Store

- Package `id.cekendaraan.app`; AAB, R8, signing key produksi (bukan debug seperti proyek contoh).
- Target SDK mengikuti kebijakan Play terbaru saat rilis (minimal API 36 pada 2026 — verifikasi).
- Privacy policy + data safety form; deklarasi **background location** dengan video penjelasan (risiko review).
- Izin: lokasi (foreground/background opsional), activity recognition, notifikasi, kamera (QR), foto.
- Closed testing: 12 tester selama 14 hari untuk akun developer personal baru.
- Listing: nama "Cek Kendaraan", ikon, feature graphic, 6–8 screenshot (hero 3D, status, riwayat, trip, widget), deskripsi keyword "catatan servis mobil motor".
- Produk Pro: `pro_6m` (langganan) dan `pro_lifetime`; harga placeholder Rp 29.000 / Rp 99.000 — validasi nanti.
- Versi: `1.0.0` (fase 1–2), `1.1.0` (trip), `1.2.0` (bengkel/Pro), `1.3.0` (3D).

## 12. Risiko & mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Scope parity penuh terlalu besar | Rilis mundur | Rilis bertahap; MVP setelah fase 2; trip/3D/Pro menyusul |
| Aset 3D mahal & lama | Fitur hero tertunda | 2D interaktif dulu; 3D hanya 2 model; fallback 2D |
| Review Play menolak background location | Trip auto tidak bisa rilis | Manual tracking dulu; deklarasi jelas; siapkan opsi tanpa auto-track |
| Peta & geocoding berbiaya | Biaya operasional | MapLibre/OSM; Google Maps opsional dengan key dibatasi |
| Tidak ada backend | Bengkel mitra/QR/rating terbatas | Seed + deep link; backend hanya jika bisnis bengkel terbukti |
| Data hilang (offline-only) | Kepercayaan pengguna rusak | Auto-backup lokal harian, export manual, Drive opsional |
| Notifikasi tidak muncul di OEM tertentu | Fitur pengingat dianggap rusak | Panduan battery optimization; tampilkan status pengingat di app |
| Menyalin brand/aset RideCheck | Takedown/tertolak | Identitas sendiri; hanya parity fungsi |
| GPS mencatat jalan kaki | Odometer salah | Filter kecepatan/akurasi + edit/hapus trip |

## 13. Keputusan yang perlu diambil

1. **Nama & brand final** — "Cek Kendaraan" sementara; perlu nama unik untuk Play Store.
2. **Target rilis** — MVP dulu (±3 minggu) atau langsung parity penuh (±3 bulan)?
3. **3D di MVP?** Rekomendasi: tidak; 2D interaktif dulu.
4. **Auto-track di MVP?** Rekomendasi: tidak; manual dulu karena izin & baterai.
5. **Backend?** Rekomendasi: tidak untuk v1; backup file/Drive, bengkel seed.
6. **Monetisasi saat launch?** Rekomendasi: gratis dulu 1–2 rilis, Pro setelah ada pengguna.
7. **Sumber bengkel** — data seed kota awal, Google Places, atau kemitraan manual?
8. **Tim & deadline** — asumsi 1 developer; jika ada desainer/QA, estimasi bisa dipangkas.

---

### Ringkasan keputusan teknis

- Kotlin + Compose + Room, satu modul, offline-first, manual DI.
- 5 tab: Kendaraan, Perjalanan, Riwayat, Bengkel, Akun.
- Status dihitung dari rasio km dan waktu; ambang 80%/100%.
- MVP = kendaraan + komponen + servis + riwayat + notifikasi, tanpa GPS/3D/backend.
- Perjalanan, bengkel/QR, Pro, dan 3D menyusul per fase.
- Tidak menyalin aset atau identitas RideCheck.
