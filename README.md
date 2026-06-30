# FamilyChore

A Kotlin Multiplatform project for managing family chores, targeting Android, iOS, Desktop (JVM), and a local Server.

## Project Structure

* **[:app:androidApp](./app/androidApp)**: Android-specific entry point and configuration.
* **[:app:desktopApp](./app/desktopApp)**: Desktop (JVM) entry point using Compose for Desktop.
* **[:app:shared](./app/shared)**: Shared UI and navigation orchestration for all client platforms.
* **[:core](./core)**: Shared domain models, infrastructure, and common utilities used by both client and server.
* **[:feature:auth](./feature/auth)**: Authentication and onboarding feature module (QR pairing, PIN entry).
* **[:server](./server)**: Ktor-based backend serving as the local family hub.
* **[:build-logic](./build-logic)**: Gradle Convention Plugins for centralized build configuration.

### Shared Code Strategy

The project maximizes code sharing across all targets:
- **Business Logic**: Common in `:core` and feature modules.
- **UI**: Shared using Compose Multiplatform in `:app:shared` and features.
- **Platform Specifics**: Handled via `expect`/`actual` (e.g., QR scanning in `:feature:auth`).

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :app:androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
  - Standard run: `./gradlew :app:desktopApp:run`
- Server: `./gradlew :server:run`
- iOS app: open the [/app/iosApp](./app/iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :app:shared:testAndroidHostTest`
- Desktop tests: `./gradlew :app:shared:jvmTest`
- Server tests: `./gradlew :server:test`
- iOS tests: `./gradlew :app:shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…