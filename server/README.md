# Server Module (:server)

## Purpose
The `:server` module contains the Ktor backend application. It serves as the local family hub, managing data isolation and synchronization between family members.

## Key Features
- **Pairing & Onboarding**: Handles QR-based family setup and device pairing via short-lived tokens.
- **PIN Authentication**: Implements user-specific 4-digit PIN verification.
- **Family Isolation**: Implements multi-tenancy using scoped routing and `familyId` identifiers.
- **Data Sovereignty**: Designed for home-lab deployment (no external data tracking).
- **Serialization**: uses KotlinX Serialization for all JSON responses.

## Dependencies
- `:core`: Uses shared domain models and `Result` wrapper.
- **Ktor Server**: Netty engine, Content Negotiation (JSON), Routing.
- **Logback**: Server logging.

## Consumers
- All client applications (`:app:*`) via REST and WebSockets.

## Testing
- Integration tests are located in `src/test`.
- Uses `ktor-server-test-host` for endpoint verification.
