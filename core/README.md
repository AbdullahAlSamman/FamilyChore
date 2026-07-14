# Core Module (:core)

## Purpose
The `:core` module provides shared infrastructure, domain models, and utilities used by both the server and all client applications. It is the central foundation of the FamilyChore project.

## Key Components
- **Domain Utilities**: `Result` wrapper for idiomatic error handling and `DataError` definitions.
- **Presentation Utilities**: `UiText` for multiplatform-safe string resource handling and `ValidationMappers` for error localization.
- **Localization**: Single Source of Truth (SSOT) for all application strings via `composeResources`. Supports English and Arabic (RTL).
- **Data Utilities**: `HttpClientFactory` for Ktor client configuration and `SafeCall` helpers for consistent network error mapping.
- **Local Persistence**: `FamilyDatabase` (Room KMP) definition and entities (`UserEntity`, `ChoreEntity`, `TransactionEntity`) with multi-tenant scoping.
- **Domain Models**: Core models like `User`, `Family`, `Transaction`, `Reward`, and `PairingToken` used across the project.

## Dependencies
- **Kotlin Multiplatform**: Core platform.
- **Room KMP**: Local database with KSP code generation.
- **Ktor Client**: Core networking.
- **Compose Multiplatform**: For `UiText` resource resolution.
- **Koin**: Dependency injection core.
- **KotlinX Serialization**: JSON serialization.

## Consumers
- `:server`: Uses domain models, Result wrapper, and shared data entities.
- `:app:shared`: Main consumer for UI logic and local database access.
- `:feature:auth`: Uses domain models and presentation utilities for onboarding.
- All future feature modules.

## Testing
- Unit tests are located in `src/commonTest`.
- `ResultTest`: Tests functional operators.
- `SafeCallTest`: Tests Ktor response mapping using `MockEngine`.
- `UiTextTest`: Basic validation of string wrappers.
