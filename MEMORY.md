# FamilyChore Project Memory

This file tracks critical architectural decisions and domain rules for the FamilyChore project.

## Critical Engineering Decisions
- **Role-Based UX**: Unified binary, branched UI (Parent vs. Child dashboards).
- **Offline-First**: Room KMP is the Single Source of Truth.
- **Data Sovereignty**: Local Ktor server (Home Lab); no external analytics.
- **Multi-Tenancy**: Every entity and request is scoped via `familyId`.
- **Navigation**: Type-Safe Compose Navigation (@Serializable routes).
- **Build Infra**: Gradle Convention Plugins in `:build-logic`.

## Domain Rules
- **Chore Verification**: Mandatory live photo (no gallery uploads).
- **Points**: Points are never deducted without parent approval (default).
- **Authentication**: Optional 4-digit PIN for child profiles.
