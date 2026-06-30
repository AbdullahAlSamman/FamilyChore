# Auth Feature Module (:feature:auth)

## Purpose
This module handles the onboarding and authentication flow for the FamilyChore application. It includes pairing with a local server via QR code, profile selection, and PIN-based authentication.

## Key Screens
- **WelcomeScreen**: Entry point for setting up or joining a family.
- **QrScannerScreen**: Handles scanning the pairing QR code from a parent device.
- **UserSelectionScreen**: Fetches and displays available profiles in a paired family.
- **PinEntryScreen**: Handles PIN setup and verification.

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

## Testing
- (Planned) Unit tests for ViewModels and Repositories.
