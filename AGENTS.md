# AGENTS.md — Cek Kendaraan

Panduan untuk agen AI (Claude Code, Antigravity, Gemini, Codex, Cursor, dll.) yang mengerjakan proyek ini. Baca file ini dulu, lalu [docs/DESIGN.md](docs/DESIGN.md) untuk tampilan dan [docs/ROADMAP.md](docs/ROADMAP.md) untuk pekerjaan berikutnya.

## Produk

Aplikasi Android pelacak perawatan kendaraan (motor dan mobil) untuk pengguna Indonesia. Fiturnya:

- Mencatat komponen (oli, kampas rem, ban, …), perawatan, dan dokumen (STNK, SIM).
- Menghitung sisa umur tiap item dari jarak (km) dan waktu (bulan). Yang lebih dulu habis menentukan status.
- Mencatat riwayat servis, mengirim pengingat harian, dan menyediakan scan odometer.
- Offline-first: semua data ada di perangkat (Room). Tidak ada backend, akun, atau iklan.

Bahasa antarmuka: **Bahasa Indonesia**. Tema terpilih: **Neon Cockpit** (gelap, aksen cyan).

## Stack

- Kotlin 2.0.21, Jetpack Compose (BOM 2024.12.01), Material 3, AGP 8.7.3, Gradle 8.9.
- Room 2.6.1 (KSP), WorkManager 2.9.1. minSdk 26, targetSdk 35.
- Font: **Manrope** variabel (`app/src/main/res/font/manrope.ttf`, lisensi OFL di `FONT-LICENSE.txt`).
- Package: `id.cekendaraan.app`.

## Build & jalankan

```powershell
$env:JAVA_HOME = "D:\AndroidDev\jdk17"   # JDK tidak ada di PATH
./gradlew assembleRelease                # ±2–3 menit saat dingin; jangan dipotong timeout
D:\AndroidDev\sdk\platform-tools\adb.exe install -r app/build/outputs/apk/release/app-release.apk
```

- Build **release** (R8 aktif, ditandatangani kunci debug) dipakai untuk menilai kelancaran. Build debug Compose memang lambat.
- Setelah build, **cek timestamp APK**. Build yang terpotong tetap meninggalkan APK lama yang terpasang dengan status "Success".
- HP uji: OnePlus CPH2591, 720×1612, 320 dpi (1 dp = 2 px).
  - ADB sering berubah jadi `unauthorized`. Jalankan `adb kill-server` lalu `adb start-server`, atau minta pengguna menekan Izinkan di HP.
  - `pm clear` diblokir OnePlus. Untuk reset data pakai menu Akun/Garasi → Hapus semua data, atau uninstall/install.
- Ukur kelancaran scroll: `adb shell dumpsys gfxinfo id.cekendaraan.app` (target < 3% janky frames).

## Struktur kode

```
app/src/main/java/id/cekendaraan/app/
  data/
    Db.kt          Room: Vehicle, Component, ServiceEvent, ServiceItem, DAO, GarageRepository, MIGRATION_1_2
    Catalog.kt     VehicleKind (MATIC/MANUAL/SPORT/ELECTRIC/CAR), merek & model populer Indonesia, slug()
    Seed.kt        ComponentTemplate (Kind: COMPONENT/TREATMENT/PAPERWORK), interval default, Seed.fits(template, kind)
    Prefs.kt       SharedPreferences: pengingat aktif, catatan per komponen
    Reminder.kt    WorkManager notifikasi harian 08.00
  domain/Due.kt    dueInfo(): status OK/SOON(≥80%)/DUE(≥100%)/UNKNOWN dari km & bulan
  ui/
    theme/Theme.kt Token warna (objek C) + Typography
    CekKendaraanApp.kt  navigasi state-based (tanpa Navigation library), FloatingNav, dialog global
    HomeScreen.kt  beranda, VehicleHero + rememberVehicleArt (gambar dicari dari nama file)
    ComponentScreen.kt / ComponentSetupScreen.kt  detail item & form tambah item
    HistoryScreen.kt, GarageScreen.kt, ExtraScreens.kt (Trip, Bengkel, Cari), OnboardingScreen.kt
    Components.kt  komponen UI bersama (tombol, field, ikon komponen, format angka Fmt)
    Particles.kt   animasi partikel onboarding
    Sheets.kt      bottom sheet catat servis, ubah kendaraan, dialog odometer
res/drawable/ic_comp_*.xml       ikon komponen 48dp (dibuat oleh skrip, lihat di bawah)
res/drawable-nodpi/vehicle_*.webp gambar kendaraan transparan (vehicle_<merek_model> atau vehicle_kind_<jenis>)
res/raw/keep.xml                  menjaga resource yang dicari lewat nama agar tidak dibuang R8
tools/cutout.py      hapus latar putih render → WebP transparan
tools/fetch_refs.py  unduh foto referensi ke art/image/
art/                 aset sumber, PROMPTS.md (prompt Nano Banana), mockup-tema.html (acuan desain)
```

