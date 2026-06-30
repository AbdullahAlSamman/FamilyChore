# FamilyChore Implementation Plan: Full Roadmap & Phase 7

This document outlines the complete roadmap for the **FamilyChore** application, focusing on the upcoming **Phase 7: The Points Economy**.

## Goal Description
To build a high-engagement, gamified KMP application with a local Ktor backbone, offline-first Room KMP storage, and a multi-tenant architecture scoped by `familyId`.

---

## User Review Required

> [!IMPORTANT]
> **Phase 7 - Point Integrity**: As we introduce point transactions, we must ensure the local Room database remains the source of truth, but server-side validation is required to prevent "ghost" points.
> **Role-Based UI**: Phase 7 will heavily differentiate the UI between Parents (award/deduct) and Children (view balance/history).

---

## Proposed Changes

### 1. Foundation & Infrastructure (Completed)
- **Convention Plugins**: Establishing `:build-logic` for `android-feature`, `compose`, `room`, `ktor`, and `koin`.
- **Foundation Layer**: `Result` wrappers, `SafeCall` helpers, and `UiText` resources in `:core`.
- **Phase 6: Onboarding**: QR Handshake, Family Creation, and PIN authentication are fully implemented and unit-tested.

### 2. Phase 7: The Points Economy (ACTIVE)
This phase introduces the base token economy and dashboards.

#### [NEW] Transaction Domain models
- Already defined `Transaction` and `TransactionType` in `:core`.

#### [NEW] Transaction Repository
- **`TransactionRepository`**: Interface in `:core:domain` for fetching family point history and recording new entries.
- **`TransactionRepositoryImpl`**: Implementation in `:core:data` with Room & Ktor sync logic.

#### [NEW] Feature: Points Module
- **`DashboardParentScreen`**: Summary of family points + Navigation to transaction history.
- **`DashboardChildScreen`**: Personal point balance + animated celebratory UI for new points.
- **`TransactionHistoryScreen`**: Detailed ledger of all point movements.

#### [MODIFY] Server-Side Point Logic
- **`POST /points/transaction`**: Secure endpoint to record point changes (Parent only).
- **`GET /points/history`**: Scoped history retrieval.

### 3. Future Phases (Roadmap)
- **Phase 8: Task Management**: Personalized chores, frequencies, and deadline penalties.
- **Phase 9: Behavioral Ledger (Dos & Don'ts)**: Spontaneous point adjustments and behavior tracking.
- **Phase 10: Reward Store**: Custom rewards and redemption workflow.
- **Phase 11: Sync & Background Workers**: WebSocket live updates and Platform Workers for database integrity.

---

## Verification Plan

### Automated Tests
- **Repository Tests**: `TransactionRepository` logic verified with `MockEngine`.
- **Domain Logic**: Unit tests for point balance calculation from transaction logs.
- **MVI Tests**: ViewModel testing for Parent/Child dashboard states.

### Manual Verification
- **E2E Transactions**: Award points on Parent device -> Verify WebSocket/Sync -> Check balance on Child device.
- **Role Enforcement**: Ensure Child accounts cannot access "Deduct/Award" endpoints.

---

## Documentation & Knowledge Management

### [MEMORY.md](file:///Users/aalsamman/AndroidProjects/FamilyChore/MEMORY.md)
Updated with pairing logic, PIN authentication decisions, and transaction model definitions.

### Module READMEs
All existing modules updated. New feature modules MUST include a `README.md` before implementation begins.
