# FamilyChore Project Memory

## General Rules
- **After finishing up with implementation and testing always update documentation files `README.md`**
- **before reading files of any module, read documentation first `README.md` file then if necessary read files**
- Tests must be related to implementation don't placeholder tests.
- No placeholders UI components in any screen, implement real feature with permission requesting in mind e.g camera for QR code scans, if clarification is required then stop implementation and ask
- Don't add any file as placeholder unless you add to `TODO` inside it explain why! should be also document it `README.md` so you can pick it up from there next time.
- Commit message starts with T* number of the ticket mentioned in branch name.
- Don't commit until you have been asked to.

## Data Entry & Validation
- **Domain-Level SSOT**: All validation logic MUST reside in the `core:domain` layer (e.g., `ChoreValidator`).
- **Standardized Error Types**: Validators MUST return domain-specific error types inheriting from `core.domain.util.Error`.
- **Presentation Mapping**: Domain errors MUST be mapped to `UiText` in the presentation layer via `ValidationMappers.kt`.
- **Zero Plain-Text Persistence**: Sensitive data (like PINs) MUST NEVER be saved to any database (local or server) in plain text. Hashing (SHA-256) MUST occur at the Repository level before any storage or network transmission.
- **UI Pattern**: Screens MUST support real-time feedback (on field change) and final "On-Submit" validation.
- **Server Parity**: Server-side routes MUST implement mirror validation logic. If validation fails, the server MUST respond with `HttpStatusCode.BadRequest` (400) and a structured `ErrorResponse` detailing the validation errors.
- **Client-Side Handling**: The client `SafeCall` mechanism MUST be able to parse server-side validation errors and map them to `DataError.Network.VALIDATION_ERROR`.

## Implementation rules
- Features should be always implemented by the skill set available to you, with complete layers data, domain, ui.
- Always localize ui layer with English and Arabic following localization skill.
- **Any field or data entry should be validated before submission.**
- Use sealed interfaces/classes as state no data class.
- Each transaction with server side should be logged and audited followup on issues.

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
- **Session Persistence**: `TokenStorage` persists `token`, `familyId`, and `userId` to maintain user context across app restarts.
- **STRICT LOCALIZATION & NAMING CONVENTION**: All user-facing strings MUST be defined in `composeResources/values/strings.xml`. Arabic support (`values-ar/strings.xml`) is mandatory. 
    - **Naming Convention**: `[feature/screen]_[description]` (e.g., `welcome_title`, `discovery_no_servers`).
    - **Element Type (Optional)**: `[feature/screen]_[type]_[description]` (e.g., `login_btn_submit`, `home_lbl_points`).
    - **Shared Strings**: Generic strings like `ok`, `cancel`, `save` use their base name without a prefix.
    - **Case**: Always use `snake_case`.
    - **State**: Use `UiText` in ViewModels to handle localized resources.
- **Pairing Flow**: Onboarding uses a real-time QR handshake (CameraX/ML Kit). QR content is a JSON `PairingToken` (ip, token, familyName, userId). Joining a family involves scanning, fetching users via token, and picking a profile.
- **PIN Authentication**: 4-digit PIN is verified against the server or local DB (if offline). PINs are ALWAYS hashed (SHA-256) on the client before leaving the Repository. Child PINs are optional (managed via `requiresPin` flag). During onboarding, the parent sets a PIN (setup mode), and subsequent joins or re-auths use verification mode unless disabled.
- **Points Economy**: Points are managed via a local transaction ledger (Room) as the Single Source of Truth. Each point movement (Chore, Bonus, Penalty, Redemption) is recorded as a `Transaction` entity. Parents can award points directly via a dedicated **Behavior** management tab.
- **Role-Based Dashboard**: The main UI uses a role-sensitive navigation system (Bottom Navigation Bar).
    - **Parents**: 5-tab system (Overview, Tasks, Behavior, Rewards, Family).
    - **Children**: 3-tab system (Today, History, Store).
- **Navigation logic** automatically routes users to their specific functional area and uses type-safe sub-routes within the Dashboard.
- **Logging** use kermit to log.
- **Auditing** all transaction should be preserved in DB server and local. 

## Domain Rules
- **Chore Verification**: Mandatory live photo (no gallery uploads).
- **Points**: Points are never deducted without parent approval (default).
- **Point SSOT**: The local Room `Transaction` ledger is the final authority on balances; server sync ensures multi-device consistency.
- **Authentication**: Optional 4-digit PIN for child profiles.
- **Secure Communication**: [FUTURE] Migrate from cleartext HTTP to HTTPS for all server communications (currently using `usesCleartextTraffic` for development).
