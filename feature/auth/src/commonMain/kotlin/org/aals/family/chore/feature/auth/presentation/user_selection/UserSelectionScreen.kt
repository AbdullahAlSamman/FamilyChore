package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.refresh
import familychore.core.generated.resources.role_child
import familychore.core.generated.resources.role_parent
import familychore.core.generated.resources.user_selection_screen_title
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.aals.family.chore.feature.auth.presentation.components.ConnectivityBanner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserSelectionRoot(
    onPairingConfirmed: (String) -> Unit,
    onPinVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: UserSelectionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is UserSelectionEvent.PairingConfirmed -> onPairingConfirmed(event.userId)
            UserSelectionEvent.PinVerified -> onPinVerified()
        }
    }

    UserSelectionScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun UserSelectionScreen(
    state: UserSelectionState,
    onAction: (UserSelectionAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(Res.string.user_selection_screen_title)) },
                    navigationIcon = {
                        if ((state is UserSelectionState.Success) && state.isFromDiscovery) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                )
                if (!state.isOfflineMode) {
                    ConnectivityBanner(isReachable = state.isServerReachable)
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is UserSelectionState.Loading -> {
                    CircularProgressIndicator()
                }
                is UserSelectionState.Success -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.users) { user ->
                                ListItem(
                                    headlineContent = { Text(user.nickname) },
                                    supportingContent = {
                                        val roleText = when (user.role) {
                                            UserRole.PARENT -> stringResource(Res.string.role_parent)
                                            UserRole.CHILD -> stringResource(Res.string.role_child)
                                        }
                                        Text(roleText)
                                    },
                                    modifier = Modifier.clickable(enabled = !state.isConfirming) {
                                        onAction(UserSelectionAction.OnUserClick(user))
                                    },
                                )
                            }
                        }

                        if (state.isConfirming) {
                            CircularProgressIndicator()
                        }

                        state.error?.let {
                            Text(
                                text = it.asString(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                            )
                        }
                    }
                }
                is UserSelectionState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = state.message.asString(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = { onAction(UserSelectionAction.OnRetryClick) }) {
                            Text(stringResource(Res.string.refresh))
                        }
                    }
                }
            }
        }
    }
}
