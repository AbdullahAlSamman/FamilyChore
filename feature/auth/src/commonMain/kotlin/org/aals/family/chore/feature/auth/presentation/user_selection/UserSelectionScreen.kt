package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.presentation.ObserveAsEvents
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
            TopAppBar(title = { Text("Select Profile") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.users) { user ->
                        ListItem(
                            headlineContent = { Text(user.nickname) },
                            supportingContent = { Text(user.role.name) },
                            modifier = Modifier.clickable {
                                onAction(UserSelectionAction.OnUserClick(user))
                            }
                        )
                    }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
