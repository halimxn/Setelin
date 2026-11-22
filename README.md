<div align="center">

  <img src="art/logo.svg" alt="Setelin Logo" width="132" height="132" />

  # Setelin
  
  **The ultimate offline-first, precision maintenance & interval tracker for motorcycles and cars.**
  
  *Setel interval, rawat kendaraan tanpa menebak.*

  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
  [![Language](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Design Theme](https://img.shields.io/badge/Theme-Neon%20Cockpit-22D3EE?style=for-the-badge)](docs/DESIGN.md)
  [![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success?style=for-the-badge)](#privacy--offline-first)
  [![APK Size](https://img.shields.io/badge/APK%20Size-~1.9%20MB-blueviolet?style=for-the-badge)](#performance--architecture)

</div>

---

## 🌟 What is Setelin?

**Setelin** (derived from the Indonesian automotive slang *"setel"* — meaning to fine-tune, tune up, and calibrate) is an ultra-lightweight, privacy-respecting Android application tailored specifically for vehicle owners in Indonesia.

Whether you commute daily on a scooter (**Honda Vario, Beat, Scoopy, Yamaha NMAX, Aerox**), ride an enthusiast sportbike, pilot an electric commuter (**Alva, Gesits, Wuling Air EV**), or drive a family car (**Avanza, Xpander, Brio, Sigra**), Setelin gives you cockpit-grade control over your vehicle's health.

It tracks oil changes, brake pads, CVT drive belts, spark plugs, filters, and regulatory paperwork (STNK & SIM renewals) using dual-axis predictions: **distance (km)** and **time (calendar months)**. Whichever runs out first determines your vehicle's status.

> **Zero accounts. Zero tracking. Zero ads.** All data stays strictly on your device.

---

## ✨ Key Highlights

- **⚡ Neon Cockpit Design System**: Deep black panels (`#060A10`), luminous cyan instruments (`#22D3EE`), and high-contrast status feedback. Designed strictly around the variable **Manrope** typographic scale (weights 500, 600, 700) with tabular numerals (`tnum`).
- **⏱️ Dual-Limit Predictive Health**: Accurately computes remaining lifespan based on both kilometer intervals and monthly calendar limits. Status tags clearly announce **Aman (Good)**, **Segera (Due Soon)**, or **Lewat (Overdue)**.
- **🛵 Built-in Indonesian Fleet Presets**: Pre-configured service templates for popular Indonesian motorcycles and cars, including engine oil, gear/transmission oil, CVT roller/belt checks, radiator coolant, valve adjustments, and vehicle taxes.
- **📷 Instant On-Device Odometer Scanner**: Point your camera at your digital or analog odometer to capture readings in seconds. Powered by Google ML Kit on-device text recognition with zero network calls.
- **🛡️ 100% Offline & Private**: Backed by an encrypted local Room SQLite database. Your vehicle data, expenses, and journey records never leave your phone.
- **🪶 Featherweight Footprint**: Optimized with R8 shrinking and vector assets. Yields a compact release APK (~1.9 MB) with fluid 60/120 fps animations.

---

## 🎨 Design Philosophy: "Neon Cockpit"

Setelin embraces a purpose-built aesthetic designed for high legibility in low-light environments, such as inside garages or at night.

| Token | Hex | Role |
|---|---|---|
| `bg` | `#060A10` | Deep cockpit background |
| `bgTop` | `#0E2332` | Top atmospheric gradient |
| `s1` | `#0C141D` | Instrument card surface |
| `s2` | `#111C28` | Secondary interactive containers & buttons |
| `s3` | `#1A2A3B` | Gauge tracks & progress bars |
| `t1` | `#F2F8FC` | Primary readable text |
| `t2` | `#9AB0C2` | Secondary meta text & descriptions |
| `acc` | `#22D3EE` | Primary luminous cyan glow |
| `acc2` | `#7C8CFF` | Electric indigo gradient termination |
| `ok` | `#34D399` | Optimal status (Aman) |
| `warn` | `#FBBF24` | Inspection required soon (Segera) |
| `bad` | `#F87171` | Critical / overdue maintenance (Lewat) |

Explore the design guidelines in [docs/DESIGN.md](docs/DESIGN.md) and the interactive visual guide at [art/panduan-ui.html](art/panduan-ui.html).

---

## 🏗️ Architecture & Structure

```
id.cekendaraan.app
├── data/
│   ├── Db.kt           # Room Database, Entities (Vehicle, Component, ServiceEvent), DAOs & Migrations
│   ├── Catalog.kt      # Popular vehicle models & makes (Indonesia)
│   ├── Seed.kt         # Component templates, intervals & presets
│   ├── Prefs.kt        # SharedPreferences local configuration
│   └── Reminder.kt     # WorkManager daily morning notifications
├── domain/
│   └── Due.kt          # Dual-axis health & remaining lifespan algorithm
└── ui/
    ├── theme/          # Neon Cockpit color tokens, gradients & Manrope typography
    ├── CekKendaraanApp # State-based root navigation & modal coordinator
    ├── HomeScreen      # Dashboard, instrument gauges & quick actions
    ├── ComponentScreen # Component detail telemetry & interval editor
    └── Components.kt   # Reusable atomic design components (Buttons, GaugeRing, ItemRow, etc.)
```

- **UI Framework**: Jetpack Compose (BOM 2024.12.01) with Material 3.
- **Typography**: Variable Manrope (OFL licensed) with tabular numerals (`tnum`).
- **Database**: SQLite via Android Room 2.6.1 + KSP.
- **Target SDK**: Android 15 (API 35) | **Minimum SDK**: Android 8.0 (API 26).

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 (e.g. OpenJDK 17)
- Android SDK with Platform 35 and Build Tools installed
- Git

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/username/setelin.git
   cd setelin
   ```

2. **Configure your Android SDK path:**
   Create a `local.properties` file in the root directory:
   ```properties
   sdk.dir=D:\\AndroidDev\\sdk
   ```

3. **Build the Release APK:**
   ```powershell
   # On Windows (PowerShell)
   $env:JAVA_HOME = "D:\AndroidDev\jdk17"
   ./gradlew assembleRelease
   ```

4. **Install on connected device via ADB:**
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

This project is open-source. The Manrope font family is licensed under the SIL Open Font License (OFL) — see [FONT-LICENSE.txt](FONT-LICENSE.txt).
