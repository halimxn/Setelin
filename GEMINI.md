# GEMINI.md

Instruksi proyek ini ada di [AGENTS.md](AGENTS.md). Baca file itu dulu, lalu [docs/DESIGN.md](docs/DESIGN.md) dan [docs/ROADMAP.md](docs/ROADMAP.md).

Ringkasnya:

- Aplikasi Android Kotlin + Jetpack Compose, UI berbahasa Indonesia, tema **Neon Cockpit**.
- Ikuti token dan skala huruf di DESIGN.md (font Manrope, berat 500/600/700). Jangan menulis warna hex di layar.
- Perubahan database wajib disertai migrasi tanpa menghapus data.
- Tetap ringan: tanpa 3D runtime, dan cek kelancaran memakai build release.
- Foto di `art/image/` hanya referensi dan tidak boleh masuk ke `res/`.
- Build: `$env:JAVA_HOME="D:\AndroidDev\jdk17"; ./gradlew assembleRelease`, lalu cek di HP sebelum menyatakan selesai.
