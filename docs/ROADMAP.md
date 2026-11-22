# ROADMAP.md — pekerjaan berikutnya

Kerjakan berurutan. Setiap tugas punya kriteria selesai yang bisa dicek. Desain mengikuti [DESIGN.md](DESIGN.md), dan aturan umum ada di [../AGENTS.md](../AGENTS.md).

## Kondisi saat ini (22 Nov 2026)

Sudah berjalan di HP:

- Onboarding dengan partikel animasi, pilih jenis (motor matic/manual/listrik, mobil), merek dan model dari katalog, nama, dan odometer.
- Beranda bergaya lama (grid kartu ala RideCheck, sedang diganti).
- Detail item, setup item, Riwayat, Garasi, Trip (placeholder), Bengkel (membuka Google Maps), Cari.
- Template komponen per jenis kendaraan, pengingat harian, migrasi DB 1→2 (kolom `vehicles.kind`).

Palet Neon sudah ada di `Theme.kt`, tapi tipografi masih `FontFamily.SansSerif` dan layar belum mengikuti DESIGN.md.

> Perhatian: perubahan palet terakhir (`Theme.kt` dan beberapa warna di layar) **belum pernah di-build**. Mulai dengan `./gradlew assembleRelease` dan perbaiki error kompilasi bila ada.

Gambar kendaraan: jalankan `python tools/batch_cutout.py`. Skrip ini melewati gambar yang sudah jadi, jadi aman diulang bila proses sebelumnya terhenti. Hasilnya `res/drawable-nodpi/vehicle_*.webp` dan `vehicle_kind_*`.

## 1. Fondasi tema Neon

- `Theme.kt`:
  - token sesuai tabel DESIGN.md (tambahkan `bgTop`, `ink`, dan `acc2` bernama jelas),
  - Typography memakai Manrope (`R.font.manrope`, variable weights 500/600/700),
  - text style bernama `Display`, `H1`, `H2`, `Stat`, `Item`, `Small`, `Body`, `Meta`, `Label`, `Caption`.
- `Components.kt`: PrimaryButton (gradasi), SecondaryButton, IconButton 42dp, Chip, StatusTag, ItemRow, InfoCard (kunci–nilai), ProgressBar 5dp, GaugeRing.
- **Selesai bila:** semua layar dibangun dari komponen ini, tidak ada `Color(0x…)` di file layar, dan tidak ada `fontWeight` di luar 500/600/700.

## 2. Navigasi baru

- Ganti FloatingNav menjadi **Dasbor · Jurnal · [Scan] · Layanan · Akun**.
- Garasi dipindah ke tab Akun. Trip dan Riwayat dilebur ke Jurnal. Bengkel menjadi Layanan.
- Tombol Scan membuka layar scan (tugas 5).
- **Selesai bila:** tombol back dari tab non-Dasbor kembali ke Dasbor, dan state tab bertahan saat rotasi.

## 3. Dasbor & Detail item

- Dasbor sesuai DESIGN.md → Layar 1:
  - "Kondisi" = rata-rata sisa umur item aktif (0–100%),
  - "Servis berikutnya" = hari menuju item terdekat,
  - "Perlu perhatian" = item SOON/DUE, maksimal 3.
- Katalog item lama (grid template) dipindah ke sheet "Tambah item" dengan pencarian dan filter Komponen/Perawatan/Dokumen.
- Detail item sesuai Layar 2, termasuk bilah aksi tetap. "Ubah interval" membuka sheet interval yang sekarang ada di bagian "Atur Umur Pakai".
- **Selesai bila:** screenshot di HP cocok dengan `art/mockup-tema.html` (Neon), tidak ada teks terpotong, dan scroll janky < 3%.

## 4. Jurnal, Layanan, Akun

- **Jurnal:** gabungan `service_events` dan catatan odometer. Catatan odometer perlu tabel baru `odometer_logs(id, vehicleId, km, at, source)` dengan `source` = manual/scan, plus `MIGRATION_2_3`.
- **Layanan:** kueri Maps per merek/jenis (logika sekarang ada di `ExtraScreens.kt`, fungsi `shopQueries`), dan dokumen dari item ber-Kind PAPERWORK.
- **Akun:** isi `GarageScreen` dipecah ke pola grup DESIGN.md → Layar 6. "Cadangkan data" ditandai Segera hadir.
- **Selesai bila:** update odometer dari mana pun tercatat di Jurnal, dan migrasi diuji dengan memasang di atas versi lama (data tetap ada).

## 5. Scan odometer (kamera + OCR on-device)

- Dependensi:
  - `androidx.camera:camera-camera2`, `camera-lifecycle`, `camera-view` (1.4.x),
  - `com.google.android.gms:play-services-mlkit-text-recognition` (model lewat Play Services, APK tetap kecil).
- Izin `CAMERA` diminta saat layar scan dibuka. Kalau ditolak, tampilkan pesan dan tombol "Ketik manual".
- `ImageAnalysis` (`STRATEGY_KEEP_ONLY_LATEST`):
  1. Kenali teks dan ambil token angka 3–7 digit (abaikan titik, koma, dan spasi di dalamnya).
  2. Utamakan token yang kotak teksnya berada di tengah bingkai.
  3. Angka dianggap stabil bila sama di 3 frame berturut-turut.
- Sheet hasil menampilkan angka, selisih dengan odometer terakhir, dan peringatan bila angkanya lebih kecil. Tombol Simpan memperbarui odometer dan menulis `odometer_logs` dengan `source = scan`. Tombol Ketik manual membuka dialog odometer.
- Tidak ada foto yang disimpan, dan semua proses terjadi di perangkat.
- **Selesai bila:** angka odometer motor asli terbaca dalam ±3 detik dalam cahaya normal, salah baca bisa dikoreksi, dan APK naik < 1 MB.

## 6. Usulan berikutnya (belum disetujui pengguna)

- Catat bensin (liter, harga, km/L) yang tampil di Jurnal.
- Widget layar utama (item paling mendesak dan odometer).
- Pengingat mingguan "perbarui odometer".
- Cadangkan dan pulihkan data ke file.

## Aset

- Ikon komponen dibuat oleh `tools/icons.py` (48dp, glyph putih): tambahkan entri di `ICONS`, jalankan `python tools/icons.py app/src/main/res/drawable preview.html`, lalu daftarkan key-nya di `componentIconRes()` (`Components.kt`). Gaya: glyph padat, sudut membulat, satu warna.
- Gambar kendaraan: generate dengan Nano Banana (`art/PROMPTS.md`), proses dengan `python tools/cutout.py art/<file>.jpg app/src/main/res/drawable-nodpi/vehicle_<key>.webp`, lebar maksimal 640px.
