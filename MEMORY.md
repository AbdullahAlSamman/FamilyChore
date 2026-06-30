# FamilyChore Project Memory

## General Rules
- **After finishing up with implementation and testing always update documentation files `README.md`**
- **before reading files of any module, read documentation first `README.md` file then if necessary read files**
- Tests must be related to implementation don't placeholder tests.
- No placeholders UI components in any screen, implement real feature with permission requesting in mind e.g camera for QR code scans, if clarification is required then stop implementation and ask
- Don't add any file as placeholder unless you add to `TODO` inside it explain why! should be also document it `README.md` so you can pick it up from there next time.
- Always keep the architecture as agreed on based on the skills available to you.
- Each transaction with server side should be logged to logs to followup on issues.

This file tracks critical architectural decisions and domain rules for the FamilyChore project.
## Critical Engineering Decisions
- **Role-Based UX**: Unified binary, branched UI (Parent vs. Child dashboards).
- **Offline-First**: Room KMP is the Single Source of Truth.
- **Data Sovereignty**: Local Ktor server (Home Lab); no external analytics.
- **Multi-Tenancy**: Every entity and request is scoped via `familyId`.
- **Navigation**: Type-Safe Compose Navigation (@Serializable routes).
- **Build Infra**: Gradle Convention Plugins in `:build-logic`.
- **Module Documentation**: Every module MUST have a `README.md` explaining its purpose, dependencies, consumers, and tests. Read this FIRST.
- **Strict MVI & UI Split**: Every screen MUST be split into a **Root** (logical/DI) and **Screen** (dumb UI) composable. ViewModels MUST follow the `State`, `Action`, `Event` pattern.
- **Test-Driven Execution**: Every implementation phase MUST conclude with comprehensive unit tests before proceeding to the next phase. Foundation testing (Result, SafeCall mapping, UiText) is complete.
- **Server Discovery**: Onboarding includes an automated search for the local Ktor server (mDNS/Network scanning). Once found, the IP is cached in `TokenStorage` and reused for all subsequent requests.
- **Pairing Flow**: Onboarding uses a real-time QR handshake (CameraX/ML Kit). QR content is a JSON `PairingToken` (ip, token). Joining a family involves scanning, fetching users via token, and picking a profile.
- **PIN Authentication**: 4-digit PIN is verified against the server. During onboarding, the parent sets a PIN (setup mode), and subsequent joins or re-auths use verification mode.

## Domain Rules
- **Chore Verification**: Mandatory live photo (no gallery uploads).
- **Points**: Points are never deducted without parent approval (default).
- **Authentication**: Optional 4-digit PIN for child profiles.
- **Secure Communication**: [FUTURE] Migrate from cleartext HTTP to HTTPS for all server communications (currently using `usesCleartextTraffic` for development).
