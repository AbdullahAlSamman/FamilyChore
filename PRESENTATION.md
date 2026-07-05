# Presentation Layer Guidelines

This project follows the **MVI (Model-View-Intent)** pattern for the presentation layer, ensuring predictable state management and a clear separation of concerns.

## MVI Components

### 1. State (`ScreenState`)
A single, immutable data class that represents the entire state of a screen at any given moment.
- **Location**: `feature:<name>/src/commonMain/kotlin/.../presentation/<Screen>State.kt`
- **Naming**: Always suffixed with `State` (e.g., `DashboardState`).

### 2. Action (`ScreenAction`)
A sealed interface representing all possible user interactions or external triggers.
- **Location**: `feature:<name>/src/commonMain/kotlin/.../presentation/<Screen>Action.kt`
- **Naming**: Always suffixed with `Action` (e.g., `DashboardAction`).

### 3. Event (`ScreenEvent`)
A sealed interface for one-time side effects that shouldn't be part of the persistent state (e.g., navigation, showing a snackbar).
- **Location**: `feature:<name>/src/commonMain/kotlin/.../presentation/<Screen>Event.kt`
- **Naming**: Always suffixed with `Event`.

### 4. ViewModel
The brain of the screen. It holds the `StateFlow<State>`, handles `Actions`, and emits `Events`.
- **Location**: `feature:<name>/src/commonMain/kotlin/.../presentation/<Screen>ViewModel.kt`
- **Dependency Injection**: ViewModels are provided via Koin (`koinViewModel()`).

## Composable Structure

Each screen is split into two parts within the same file:

### Root Composable (`<Screen>Root`)
- Entry point for the screen.
- Obtains the ViewModel via Koin.
- Collects state with `collectAsStateWithLifecycle()`.
- Observes events via `ObserveAsEvents`.
- Passes state and the `onAction` callback down to the pure screen composable.

### Screen Composable (`<Screen>Screen`)
- A pure, stateless (mostly) composable.
- Receives only `State` and `onAction: (Action) -> Unit`.
- Ideal for Compose Previews.

## Navigation

We use **Type-Safe Compose Navigation** with Kotlin Serialization.
- **Routes**: Defined as `@Serializable` objects or data classes.
- **Nested Graphs**: Features define their own `NavGraphBuilder` extension functions (e.g., `dashboardGraph`).
- **Sub-Navigation**: Complex screens like the Dashboard use internal routes for tab-based navigation.

## Design System

- **Material 3**: We use Material 3 components for a modern look and feel.
- **Common Components**: Shared UI elements (buttons, text fields, banners) live in the `core` module or a dedicated `core:ui` module if applicable.
- **Role-Based Experiences**: Screens adapt their layout and navigation based on the `UserRole` (PARENT vs. CHILD).

## Testing Strategy

- **ViewModel Tests**: Unit test state transitions and event emissions using `Turbine` and `UnconfinedTestDispatcher`.
- **UI Tests**: Test screen-level composables using `ComposeTestRule` and the Robot pattern for complex flows.
- **Fakes**: Prefer fakes over mocks for repository and service dependencies.
