# DESIGN.md — Neon Cockpit

Sistem desain Cek Kendaraan. Acuan visual: `art/mockup-tema.html` (pilih tema **Neon Cockpit**). Prinsipnya:

- satu font,
- satu aksen,
- kontras dari lapisan warna, bukan garis tepi,
- teks jelas dan tidak berdesakan.

## Warna (token)

Definisikan sekali di `ui/theme/Theme.kt` (objek `C`). Layar tidak boleh memakai hex langsung.

| Token | Hex | Pakai untuk |
|---|---|---|
| `bg` | `#060A10` | latar halaman |
| `bgTop` | `#0E2332` | gradasi atas latar (0 → 300dp) |
| `s1` | `#0C141D` | kartu |
| `s2` | `#111C28` | tombol ikon, chip non-aktif, tombol sekunder |
| `s3` | `#1A2A3B` | track progress, switch off |
| `t1` | `#F2F8FC` | teks utama |
| `t2` | `#9AB0C2` | teks kedua (keterangan) |
| `t3` | `#5D7385` | teks ketiga (label, caption) |
| `acc` | `#22D3EE` | aksen: link, tab aktif, chip aktif |
| `acc2` | `#7C8CFF` | ujung gradasi aksen (tombol utama, tombol Scan) |
| `ink` | `#031820` | teks/ikon di atas aksen |
| `ok` | `#34D399` | status aman |
| `warn` | `#FBBF24` | status segera |
| `bad` | `#F87171` | status lewat / bahaya |

- Latar ikon status memakai warna status dengan alpha 13%, misalnya `ok.copy(alpha = 0.13f)`.
- Pemisah dalam kartu memakai `t1` dengan alpha 6%.
- Kartu tanpa border. Gradasi status untuk kartu peringatan: warna status 14% di kiri, memudar ke `s1` di 75%.

## Tipografi

Satu keluarga: **Manrope**. Ketebalan yang dipakai hanya 500, 600, dan 700. Jangan pakai 300/400 tipis atau 800. Angka memakai tabular figures (`fontFeatureSettings = "tnum"`).

| Gaya | Ukuran (sp) | Berat | Letter-spacing | Pakai untuk |
|---|---|---|---|---|
| Display | 40 (36 di dalam cincin) | 700 | −0.035em | angka utama: sisa km, hasil scan |
| H1 | 24 | 700 | −0.025em | judul halaman, nama kendaraan |
| H2 | 17 | 700 | −0.015em | judul bagian, judul top bar |
| Stat | 19 | 700 | −0.02em | angka ringkasan; satuan 12/600 warna `t3` |
| Item | 15 | 600 | −0.01em | nama item di daftar |
| Small | 13.5 | 600 | 0 | nilai di baris info, teks tombol kecil |
| Body | 14 | 500 | 0, line-height 1.55 | paragraf saran |
| Meta | 13 | 500 | 0, line-height 1.4 | keterangan di bawah item (`t2`) |
| Label | 12 | 600 | +0.01em | label ringkasan, judul grup (`t3`) |
| Caption | 12 | 500 | 0 | catatan kecil (`t3`) |
| Button | 15 | 700 | 0 | tombol utama/sekunder |
| Nav | 11 | 600 | 0 | label navigasi bawah |

Aturan:

- Huruf kapital hanya di awal kalimat. Tidak ada label SEMUA KAPITAL.
- Maksimal dua baris untuk nama dan keterangan, lalu `ellipsis`. Nilai angka tidak boleh terbungkus (`maxLines = 1`, `softWrap = false`).
- Satuan ditulis lebih kecil dan redup dari angkanya, misalnya **15.420** km.

## Bentuk & jarak

- Grid 4dp. Padding halaman 20dp. Jarak antar kartu 8–12dp. Jarak antar bagian 28dp (judul bagian ke konten 12dp).
- Radius: kartu kendaraan 28, kartu biasa 20–22, ikon dalam kotak 13–14 (44dp), tombol 18 (tinggi 54), chip penuh (pill), navigasi 24.
- Ikon garis 1.8px (22dp). Ikon komponen glyph padat dari `componentIcon()`.
- Tanpa border pada kartu. Bayangan hanya untuk navigasi melayang.

