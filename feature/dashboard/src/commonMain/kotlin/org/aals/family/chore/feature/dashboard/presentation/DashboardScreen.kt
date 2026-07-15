package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_tab_store
import familychore.core.generated.resources.error_unknown
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.aals.family.chore.feature.dashboard.presentation.components.BehaviorTabContent
import org.aals.family.chore.feature.dashboard.presentation.components.ChildTodayContent
import org.aals.family.chore.feature.dashboard.presentation.components.ConnectivityBanner
import org.aals.family.chore.feature.dashboard.presentation.components.DashboardBottomBar
import org.aals.family.chore.feature.dashboard.presentation.components.DashboardTopBar
import org.aals.family.chore.feature.dashboard.presentation.components.FamilyManagementContent
import org.aals.family.chore.feature.dashboard.presentation.components.HistoryTabContent
import org.aals.family.chore.feature.dashboard.presentation.components.InviteQrDialog
import org.aals.family.chore.feature.dashboard.presentation.components.ParentOverviewContent
import org.aals.family.chore.feature.dashboard.presentation.components.ParentTasksContent
import org.aals.family.chore.feature.dashboard.presentation.navigation.BehaviorRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildTodayRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.FamilyManagementRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.HistoryRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentTasksRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onLogout: (isServerOnline: Boolean, familyId: String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DashboardEvent.Logout -> onLogout(event.isServerOnline, event.familyId)
            DashboardEvent.NavigateToSettings -> onNavigateToSettings()
        }
    }

    DashboardScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                val currentLanguage = (state as? DashboardState.Success)?.language ?: AppLanguage.ENGLISH
                DashboardTopBar(state, currentLanguage, onAction)
                if (state is DashboardState.Success) {
                    ConnectivityBanner(state.isServerReachable)
                }
            }
        },
        bottomBar = {
            if (state is DashboardState.Success) {
                DashboardBottomBar(state, onAction)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                DashboardState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardState.Error -> {
                    Text(state.message.asString(), modifier = Modifier.align(Alignment.Center))
                }
                is DashboardState.Success -> {
                    DashboardContent(state, onAction)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Box {
        AnimatedContent(targetState = state.currentTab) { tab ->
            when (tab) {
                ParentOverviewRoute -> ParentOverviewContent(state, onAction)
                ParentTasksRoute -> ParentTasksContent(state, onAction)
                BehaviorRoute -> BehaviorTabContent(state, onAction)
                ParentRewardsRoute -> Text(stringResource(Res.string.dashboard_tab_store), modifier = Modifier.fillMaxSize())
                FamilyManagementRoute -> FamilyManagementContent(state, onAction)
                
                ChildTodayRoute -> ChildTodayContent(state, onAction)
                HistoryRoute -> HistoryTabContent(state, onAction)
                ChildRewardsRoute -> Text(stringResource(Res.string.dashboard_tab_store), modifier = Modifier.fillMaxSize())
                else -> Text(stringResource(Res.string.error_unknown))
            }
        }

        state.inviteQrContent?.let { content ->
            InviteQrDialog(
                qrContent = content,
                onDismiss = { onAction(DashboardAction.DismissInviteQr) }
            )
        }
    }
}

@Preview
@Composable
private fun DashboardScreenParentPreview() {
    val user = User("1", "family1", "Dad", UserRole.PARENT, 0)
    val members = listOf(
        User("2", "family1", "Alice", UserRole.CHILD, 100),
        User("3", "family1", "Bob", UserRole.CHILD, 50)
    )
    val state = DashboardState.Success(
        user = user,
        familyMembers = members,
        currentTab = ParentOverviewRoute,
        selectedAssigneeId = "2"
    )
    DashboardScreen(state = state, onAction = {})
}

@Preview
@Composable
private fun DashboardScreenChildPreview() {
    val user = User("2", "family1", "Alice", UserRole.CHILD, 100)
    val state = DashboardState.Success(
        user = user,
        currentTab = ChildTodayRoute
    )
    DashboardScreen(state = state, onAction = {})
}
