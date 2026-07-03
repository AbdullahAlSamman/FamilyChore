package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.domain.model.UserRole
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onAction = { action ->
            if (action is DashboardAction.Logout) {
                onLogout()
            } else {
                viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                DashboardTopBar(state, onAction)
                if (state is DashboardState.Success) {
                    ConnectivityBanner(state.isServerReachable)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                DashboardState.Loading -> CircularProgressIndicator()
                is DashboardState.Error -> Text(state.message)
                is DashboardState.Success -> {
                    if (state.user.role == UserRole.PARENT) {
                        ParentDashboardContent(state, onAction)
                    } else {
                        ChildDashboardContent(state, onAction)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    TopAppBar(
        title = {
            if (state is DashboardState.Success) {
                Text(state.user.nickname)
            } else {
                Text("Dashboard")
            }
        },
        actions = {
            IconButton(onClick = { onAction(DashboardAction.Logout) }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            }
        }
    )
}

@Composable
fun ConnectivityBanner(isReachable: Boolean) {
    if (!isReachable) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Server Unreachable - Offline Mode",
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ParentDashboardContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Parent Dashboard (Admin)", style = MaterialTheme.typography.headlineMedium)
        Text("Family Overview: Awaiting Approvals")
        // TODO: Implement overview sections
    }
}

@Composable
fun ChildDashboardContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Child Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text("Current Points: ${state.user.points}")
        Text("Today's Tasks")
        // TODO: Implement unified view
    }
}