## Komponen

- **Tombol utama:** gradasi `acc` → `acc2` (135°), teks `ink` 15/700, tinggi 54, radius 18.
- **Tombol sekunder:** latar `s2`, teks `t1` 15/700.
- **Tombol ikon:** 42×42, radius 14, latar `s1`. Titik notifikasi 7dp warna `acc`.
- **Chip filter:** pill, 13/600. Aktif: latar `acc`, teks `ink`. Non-aktif: latar `s2`, teks `t2`. Baris chip bisa digeser horizontal.
- **Tag status:** pill 12/700, teks warna status, latar status 15%. Isinya "Aman", "Segera", "Lewat", atau "Perlu dicek segera".
- **Baris item:** kartu `s1` radius 20, padding 14, isinya:
  - kiri: ikon dalam kotak 44dp berwarna status,
  - kanan atas: nama (Item) dan nilai (14/700) sejajar,
  - di bawahnya: progress 5dp, lalu keterangan (Meta).
- **Kartu info (kunci–nilai):** label Meta warna `t2` di kiri, nilai Small warna `t1` di kanan, baris 12dp, pemisah 6%.
- **Cincin gauge (detail):** 176dp, tebal 12, track `s3`, busur warna status, mulai dari jam 12 searah jarum jam, panjangnya sama dengan **sisa** umur. Di tengah: angka Display 36 dan "km lagi" (Meta).
- **Navigasi bawah:** melayang (inset 16), tinggi 66, radius 24, latar `s1` 88% + blur. Isinya Dasbor · Jurnal · [Scan] · Layanan · Akun. Scan berupa tombol 56dp radius 20, gradasi aksen, naik 30dp. Item aktif berwarna `acc`.
- **Bilah aksi bawah (detail):** tetap di bawah, latar gradasi transparan → `bg`. Berisi tombol sekunder dan tombol utama berdampingan. Konten di atasnya diberi padding bawah supaya tidak tertutup.

## Layar

1. **Dasbor:**
   - sapaan (Meta) + nama kendaraan (H1 + chevron),
   - kartu kendaraan: gambar dengan glow aksen, lalu tiga statistik (Odometer · Kondisi · Servis berikutnya),
   - tiga aksi cepat (Scan odometer · Catat servis · Isi bensin),
   - **Perlu perhatian** (1–3 item paling mendesak, kartu bergradasi status),
   - **Semua item** dengan chip filter dan daftar baris item. Tombol "＋ Tambah" membuka katalog item.
2. **Detail item:** top bar (kembali · "Detail item" · lainnya), cincin gauge dengan glow status, berisi ikon komponen 26dp, angka sisa, dan "km lagi". Di bawah cincin: nama item (H1), kategori · kendaraan (Meta), dan tag status. Lalu dua kartu mini (Jarak dan Waktu: % terpakai, progress, "10.320 / 12.000 km"), kartu info (Estimasi ganti, Servis terakhir, Odometer saat servis, Interval), satu baris saran dengan ikon, Riwayat (maks. 3, "Lihat semua"), bilah aksi (Ubah interval · Catat servis).
3. **Scan odometer:** kamera layar penuh, bingkai pemindai bersudut warna aksen, garis laser, petunjuk "Arahkan kamera ke angka odometer", lalu sheet bawah: "Angka terbaca" + angka Display warna aksen, "Terbaca sama 3 kali · naik 38 km dari catatan terakhir", tombol Ketik manual dan Simpan.
4. **Jurnal:** ringkasan (Jarak bulan ini · Biaya tahun ini · Servis tahun ini), chip (Semua · Servis · Odometer · Bensin), linimasa per bulan (label bulan, baris: ikon, judul, keterangan, nilai di kanan).
5. **Layanan:** kartu darurat bergradasi `bad` ("Butuh bantuan di jalan?"), grid 2×2 "Cari di sekitar" (membuka Google Maps dengan kueri sesuai merek/jenis), daftar Dokumen dengan sisa hari.
6. **Akun:** kartu profil, grup Kendaraan, grup Pengaturan (pengingat, satuan, tema), grup Data (cadangkan, bantuan, hapus semua data dengan warna `bad`), versi.

## Logo

