package org.aals.family.chore.feature.auth.presentation.pin_entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.pin_instructions
import familychore.core.generated.resources.pin_label
import familychore.core.generated.resources.pin_title
import familychore.core.generated.resources.submit
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PinEntryRoot(
    onPinVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PinEntryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PinEntryEvent.PinVerified -> onPinVerified()
            PinEntryEvent.NavigateBack -> onNavigateBack()
        }
    }

    PinEntryScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun PinEntryScreen(
    state: PinEntryState,
    onAction: (PinEntryAction) -> Unit
) {
    if (state is PinEntryState.Checking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isLoading = state is PinEntryState.Verifying
    val error = (state as? PinEntryState.Entering)?.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.pin_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(PinEntryAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
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
            Text(stringResource(Res.string.pin_instructions))

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.pin,
                onValueChange = { onAction(PinEntryAction.OnPinChange(it)) },
                label = { Text(stringResource(Res.string.pin_label)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.width(150.dp),
                enabled = !isLoading,
                singleLine = true
            )

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it.asString(), color = MaterialTheme.colorScheme.error)
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
                    Text(stringResource(Res.string.submit))
                }
            }
        }
    }
}

@Preview
@Composable
fun PinEntryScreenPreview() {
    MaterialTheme {
        PinEntryScreen(
            state = PinEntryState.Entering(pin = "12"),
            onAction = {}
        )
    }
}
