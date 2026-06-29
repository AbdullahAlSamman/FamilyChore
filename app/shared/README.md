# Shared App Module (:app:shared)

## Purpose
The `:app:shared` module acts as the "orchestration layer" for all client applications (Android, iOS, Desktop, Web). it contains shared UI components, ViewModels, and state management logic.

## Key Responsibilities
- **Navigation**: Implements the main navigation graph using Type-Safe Compose Navigation.
- **Shared UI**: Contains common screens (e.g., Onboarding, Dashboard) shared across all platforms.
- **Entry Points**: Provides the entry point for platform-specific modules.

## Dependencies
- `:core`: Infrastructure foundation.
- **Compose Multiplatform**: Shared UI.
- **Koin**: DI for ViewModels and repositories.
- **SKIE**: Enhanced Swift interop for iOS targets.

## Consumers
- `:app:androidApp`
- `:app:desktopApp`
- `iosApp` (Xcode project)

## Testing
- Tests are located in `src/commonTest`.
- (Planned) ViewModel unit tests and Compose UI tests.