Berkas: `art/logo.svg`, dengan panduan visual di `art/panduan-ui.html`. Bentuknya speedometer dan centang ("kendaraan sudah dicek"):

- latar squircle radius 34/120 bergradasi gelap `#10273A` → `#070D14`, dengan garis tepi aksen 55%,
- track busur `#1D3448`, busur kondisi bergradasi aksen,
- centang putih `#F2F8FC`.

Wordmark: "Cek" warna `t1` diikuti "Kendaraan" dengan teks gradasi aksen, Manrope 700, letter-spacing −0.03em. Ikon launcher dibuat dari SVG ini (adaptive icon: latar gelap + foreground busur dan centang). Ada juga versi satu warna putih untuk notifikasi.

## Gradasi (hanya empat)

| Nama | Definisi | Dipakai di |
|---|---|---|
| Aksen | linear 135°, `acc` → `acc2` | tombol utama, tombol Scan, avatar, logo, wordmark |
| Glow | radial: `acc` 22–32% di tengah → `acc2` 10% → transparan | di belakang gambar kendaraan dan cincin detail (versi status: warna status 14%) |
| Sorot status | linear 100°, warna status 14–18% → `s1` di 75% | kartu "Perlu perhatian", kartu darurat |
| Langit | linear 180°, `bgTop` → `bg` di 300dp | latar setiap halaman |

Busur cincin boleh bergradasi dari versi terang warna status ke warna status (misalnya `#FDE68A` → `warn`). Di luar daftar ini pakai warna solid.

## Tombol & teks tombol

- Isi tombol 1–2 kata kerja, huruf kapital hanya di awal ("Catat servis", "Simpan", "Ketik manual"). Selalu di tengah, satu baris (`maxLines = 1`, tidak terbungkus).
- Ukuran:
  - **Besar:** tinggi 54, teks 15/700, radius 18. Satu per layar sebagai aksi utama, atau berpasangan Sekunder + Utama dengan lebar sama dan jarak 10.
  - **Sedang:** tinggi 44, teks 14/700, radius 14, ikon 18 + jarak 8.
  - **Teks:** 13.5/700 warna `acc`, tanpa latar ("Lihat semua", "Kelola").
  - **Bahaya:** latar `bad` 12%, teks `bad`.
- Ikon di kiri teks, tidak pernah ikon saja kecuali tombol ikon 42dp yang punya `contentDescription`.

## Angka & satuan

- Angka tebal (700) dan satuan lebih kecil, sekitar 42% dari ukuran angka dengan minimum 12sp. Satuan memakai berat 600 warna `t3`, sejajar garis dasar (`Alignment.Bottom` + `alignByBaseline()`), jarak 4dp.
- Dalam satu baris, angka dan satuannya tidak boleh terpisah ke baris berbeda.
- Benar: `15.420 km`, `Rp85.000`, `14 Mar 2027`, `3,2 L`. Hindari: `15420KM`, `Rp 85,000`, `14/03/27`, `3.2 liter`.

## Anatomi kartu item

Lima bagian: ① ikon 44 berwarna status, ② nama 15/600, ③ nilai 15/700 + satuan di kanan, ④ progress 5dp, ⑤ keterangan 13/500.

Ukuran: padding 14, nama → progress 9, progress → keterangan 6. Teks maksimal 2 baris.

## Penulisan

- Kalimat utuh dan spesifik. Contoh: "Ganti dalam 1.680 km atau sebelum Jan 2027", "Estimasi ganti Mei 2027", "Jatuh tempo 14 Mar 2027". Hindari singkatan seperti "perk." atau "est.".
- Format angka: pemisah ribuan titik (`15.420`), desimal koma (`3,2 L`), rupiah tanpa spasi (`Rp85.000`), tanggal `14 Mar 2027`, bulan dan tahun `Mei 2027`.
- Istilah tetap: **Kondisi** (bukan kesehatan/skor), **Servis berikutnya**, **Catat servis**, **Perlu perhatian**, **Estimasi ganti**, **Segera / Aman / Lewat**.
- Hindari istilah Inggris kalau ada padanannya: overdue → lewat, reminder → pengingat, log → catat.
- Fitur yang belum jadi ditulis "Segera hadir", jangan dibuat seolah sudah berfungsi.
