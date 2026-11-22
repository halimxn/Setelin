---
trigger: always_on
description: Aturan proyek Cek Kendaraan untuk agen Antigravity
---

# Cek Kendaraan — aturan workspace

Sebelum mengerjakan apa pun, baca `AGENTS.md` di root proyek, lalu `docs/DESIGN.md` (sistem desain Neon Cockpit) dan `docs/ROADMAP.md` (urutan tugas dan kriteria selesai).

Aturan wajib:

1. UI berbahasa Indonesia dengan format angka Indonesia (`15.420 km`, `Rp85.000`, `14 Mar 2027`). Status ditulis dengan kata, bukan hanya warna.
2. Pakai token warna dan text style dari `ui/theme/Theme.kt`. Font Manrope dengan berat 500/600/700 saja. Jangan menulis `Color(0x…)` di file layar.
3. Setiap perubahan entity Room wajib menaikkan versi database dan menambah `Migration`. Data pengguna tidak boleh hilang.
4. Jangan menambah library berat atau 3D runtime. APK release harus tetap kecil (±2 MB).
5. Foto di `art/image/` hanya referensi dan tidak boleh disalin ke `app/src/main/res`.
6. Jangan menyalin nama, logo, teks, atau susunan menu aplikasi lain (termasuk RideCheck).
7. Fitur yang belum jadi ditandai "Segera hadir". Jangan menampilkan data palsu.
8. Verifikasi dengan build release di HP atau emulator. Cek teks terpotong dan kelancaran scroll sebelum menyatakan selesai.
