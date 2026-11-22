<div align="center">

  <img src="art/logo.svg" alt="Cek Kendaraan Logo" width="128" height="128" />

  # Cek Kendaraan
  
  **The ultimate offline-first, precision maintenance tracker for motorcycles and cars.**
  
  *Keep your ride road-worthy without second-guessing.*

  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
  [![Language](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Design Theme](https://img.shields.io/badge/Theme-Neon%20Cockpit-22D3EE?style=for-the-badge)](docs/DESIGN.md)
  [![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success?style=for-the-badge)](#privacy--offline-first)
  [![APK Size](https://img.shields.io/badge/APK%20Size-~1.9%20MB-blueviolet?style=for-the-badge)](#performance--architecture)

</div>

---

## 🌟 Overview

**Cek Kendaraan** is an ultra-lightweight, privacy-respecting Android application tailored specifically for vehicle owners in Indonesia. Whether you ride a daily commuter scooter (Honda Vario, Yamaha NMAX, Beat), an enthusiast sportbike, an electric motorcycle (Alva, Gesits), or drive a family MPV (Avanza, Xpander), Cek Kendaraan tracks every critical component, scheduled service, and regulatory document with cockpit-grade precision.

No accounts to create. No ad tracking. No network dependencies. Every byte lives on your device.

---

## ✨ Key Features

- **⚡ Neon Cockpit Design System**: Inspired by dark, high-contrast digital instrument clusters. Crafted on a pure Manrope variable typographic scale with focused cyan accents, subtle surface gradients, and zero distracting borders.
- **⏱️ Dual-Limit Life Calculation**: Intelligently predicts remaining life by evaluating both distance (km) and calendar duration (months). Whichever limit expires first triggers timely status alerts: **Aman (Good)**, **Segera (Due Soon)**, or **Lewat (Overdue)**.
- **🛵 Indonesian Fleet Catalog**: Rich built-in templates tailored to local market specifications—from CVT drive belts and gear oil for automatic scooters to spark plugs, coolant flushes, valve inspections, and STNK/SIM tax renewals.
- **📷 Rapid On-Device Odometer Scanning**: Point your camera at your dashboard odometer and let the on-device ML model extract readings in seconds. 100% processed locally on your phone.
- **🛡️ 100% Offline & Private**: Built on Room database with strict local persistence. Your mileage, expenses, and vehicle documents never touch third-party servers.
- **🪶 Featherweight Performance**: Optimized with R8 shrinking, vector drawables, and native Jetpack Compose. Tiny footprint (< 2 MB release APK) that delivers fluid 60/120 fps animations.

---

## 🎨 Design Philosophy: "Neon Cockpit"

Cek Kendaraan embraces a purpose-built aesthetic designed for high legibility in low-light environments, such as inside garages or at night.

| Token | Hex | Role |
|---|---|---|
| `bg` | `#060A10` | Deep cockpit background |
| `bgTop` | `#0E2332` | Subtle top atmospheric gradient |
| `s1` | `#0C141D` | Instrument card surface |
| `s2` | `#111C28` | Secondary interactive containers |
| `acc` | `#22D3EE` | Primary luminous cyan glow |
| `acc2` | `#7C8CFF` | Vibrant indigo gradient termination |
| `ok` | `#34D399` | Optimal status |
| `warn` | `#FBBF24` | Attention required soon |
| `bad` | `#F87171` | Overdue / critical inspection |

Explore the full design tokens, visual hierarchy, and component rules in [docs/DESIGN.md](docs/DESIGN.md) and [art/panduan-ui.html](art/panduan-ui.html).

---

## 🏗️ Architecture & Tech Stack

```
id.cekendaraan.app
├── data/
│   ├── Db.kt           # Room Database, Entities (Vehicle, Component, ServiceEvent), DAOs & Migrations
│   ├── Catalog.kt      # Popular vehicle models & makes (Indonesia)
│   ├── Seed.kt         # Component templates, intervals & presets
│   ├── Prefs.kt        # Local preference stores
│   └── Reminder.kt     # WorkManager daily maintenance reminders
├── domain/
│   └── Due.kt          # Dual-axis health & remaining lifespan algorithm
└── ui/
    ├── theme/          # Neon Cockpit color tokens & Manrope typography
    ├── CekKendaraanApp # State-based root navigation & modal coordinator
    ├── HomeScreen      # Dashboard & vehicle condition gauge
    ├── ComponentScreen # Detail view & maintenance telemetry
    └── Components.kt   # Reusable atomic design components
```

- **UI Framework**: Jetpack Compose (BOM 2024.12.01) with Material 3 integration.
- **Typography**: Variable Manrope (OFL licensed) with tabular numerals (`tnum`).
- **Database**: SQLite via Android Room 2.6.1 + KSP.
- **Background Tasks**: AndroidX WorkManager 2.9.1.
- **Target SDK**: Android 15 (API 35) | **Minimum SDK**: Android 8.0 (API 26).

---

## 🚀 Building From Source

### Prerequisites
- JDK 17 (e.g. OpenJDK 17)
- Android SDK with Platform 35 and Build Tools installed
- Git

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/username/cek-kendaraan.git
   cd cek-kendaraan
   ```

2. **Configure your Android SDK path:**
   Create a `local.properties` file in the root directory:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```

3. **Build the Release APK:**
   ```powershell
   # On Windows (PowerShell)
   $env:JAVA_HOME = "D:\AndroidDev\jdk17"
   ./gradlew assembleRelease
   ```

4. **Install on connected device:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

---

## 🤝 Contributing

Contributions, bug reports, and suggestions are welcome!
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feat/amazing-feature`)
3. Commit your Changes using Conventional Commits (`git commit -m 'feat: add fuel logging sheet'`)
4. Push to the Branch (`git push origin feat/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open-source. Manrope font is licensed under the SIL Open Font License (OFL) — see [FONT-LICENSE.txt](FONT-LICENSE.txt).
