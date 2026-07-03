# Dashboard Feature Module (:feature:dashboard)

## Purpose
The `:feature:dashboard` module provides the main user interface after successful authentication. It implements a role-based experience tailored for Parents (Admins) and Children.

## Key Features
- **Role-Based UI Branching**:
    - **Parent Dashboard**: Family overview, awaiting approvals, behavior management (Do/Don'ts), and reward catalog management.
    - **Child Dashboard**: Unified "Today" view with points balance and assigned tasks, history ledger, and reward store browsing.
- **Connectivity Monitoring**: Real-time server health tracking with a persistent connectivity banner for offline feedback.
- **Offline Resilience**: Immediate display of cached data from the local database while the server is unreachable.
- **Unified Navigation**: Docked top bar for role-consistent navigation and logout actions.

## Architecture
Follows the **MVI** pattern:
- **State**: `DashboardState` (Loading, Success, Error).
- **Action**: `DashboardAction` (Refresh, Logout).
- **ViewModel**: `DashboardViewModel` orchestrates data from `AuthRepository`, `TransactionRepository`, and `ConnectivityRepository`.

## Dependencies
- `:core`: Domain models, repositories, and UI utilities.
- **Compose Multiplatform**: Shared UI implementation.
- **Koin**: Dependency injection.

## Testing
- Unit tests in `src/commonTest` for ViewModel logic and state transitions.
