# Auth Feature Module (:feature:auth)

## Purpose
This module handles the onboarding and authentication flow for the FamilyChore application. It includes pairing with a local server via QR code, profile selection, and PIN-based authentication.

## Key Screens
- **WelcomeScreen**: Entry point for setting up or joining a family.
- **CreateFamilyScreen**: Allows parents to initialize a new family hub and parent profile.
- **QrScannerScreen**: Handles scanning the pairing QR code from a parent device.
- **UserSelectionScreen**: Fetches and displays available profiles in a paired family.
- **PinEntryScreen**: Handles PIN setup (for new users) and verification (for existing users).

## Architecture
Follows the project's strict MVI pattern:
- **Root/Screen Split**: Logic (DI, Events) in Root, UI (State, Actions) in Screen.
- **ViewModel**: Uses `State`, `Action`, and `Event` pattern.
- **Navigation**: Uses Type-Safe Compose Navigation with local routes.

## Dependencies
- `:core`: Shared infrastructure and domain models.
- **Koin**: Dependency injection.
- **Navigation Compose**: Shared navigation logic.
- **Lifecycle Compose**: State observation.
- **CameraX & ML Kit (Android)**: Real-time QR code scanning infrastructure.

## Implementation Details
- **QrScannerView**: A platform-specific Composable implemented via `expect`/`actual`.
    - **Android**: Uses CameraX for preview and Google ML Kit Barcode Scanning for detection.
    - **JVM/iOS**: Current placeholder implementation.
- **Permission Handling**: `RequestCameraPermission` (expect/actual) handles the camera permission request flow, updating the MVI `QrScannerState`.

## Testing
- **ViewModel Unit Tests**: Comprehensive coverage for all onboarding flows, including QR parsing, permission state updates, and navigation events, using JUnit5, Turbine, and AssertK.
