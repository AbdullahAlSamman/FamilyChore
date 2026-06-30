package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeRoot(
    onNavigateToSetupFamily: () -> Unit,
    onNavigateToJoinFamily: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            WelcomeEvent.NavigateToSetupFamily -> onNavigateToSetupFamily()
            WelcomeEvent.NavigateToJoinFamily -> onNavigateToJoinFamily()
        }
    }

    WelcomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onAction: (WelcomeAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.serverName != null) {
                    "Welcome to ${state.serverName}"
                } else {
                    "Welcome to FamilyChore"
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Get started by setting up your family hub or joining an existing one.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { onAction(WelcomeAction.OnSetupNewFamilyClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Setup New Family")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { onAction(WelcomeAction.OnJoinFamilyClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Existing Family")
            }
        }
    }
}

@Composable
@Preview
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            state = WelcomeState(serverName = "My Family Hub"),
            onAction = {}
        )
    }
}

