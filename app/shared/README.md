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

## Multi-Language Support
The app supports English and Arabic with full RTL support. 
- **Localization**: All strings are stored in `:core` and accessed via `stringResource(Res.string.key)`.
- **Locale Management**: `MainViewModel` observes the language preference from `TokenStorage` and updates the `MainState`.
- **RTL Handling**: The `App` composable provides `LocalLayoutDirection` and uses a platform-specific `SetLocale` helper to ensure Compose Multiplatform Resources reload correctly on language change.
- **Arabic Support**: Mandatory Arabic translations are provided in `values-ar/strings.xml`.

## Testing
- Tests are located in `src/commonTest`.
- (Planned) ViewModel unit tests and Compose UI tests.
