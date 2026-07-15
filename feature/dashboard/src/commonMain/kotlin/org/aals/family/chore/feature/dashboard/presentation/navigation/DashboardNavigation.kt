package org.aals.family.chore.feature.dashboard.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.aals.family.chore.feature.dashboard.presentation.DashboardRoot
import org.aals.family.chore.feature.dashboard.presentation.settings.SettingsRoot

@Serializable object DashboardGraph

@Serializable object MainDashboardRoute
@Serializable object SettingsRoute

sealed interface DashboardTabRoute

// Parent Routes
@Serializable object ParentOverviewRoute : DashboardTabRoute
@Serializable object ParentTasksRoute : DashboardTabRoute
@Serializable object BehaviorRoute : DashboardTabRoute
@Serializable object ParentRewardsRoute : DashboardTabRoute
@Serializable object FamilyManagementRoute : DashboardTabRoute

// Child Routes
@Serializable object ChildTodayRoute : DashboardTabRoute
@Serializable object HistoryRoute : DashboardTabRoute
@Serializable object ChildRewardsRoute : DashboardTabRoute

fun NavGraphBuilder.dashboardGraph(
    navController: NavController,
    onLogout: (isServerOnline: Boolean, familyId: String?) -> Unit
) {
    navigation<DashboardGraph>(
        startDestination = MainDashboardRoute
    ) {
        composable<MainDashboardRoute> {
            DashboardRoot(
                onLogout = onLogout,
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                }
            )
        }
        composable<SettingsRoute> {
            SettingsRoot(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
