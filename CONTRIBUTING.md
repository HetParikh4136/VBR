# Contributing to VBR

Thank you for considering contributing to **VBR**! Contributions from the community help make VBR a better utility for everyone with broken physical buttons.

---

## 🚀 How Can I Contribute?

### Reporting Bugs
Before opening a new issue, please check existing issues to ensure the bug hasn't already been reported. When submitting a bug report, please include:
* Device model and Android OS version.
* Clear steps to reproduce the issue.
* Relevant logcat excerpts if available.

### Suggesting Features
Enhancement suggestions are always welcome! Please open an issue detailing:
* The feature or improvement you'd like to see.
* Why this feature would be beneficial for users.

### Submitting Pull Requests

1. **Fork the repository** and create a feature branch off `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. **Make your changes** following Kotlin coding conventions and existing project architecture.
3. **Verify the build and run unit tests**:
   ```bash
   ./gradlew :app:assembleDebug :app:testDebugUnitTest
   ```
4. **Commit your changes** with a concise, descriptive commit message.
5. **Push to your fork** and open a Pull Request against the `main` branch.

---

## 🛠️ Code Style & Conventions

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose + Material 3
* **Code Formatting:** Follow official Kotlin coding style (`kotlin.code.style=official`).
* **Architecture:** Keep services decoupled (`FingerprintVolumeService`, `OverlayVolumeService`, `VolumeTileService`) and use `PreferencesRepository` for persistent settings.

Thank you for helping improve VBR!
