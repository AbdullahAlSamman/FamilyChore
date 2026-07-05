# Dashboard Feature Module (:feature:dashboard)

## Purpose
The `:feature:dashboard` module provides the main user interface after successful authentication. It implements a role-based experience tailored for Parents (Admins) and Children.

## Key Features
- **Role-Based Navigation**:
    - **Parents**: 5-tab system (Overview, Tasks, Behavior, Rewards, Family).
    - **Children**: 3-tab system (Today, History, Store).
- **Points Economy Interaction**:
    - **Behavior Management**: Parents can award/deduct points using a grid of predefined behaviors (Do/Don'ts).
    - **Transaction History**: Real-time ledger showing point movements with visual indicators.
- **Connectivity Monitoring**: Real-time server health tracking with a persistent connectivity banner for offline feedback.
- **Offline Resilience**: Immediate local persistence of transactions and display of cached data.

## Architecture
Follows the **MVI** pattern:
- **State**: `DashboardState` includes role-based data, current tab, and selected child.
- **Action**: `DashboardAction` covers navigation, refreshing, and awarding points.
- **ViewModel**: `DashboardViewModel` manages complex role-based transitions and data loading.
- **Navigation**: Type-safe routing using Kotlin Serialization sub-routes within the Dashboard.

## Dependencies
- `:core`: Domain models, repositories, and UI utilities.
- **Compose Multiplatform**: Shared UI implementation.
- **Koin**: Dependency injection.

## Testing
- Unit tests in `src/commonTest` for ViewModel logic and state transitions.