## Aturan penting

1. **Desain mengikuti [docs/DESIGN.md](docs/DESIGN.md)** dan mockup `art/mockup-tema.html` (tema Neon). Jangan menebak ukuran, warna, atau ketebalan huruf. Pakai token yang ada, jangan menulis warna hex langsung di layar.
2. **Bukan tiruan RideCheck.** Proyek ini awalnya terinspirasi RideCheck, tapi sekarang punya identitas sendiri. Jangan menyalin nama, logo, teks, susunan menu, atau aset aplikasi lain.
3. **Tetap ringan.** Tanpa 3D runtime (SceneView/Filament pernah dicoba lalu dicabut karena berat), dan tanpa library besar tanpa alasan. APK release sekarang ±1,9 MB.
4. **Foto di `art/image/` hanya referensi** (milik pabrikan/dealer). Jangan pernah dimasukkan ke `res/`. Gambar kendaraan di aplikasi harus hasil generate sendiri (lihat `art/PROMPTS.md`).
5. **Skema database:** setiap perubahan entity wajib menaikkan `version` dan menambah `Migration`. Data pengguna tidak boleh hilang. `fallbackToDestructiveMigration` hanya jaring pengaman.
6. **Teks Bahasa Indonesia yang jelas.** Format angka Indonesia (`15.420 km`, `Rp85.000`, `14 Mar 2027`). Status ditulis dengan kata, bukan hanya warna. Detailnya ada di DESIGN.md → Penulisan.
7. **Tanpa data palsu.** Fitur yang belum ada ditandai "segera hadir", jangan dibuat seolah berfungsi. Jangan menampilkan jarak atau jumlah bengkel yang tidak benar-benar dihitung.
8. **Verifikasi di HP** (atau emulator) sebelum menyatakan selesai: screenshot, cek teks terpotong, cek scroll.

## Konvensi kode

- Compose saja (tanpa XML layout). State layar disimpan di `GarageViewModel.ui` (`StateFlow<UiState>`).
- Ikon komponen lewat `componentIcon(key)` (keys: oil, brake, disc, fluid, tire, belt, plug, …). Ikon baru dibuat lewat generator (lihat ROADMAP → Aset) supaya gayanya seragam.
- Gambar kendaraan baru cukup ditambahkan dengan nama yang benar, tanpa ubah kode (`rememberVehicleArt`).
- Komentar kode dalam bahasa Inggris, singkat, menjelaskan "kenapa". Teks UI dalam Bahasa Indonesia.

## Status & dokumen lain

- [docs/ROADMAP.md](docs/ROADMAP.md): tugas berikutnya beserta kriteria selesai.
- `PLANNING.md`: rencana fitur awal (sebagian sudah usang, terutama bagian "parity RideCheck").
- `art/PROMPTS.md`: cara membuat gambar kendaraan dengan gaya yang sama.
