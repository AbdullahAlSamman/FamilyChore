# FamilyChore Data Layer Documentation

This document describes the data layer architecture and persistence mechanisms used in the FamilyChore application.

## Persistence Strategy

The application uses two primary persistence mechanisms:
1. **Room Database:** For structured, relational data (Chores, Users).
2. **Jetpack DataStore:** For lightweight key-value pairs (Auth Tokens, Server Settings).

## Session Persistence (DataStore)

Critical session data is managed via the `TokenStorage` interface. The implementation, `DataStoreTokenStorage`, uses Jetpack DataStore Preferences to ensure data survives app restarts.

### Data Stored:
- **Auth Token:** JWT token for API authentication.
- **Family ID:** The ID of the currently joined family.
- **Server URL:** The base URL of the Ktor server (discovered or manually entered).
- **Server Name:** The friendly name of the discovered server.

### Platform Support:
DataStore is configured to use platform-specific storage locations:
- **Android:** Internal files directory.
- **iOS:** Documents directory.
- **Desktop:** User home directory (`.familychore/` folder).

## Room Database

The `FamilyDatabase` handles relational data. It is an offline-first source of truth for:
- **Users:** nicknames, roles, and PIN hashes.
- **Chores:** titles, status, and assignments.

## Architecture Guidelines

- **Interfaces in Domain:** All data sources and repositories must have interfaces defined in the `:core:domain` module.
- **Implementations in Data:** Concrete implementations (Room, DataStore, Ktor) live in the `:core:data` module.
- **DTOs vs Domain Models:** Always map network DTOs and Room Entities to Domain Models before exposing them to the domain/presentation layers.
- **Reactive Updates:** Prefer exposing `Flow<T>` from repositories for real-time UI updates.

## Testing

Persistence components are unit-tested using:
- **FakeFileSystem (Okio):** For hermetic DataStore testing.
- **InMemory Room:** For database unit tests.
- **MockEngine (Ktor):** For network data source tests.
