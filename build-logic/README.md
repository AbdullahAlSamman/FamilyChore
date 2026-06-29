# Build Logic Module (:build-logic)

## Purpose
This module contains Gradle Convention Plugins used to centralize and standardize build configurations across all project modules. It prevents version drift and reduces boilerplate in `build.gradle.kts` files.

## Available Plugins
- `familychore.android.application`: Configures Android app modules (e.g., `:app:androidApp`).
- `familychore.android.library`: Configures standard Android library modules.
- `familychore.android.feature`: Combines Android library, Compose, and Koin for feature modules.
- `familychore.kmp.library`: Configures shared Kotlin Multiplatform library modules (e.g., `:core`).
- `familychore.compose`: Standardized Compose Multiplatform and Compiler configuration.
- `familychore.room`: Centralized Room KMP and KSP configuration.
- `familychore.ktor`: Standardized Ktor Client and Serialization configuration.
- `familychore.koin`: Standardized Koin configuration.

## Dependencies
- **Gradle Kotlin DSL**: Core scripting.
- **Android Gradle Plugin (AGP) 9.0**: Platform management.
- **Kotlin Gradle Plugin**: Language management.
- **Compose Multiplatform Plugin**: UI framework management.

## Consumers
- All project modules via `id("familychore.*")` plugin applications.

## Testing
- Currently, build logic does not have automated unit tests.
