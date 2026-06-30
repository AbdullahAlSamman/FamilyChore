# Walkthrough: Server Discovery & Auth Refactor

I have successfully implemented the **Server Discovery** feature and refactored the **Authentication** and **Onboarding** flows to provide a smoother, automated startup experience.

## Changes Made

### Core Module
- **Dependency:** Added `jmdns` to the `core` module for Desktop (JVM) discovery.
- **API:** Updated `ServerDiscovery` interface to return a `Flow<DiscoveredServer>`, which now includes both the server name and its URL.
- **Storage:** Enhanced `TokenStorage` and its implementations to cache the selected server's name alongside the URL.
- **Platform Discovery:**
    - **Android:** Updated `AndroidServerDiscovery` to resolve and emit the service name.
    - **JVM/Desktop:** Implemented discovery using `JmDNS`.
    - **iOS:** Updated interface (native implementation remains a placeholder for platform APIs).

### Feature: Discovery
- **UI:** Created `ServerDiscoveryScreen.kt`, a new screen that scans the local network and lists available FamilyChore servers.
- **Logic:** Implemented `ServerDiscoveryViewModel.kt` to orchestrate scanning and persistence.

### Feature: Auth Refactor
- **Navigation:** Added `ServerDiscoveryRoute` to the `AuthGraph`.
- **Decoupling:** Removed discovery logic from `WelcomeViewModel`, making it focused purely on family setup and joining.

### App Level (Smart Start)
- **Logic:** Implemented a "Smart Start" mechanism in `App.kt`. On launch, the app now automatically determines the correct starting screen:
    - **Authenticated?** -> Main Dashboard.
    - **Server Connected?** -> Welcome Screen (Setup/Join).
    - **Fresh Start?** -> Server Discovery.

### Server Module
- **Broadcasting:** Updated `DiscoveryBroadcaster.kt` to use the host machine's name as the broadcast name, making it easier for users to identify their server.

## Verification Results

### Automated Tests
- **ServerDiscoveryViewModelTest:** Verified that discovered servers are correctly added to the UI state and that selection saves the correct data.
- **WelcomeViewModelTest:** Refactored to match the new constructor and state.
- **Build:** `assembleDebug` passed successfully.
- **JVM Tests:** All 15 tests in `:feature:auth` passed.

## How to Test
1.  **Server side:** Run the server. It will broadcast its host name.
2.  **App side:**
    - On first run, you should see the **Server Discovery** screen scanning.
    - Your server should appear in the list.
    - Selecting it should navigate you to the **Welcome** screen.
    - Restarting the app should now skip Discovery and land directly on the **Welcome** screen (since the server is remembered).
