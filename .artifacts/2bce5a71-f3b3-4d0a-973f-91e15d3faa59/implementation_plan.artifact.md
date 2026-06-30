# Server Discovery & Auth Feature Refactor Plan

The goal is to implement a robust **Server Discovery** feature and separate it from the **Authentication** and **Onboarding** flows. The app will automatically determine the best starting screen based on the current connection and authentication state.

## User Review Required

> [!IMPORTANT]
> - **Separation of Features:**
>     - **Discovery:** A dedicated gateway screen to find and select a local FamilyChore server.
>     - **Auth:** Identity verification (PIN entry, token storage) for existing users.
>     - **Onboarding:** Initial family creation or joining (QR scan/Setup) for new users.
> - **Smart Navigation Logic:**
>     - **Already Authenticated?** (Token + FamilyId + ServerUrl present) -> Skip straight to the **Main Dashboard**.
>     - **Server Connected but No Session?** (ServerUrl present, no Token) -> Land on the **Welcome/Auth** screen.
>     - **New Device?** (No ServerUrl) -> Start at the **Server Discovery** screen.
> - **Discovery Persistence:** The selected server URL and name are saved locally. Users can manually trigger a "Change Server" if needed later.
> - **iOS & Desktop Support:** mDNS discovery implemented via native APIs for iOS and JmDNS for Desktop.

## Proposed Changes

### Core Module

#### [MODIFY] [core/build.gradle.kts](file:///Users/aalsamman/AndroidProjects/FamilyChore/core/build.gradle.kts)
- Add `jmdns` dependency to `jvmMain` for Desktop discovery support.

#### [MODIFY] [ServerDiscovery.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/core/src/commonMain/kotlin/org/aals/family/chore/core/domain/discovery/ServerDiscovery.kt)
- Update interface to return `Flow<DiscoveredServer>`.
- `DiscoveredServer` will contain `name` (broadcasted name) and `url`.

#### [MODIFY] [Platform Implementations]
- Update `AndroidServerDiscovery.kt`, `JvmServerDiscovery.kt`, and `IosServerDiscovery.kt` to emit `DiscoveredServer` with the resolved host name.

---

### Feature: Discovery (New)

#### [NEW] [ServerDiscoveryScreen.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/feature/auth/src/commonMain/kotlin/org/aals/family/chore/feature/auth/presentation/discovery/ServerDiscoveryScreen.kt)
- A clean UI for scanning the local network.
- Displays found servers in a list/card format.
- "Scan Again" action and "Connect" on selection.

#### [NEW] [ServerDiscoveryViewModel.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/feature/auth/src/commonMain/kotlin/org/aals/family/chore/feature/auth/presentation/discovery/ServerDiscoveryViewModel.kt)
- Orchestrates the `ServerDiscovery` flow.
- Saves the selected URL and name to `TokenStorage`.
- Emits navigation events to move to the next screen.

---

### Feature: Auth & Onboarding Refactor

#### [MODIFY] [AuthNavigation.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/feature/auth/src/commonMain/kotlin/org/aals/family/chore/feature/auth/presentation/navigation/AuthNavigation.kt)
- Refactor the `AuthGraph` to include the `ServerDiscoveryRoute`.
- Decouple discovery from the "Welcome" flow.

#### [MODIFY] [WelcomeViewModel.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/feature/auth/src/commonMain/kotlin/org/aals/family/chore/feature/auth/presentation/welcome/WelcomeViewModel.kt)
- Remove all discovery-related code.
- Focus purely on "Setup New Family" and "Join Existing Family" actions.

---

### App Level

#### [MODIFY] [App.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/app/shared/src/commonMain/kotlin/org/aals/family/chore/App.kt)
- Implement the **Smart Start** logic.
- Inject `TokenStorage` to check state on startup and set the appropriate `startDestination`.
- Add a placeholder/navigation hook for the **Home Dashboard**.

---

### Server Module

#### [MODIFY] [DiscoveryBroadcaster.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/server/src/main/kotlin/org/aals/family/chore/DiscoveryBroadcaster.kt)
- Enhance the broadcaster to include the host machine's name in the mDNS service info.
- Ensure it listens and broadcasts on all valid network interfaces.

## Verification Plan

### Automated Tests
- Unit tests for `ServerDiscoveryViewModel` using a fake discovery flow.
- Navigation tests for the "Smart Start" logic in `App.kt`.

### Manual Verification
1.  **Fresh Launch:** App opens to "Scanning for Servers...".
2.  **Discovery:** Server appears with its name.
3.  **Selection:** App moves to "Join/Setup" screen.
4.  **Auto-Skip:** Restarting the app after selection skips discovery.
5.  **Dashboard Skip:** Restarting after full setup lands directly on the Dashboard.
