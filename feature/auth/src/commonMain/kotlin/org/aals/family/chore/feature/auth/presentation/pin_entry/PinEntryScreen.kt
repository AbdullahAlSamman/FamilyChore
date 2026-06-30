package org.aals.family.chore.feature.auth.presentation.pin_entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PinEntryRoot(
    onPinVerified: () -> Unit,
    viewModel: PinEntryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PinEntryEvent.PinVerified -> onPinVerified()
        }
    }

    PinEntryScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    state: PinEntryState,
    onAction: (PinEntryAction) -> Unit
) {
    val isLoading = state is PinEntryState.Verifying
    val error = (state as? PinEntryState.Entering)?.error

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Enter PIN") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter your 4-digit PIN to continue.")

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.pin,
                onValueChange = { onAction(PinEntryAction.OnPinChange(it)) },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.width(150.dp),
                enabled = !isLoading,
                singleLine = true
            )

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onAction(PinEntryAction.OnSubmit) },
                enabled = state.pin.length == 4 && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit")
                }
            }
        }
    }
}
