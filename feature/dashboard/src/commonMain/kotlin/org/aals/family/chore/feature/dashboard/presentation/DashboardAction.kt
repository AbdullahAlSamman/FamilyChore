package org.aals.family.chore.feature.dashboard.presentation

sealed interface DashboardAction {
    data object Refresh : DashboardAction
    data object Logout : DashboardAction
}
