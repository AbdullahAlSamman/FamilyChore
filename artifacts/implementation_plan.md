# FamilyChore Implementation Plan: Full Roadmap & Current Progress

This document outlines the complete roadmap for the **FamilyChore** application, including past achievements and upcoming phases.

## Current Focus: Phase 7 - The Points Economy
Implementing the core token economy, transaction ledger, and role-based dashboards for parents and children.

---

## Roadmap

### 1. Foundation & Infrastructure (Completed)
- **Convention Plugins**: Establishing `:build-logic` for `android-feature`, `compose`, `room`, `ktor`, and `koin`.
- **Foundation Layer**: `Result` wrappers, `SafeCall` helpers, and `UiText` resources in `:core`.
- **Unified Logging**: Migrated to **Kermit** for structured multiplatform logging across all modules.

### 2. Onboarding & Discovery (Completed)
- **Phase 6**: QR Handshake, Family Creation, and PIN authentication.
- **Phase 6.5 (Discovery)**: Automated mDNS/LAN scanning to find the Ktor server and cache its IP.

### 3. Smart Startup & Persistence (Completed)
- **Phase 6.5 (Smart Startup)**: Health-aware startup logic navigating to **User Selection** screen if server is healthy.
- **Connectivity Status**: Global `isServerReachable` monitor (30s polling) for real-time UI updates.
- **Phase 6.6 (Server Persistence)**: Migrated Ktor server from in-memory storage to **SQLite** using **Exposed**.

### 4. Phase 7: The Points Economy (ACTIVE)
- **Point Integrity**: Source of truth in Room with server-side validation.
- **Offline Dashboard**: Load cached data and disable mutation actions when the server is unreachable (Deferred from 6.5).
- **Transaction Domain**: `Transaction` and `TransactionType` models.
- **Dashboard Refactor**: Role-based UI for Parents and Children.
- **History**: Detailed ledger of all point movements.

### 5. Phase 8: Health & Connectivity
- **Global Connectivity Status**: Implement a periodic `ServerHealthMonitor` for real-time status updates.
- **Logging Migration**: Migrate all logging to **Kermit** for unified multiplatform logging.

### 6. Future Roadmap
- **Phase 9: Task Management**: Personalized chores and verification workflow.
- **Phase 10: Behavioral Ledger**: Spontaneous point adjustments.
- **Phase 11: Reward Store**: Redemption workflow.
- **Phase 12: Sync & Background Workers**: WebSocket updates and database integrity workers.

---

## Verification Strategy
- **Automated**: Unit tests for all repositories, ViewModels, and navigation logic.
- **Manual**: E2E verification across Android and JVM targets.
