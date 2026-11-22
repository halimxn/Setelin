<div align="center">

  <img src="art/logo.svg" alt="Setelin Logo" width="120" height="120" />

  # Setelin

  **Precision maintenance telemetry and dual-axis interval tracking for motorcycles and cars.**

  *Tune your intervals. Maintain your machine without guesswork.*

  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://android.com)
  [![Language](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Design Theme](https://img.shields.io/badge/Design-Neon%20Cockpit-22D3EE?style=flat-square)](docs/DESIGN.md)
  [![Architecture](https://img.shields.io/badge/Database-Room%20(Offline%20First)-34D399?style=flat-square)](#offline-first-architecture)
  [![Binary Footprint](https://img.shields.io/badge/Release%20APK-~1.9%20MB-blueviolet?style=flat-square)](#performance-and-footprint)

</div>

---

## Overview

Most vehicle maintenance systems fail for a simple reason: they rely on static estimates or human memory. Physical service booklets get misplaced in glove compartments, paper oil-change stickers fade in the sun, and generic reminder apps treat mechanical wear as a simple countdown of days.

Mechanical components degrade across two independent dimensions:
1. **Mechanical friction and displacement**, measured in kilometers traveled.
2. **Chemical breakdown, environmental exposure, and regulatory expiry**, measured in calendar time.

A scooter driven 3,000 km in four weeks requires an oil change just as urgently as a scooter parked in a garage for six months whose oil has oxidized. 

**Setelin** bridges this gap. Rooted in the Indonesian automotive vernacular *"setel"* (to calibrate, fine-tune, or synchronize), Setelin provides cockpit-grade telemetry and dual-axis predictive maintenance tracking for everyday commuters and automotive enthusiasts across Indonesia.

---

## Core Capabilities

### Dual-Axis Predictive Lifespan Algorithm
Every tracked component, preventative treatment, and legal document is evaluated against distance and time constraints simultaneously. The system determines status based on whichever threshold is reached first:
* **Optimal (Aman)**: Operating safely within established tolerance limits.
* **Due Soon (Segera)**: Reaching 80 percent or more of component lifespan in distance or time.
* **Overdue (Lewat)**: Exceeding 100 percent of scheduled interval threshold.

### Instant On-Device Odometer Extraction
Instead of tedious manual entry, Setelin integrates on-device optical character recognition using Google ML Kit. Point the camera at digital or analog odometer clusters to extract mileage in real time. Frame-stabilization heuristics require consecutive matching readings before confirmation, preventing misreadings. No images ever leave the device.

### Fleet Presets Tailored to the Indonesian Market
Out of the box, Setelin includes specialized maintenance templates calibrated for vehicles commonly driven across Southeast Asia:
* **Automatic Scooters (CVT)**: Engine oil, transmission/gear oil, CVT drive belts, sliding rollers, clutch shoes, and air filters.
* **Manual and Sport Motorcycles**: Chain lubrication and slack, spark plugs, valve clearance, brake fluid, and coolant.
* **Passenger Cars and Commercial MPVs**: Engine oil, oil filters, brake pads, tire rotations, cabin filters, and spark plugs.
* **Electric Vehicles**: Reduction gear oil, auxiliary 12V battery, brake fluid, and high-voltage cooling system inspections.
* **Legal Documentation**: Annual vehicle tax (Pajak STNK Tahunan), 5-year plate renewal (Pajak 5 Tahunan), and driving licenses (SIM).

### Zero-Telemetry, Offline-First Architecture
Setelin contains no user analytics, no advertising SDKs, no cloud tracking, and no account requirements. The persistence layer operates strictly on an on-device SQLite database via Android Room. Your driving records, financial expenses, and maintenance history belong entirely to you.

### Low-Overhead Engineering
By avoiding heavy third-party dependencies, runtime 3D game engines, and unnecessary background polling, Setelin compiles to an optimized release binary of under 2 MB with sub-3% frame drop rates on 120Hz displays.

---

## Design System: Neon Cockpit

The visual language of Setelin is inspired by aviation glass cockpits and digital automotive instrument clusters. It is tailored for high contrast and zero eye fatigue during low-light conditions (such as late-night garage maintenance or evening commutes).

### Color Hierarchy

| Token | Hex Value | Semantic Application |
|---|---|---|
| `bg` | `#060A10` | Deep cockpit void; canvas background |
| `bgTop` | `#0E2332` | Subtle 300dp vertical atmospheric light falloff |
| `s1` | `#0C141D` | Instrument card surfaces and structural containers |
| `s2` | `#111C28` | Secondary interactive containers, inactive chips, icon buttons |
| `s3` | `#1A2A3B` | Passive gauge tracks and progress bar backgrounds |
| `t1` | `#F2F8FC` | Primary high-legibility telemetry text and headers |
| `t2` | `#9AB0C2` | Supporting technical meta descriptions |
| `t3` | `#5D7385` | Captions, units, and tertiary labels |
| `acc` | `#22D3EE` | Primary luminous cyan glow; active states and accents |
| `acc2` | `#7C8CFF` | Electric indigo; accent gradient termination |
| `ink` | `#031820` | High-contrast glyphs and text rendered over cyan accents |
| `ok` | `#34D399` | Optimal status indicator |
| `warn` | `#FBBF24` | Inspection required soon indicator |
| `bad` | `#F87171` | Critical inspection or overdue indicator |
| `div` | `0x0FF2F8FC` | Subtle 6% opacity divider inside cards |

### Typographic Discipline
All typography is rendered using the variable **Manrope** font family. To maintain visual hierarchy without visual clutter, weights are strictly constrained to three values:
* **Medium (500)**: Informational prose, explanatory notes, and micro-captions.
* **SemiBold (600)**: Navigation items, table keys, and list item names.
* **Bold (700)**: High-priority telemetry readouts, numeric metrics, and primary action buttons.
* **Tabular Figures (`tnum`)**: All numeric readouts enforce monospaced figure widths to prevent layout shifting during telemetry animations.

For the complete technical specification, refer to [docs/DESIGN.md](docs/DESIGN.md) and the interactive visual guide at [art/panduan-ui.html](art/panduan-ui.html).

---

## Technical Stack & Architecture

```
id.cekendaraan.app
├── data/
│   ├── Db.kt           # Room Database, Entities (Vehicle, Component, ServiceEvent), DAOs & Migrations
│   ├── Catalog.kt      # Vehicle taxonomy, makes, and models
│   ├── Seed.kt         # Component templates, intervals, and presets
│   ├── Prefs.kt        # Local configuration and persistence
│   └── Reminder.kt     # WorkManager daily maintenance notifications
├── domain/
│   └── Due.kt          # Dual-axis health calculation and limit algorithms
└── ui/
    ├── theme/          # Neon Cockpit color tokens, gradients, and typography
    ├── CekKendaraanApp # Root navigation controller and modal orchestrator
    ├── HomeScreen      # Dashboard, instrument gauges, and quick actions
    ├── ComponentScreen # Component detail telemetry and interval configuration
    └── Components.kt   # Atomic design components (Buttons, GaugeRing, ItemRow, etc.)
```

* **Target SDK**: Android 15 (API level 35)
* **Minimum SDK**: Android 8.0 (API level 26)
* **UI Layer**: Jetpack Compose (BOM 2024.12.01) with Material 3
* **Local Persistence**: Android Room 2.6.1 with Kotlin Symbol Processing (KSP)
* **Background Tasks**: AndroidX WorkManager 2.9.1
* **Language Runtime**: Kotlin 2.0.21 on JVM 17
* **Code Shrinking & Obfuscation**: ProGuard / R8 with Resource Shrinking enabled

---

## Building from Source

### Prerequisites
* Java Development Kit (JDK) 17
* Android SDK (API 35 and Build Tools installed)
* Git

### Step-by-Step Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/halimxn/Setelin.git
   cd Setelin
   ```

2. **Configure SDK Location:**
   Create a `local.properties` file in the project root:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```

3. **Compile Release Binary:**
   ```powershell
   # Windows PowerShell
   $env:JAVA_HOME = "D:\AndroidDev\jdk17"
   ./gradlew assembleRelease
   ```

4. **Deploy to Target Device:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

---

## Contributing

Contributions, defect reports, and architecture enhancements are welcome.
1. Fork the repository.
2. Create your dedicated topic branch:
   ```bash
   git checkout -b feat/custom-export-module
   ```
3. Commit changes conforming to the Conventional Commits specification:
   ```bash
   git commit -m "feat(export): add local encrypted json backup routine"
   ```
4. Push your branch to GitHub:
   ```bash
   git push origin feat/custom-export-module
   ```
5. Open a Pull Request for review.

---

## License

This project is open-source under standard permissive terms. The bundled Manrope typeface is licensed under the SIL Open Font License (OFL) — see [FONT-LICENSE.txt](FONT-LICENSE.txt).
