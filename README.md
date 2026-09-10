# VBR (Volume Button Replacement)

**VBR** is a modern Android utility application designed for smartphones with broken or malfunctioning physical volume buttons. It provides a dual-mode system allowing users to seamlessly control system audio levels using hardware fingerprint sensor gestures, an edge overlay slider, or Quick Settings tiles.

---

## 🌟 Key Features

* **Hardware Fingerprint Gestures (`FingerprintVolumeService`)**
  * Utilizes `FingerprintGestureController` via an Accessibility Service.
  * Swipe up or down on the power-button fingerprint sensor to step volume up or down without pressing physical keys.

* **Assistive Edge Slider Overlay (`OverlayVolumeService`)**
  * A tactile, translucent vertical slider pinned to the screen edge beside your broken volume keys or power button using `SYSTEM_ALERT_WINDOW`.
  * Drag or swipe vertically on the edge bar to smoothly step volume up/down.
  * Fully customizable: adjustable screen edge (left/right), bar height, thickness, opacity, vertical offset, and haptic feedback tick.

* **Autostart on Boot (`BootReceiver`)**
  * Automatically restarts the edge slider service when the device reboots, powers on, or when the app is updated (`BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`).

* **Screenshot & Screen Recording Exclusion (`FLAG_SECURE`)**
  * Excludes the floating edge slider bar from system screenshots, screen recordings, and recent app previews so it never clutters captured images.

* **Quick Settings Tile (`VolumeTileService`)**
  * Custom Android Quick Settings tile ("Volume Slider") in the notification shade to summon the system volume slider directly with a single tap.

* **Modern Jetpack Compose Material 3 UI**
  * Clean dashboard displaying live service status, permission guides, quick test volume buttons, and customization controls.

---

## 📱 Tech Stack & Technical Requirements

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose + Material 3
* **Min SDK:** 26 (Android 8.0)
* **Target SDK:** 35 (Android 15)
* **Persistence:** Jetpack DataStore (Preferences)
* **Architecture:** Foreground Services, Accessibility Services, BroadcastReceivers, Quick Settings Tile Services.

### Required Permissions
* `android.permission.SYSTEM_ALERT_WINDOW`: Displays the edge overlay slider over other apps.
* `android.permission.BIND_ACCESSIBILITY_SERVICE`: Listens to physical fingerprint swipe gestures.
* `android.permission.RECEIVE_BOOT_COMPLETED`: Automatically resumes overlay on device reboot.
* `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: Runs background overlay service cleanly on Android 14+.
* `android.permission.VIBRATE`: Provides subtle haptic feedback ticks during volume adjustments.

---

## 📂 Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── res/
│   ├── xml/accessibility_service_config.xml
│   └── values/{strings.xml, colors.xml, themes.xml}
└── java/com/archy/vbr/
    ├── MainActivity.kt
    ├── data/
    │   └── PreferencesRepository.kt
    ├── receiver/
    │   └── BootReceiver.kt
    ├── service/
    │   ├── FingerprintVolumeService.kt
    │   ├── OverlayVolumeService.kt
    │   └── VolumeTileService.kt
    ├── ui/
    │   ├── theme/
    │   └── screens/
    │       ├── DashboardScreen.kt
    │       └── OverlaySettingsScreen.kt
    └── util/
        └── VolumeController.kt
```

---

## 🛠️ Building & Running

### Prerequisites
* Android Studio (Ladybug / 2024.2.1+) or Gradle 9.4+
* JDK 11 or higher
* Android Device or Emulator running Android 8.0 (API 26) or higher

### Build Commands
```bash
# Compile debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest
```

---

## 🚀 Setup & Usage Guide

1. **Fingerprint Sensor Listener (Hardware Gestures)**
   - Open VBR and tap **Enable Accessibility Service**.
   - Enable **VBR** under system Accessibility Settings.
   - Swipe up/down on your phone's fingerprint sensor to adjust volume. *(Note: Requires device driver support for `isGestureDetectionAvailable`)*.

2. **Assistive Edge Slider**
   - Toggle **Active on screen** on the main dashboard.
   - Grant the **Display over other apps** permission when prompted.
   - Tap **Customize Overlay Bar Settings** to adjust edge side (left/right), bar height, opacity, thickness, and screenshot exclusion (`FLAG_SECURE`).

3. **Notification Shade Shortcut**
   - Swipe down twice from the top of your screen to expand Quick Settings.
   - Tap the edit/pencil icon and drag **Volume Slider** into your active tiles.

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
