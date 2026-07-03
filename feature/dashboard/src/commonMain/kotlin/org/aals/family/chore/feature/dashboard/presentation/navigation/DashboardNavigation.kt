package org.aals.family.chore.feature.dashboard.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.aals.family.chore.feature.dashboard.presentation.DashboardRoot

@Serializable object DashboardGraph

@Serializable object MainDashboardRoute

fun NavGraphBuilder.dashboardGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    navigation<DashboardGraph>(
        startDestination = MainDashboardRoute
    ) {
        composable<MainDashboardRoute> {
            DashboardRoot(
                onLogout = onLogout
            )
        }
    }
}
