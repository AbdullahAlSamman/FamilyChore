# Android App Module (:app:androidApp)

## Purpose
The platform-specific entry point for the Android application. It handles Android-only lifecycle management and resources.

## Dependencies
- `:app:shared`: Shared business and UI logic.
- **Android Gradle Plugin**: Application management.
- **Compose Multiplatform**: Root UI hosting.

## Consumers
- None (Final binary).

## Testing
- Android-specific UI tests and instrumentation tests.
