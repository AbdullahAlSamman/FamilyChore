# FamilyChore Project Memory

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

## Domain Rules
- **Chore Verification**: Mandatory live photo (no gallery uploads).
- **Points**: Points are never deducted without parent approval (default).
- **Authentication**: Optional 4-digit PIN for child profiles.
