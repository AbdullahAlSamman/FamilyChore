package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.user_selection_title
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserSelectionRoot(
    onPairingConfirmed: (String) -> Unit,
    viewModel: UserSelectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is UserSelectionEvent.PairingConfirmed -> onPairingConfirmed(event.userId)
        }
    }

    UserSelectionScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionScreen(
    state: UserSelectionState,
    onAction: (UserSelectionAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.user_selection_title)) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.users) { user ->
                                ListItem(
                                    headlineContent = { Text(user.nickname) },
                                    supportingContent = { Text(user.role.name) },
                                    modifier = Modifier.clickable(enabled = !state.isConfirming) {
                                        onAction(UserSelectionAction.OnUserClick(user))
                                    }
                                )
                            }
                        }

                        if (state.isConfirming) {
                            CircularProgressIndicator()
                        }

                        state.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                            )
                        }
                    }
                }
                is UserSelectionState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
