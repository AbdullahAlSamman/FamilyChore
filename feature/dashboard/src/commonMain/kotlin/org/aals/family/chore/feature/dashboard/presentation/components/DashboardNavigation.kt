package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_logout
import familychore.core.generated.resources.dashboard_offline_banner
import familychore.core.generated.resources.dashboard_tab_behavior
import familychore.core.generated.resources.dashboard_tab_family
import familychore.core.generated.resources.dashboard_tab_history
import familychore.core.generated.resources.dashboard_tab_overview
import familychore.core.generated.resources.dashboard_tab_rewards
import familychore.core.generated.resources.dashboard_tab_store
import familychore.core.generated.resources.dashboard_tab_tasks
import familychore.core.generated.resources.dashboard_tab_today
import familychore.core.generated.resources.dashboard_title
import familychore.core.generated.resources.welcome_change_language
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.aals.family.chore.feature.dashboard.presentation.DashboardState
import org.aals.family.chore.feature.dashboard.presentation.navigation.BehaviorRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildTodayRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.FamilyManagementRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.HistoryRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentTasksRoute
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    state: DashboardState,
    currentLanguage: AppLanguage,
    onAction: (DashboardAction) -> Unit
) {
    TopAppBar(
        title = {
            Text(stringResource(Res.string.dashboard_title))
        },
        actions = {
            IconButton(onClick = {
                val nextLang = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                onAction(DashboardAction.ChangeLanguage(nextLang))
            }) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(Res.string.welcome_change_language)
                )
            }
            IconButton(onClick = { onAction(DashboardAction.Logout) }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = stringResource(Res.string.dashboard_logout)
                )
            }
        }
    )
}

@Composable
fun ConnectivityBanner(isReachable: Boolean) {
    if (!isReachable) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.8f))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.dashboard_offline_banner),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DashboardBottomBar(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    val tabs = if (state.user.role == UserRole.PARENT) {
        listOf(
            ParentOverviewRoute,
            ParentTasksRoute,
            FamilyManagementRoute,
            HistoryRoute
        )
    } else {
        listOf(
            ChildTodayRoute,
            BehaviorRoute,
            ParentRewardsRoute,
            ChildRewardsRoute
        )
    }

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = state.currentTab == tab,
                onClick = { onAction(DashboardAction.ChangeTab(tab)) },
                icon = {
                    Icon(
                        imageVector = getIconForRoute(tab),
                        contentDescription = stringResource(getLabelForRoute(tab))
                    )
                },
                label = {
                    Text(stringResource(getLabelForRoute(tab)))
                }
            )
        }
    }
}

private fun getIconForRoute(tab: DashboardTabRoute): ImageVector {
    return when (tab) {
        ParentOverviewRoute -> Icons.Default.Dashboard
        ParentTasksRoute -> Icons.Default.List
        FamilyManagementRoute -> Icons.Default.People
        HistoryRoute -> Icons.Default.History
        ChildTodayRoute -> Icons.Default.Today
        BehaviorRoute -> Icons.Default.Star
        ParentRewardsRoute -> Icons.Default.ShoppingCart
        ChildRewardsRoute -> Icons.Default.ShoppingCart
        else -> Icons.Default.Dashboard
    }
}

private fun getLabelForRoute(tab: DashboardTabRoute): org.jetbrains.compose.resources.StringResource {
    return when (tab) {
        ParentOverviewRoute -> Res.string.dashboard_tab_overview
        ParentTasksRoute -> Res.string.dashboard_tab_tasks
        FamilyManagementRoute -> Res.string.dashboard_tab_family
        HistoryRoute -> Res.string.dashboard_tab_history
        ChildTodayRoute -> Res.string.dashboard_tab_today
        BehaviorRoute -> Res.string.dashboard_tab_behavior
        ParentRewardsRoute -> Res.string.dashboard_tab_rewards
        ChildRewardsRoute -> Res.string.dashboard_tab_store
        else -> Res.string.dashboard_title
    }
}
