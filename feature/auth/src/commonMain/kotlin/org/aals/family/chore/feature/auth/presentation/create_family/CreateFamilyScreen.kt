package org.aals.family.chore.feature.auth.presentation.create_family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.create_family_action
import familychore.core.generated.resources.create_family_name_label
import familychore.core.generated.resources.create_family_nickname_label
import familychore.core.generated.resources.create_family_title
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateFamilyRoot(
    onNavigateBack: () -> Unit,
    onFamilyCreated: (String, String) -> Unit,
    viewModel: CreateFamilyViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateFamilyEvent.FamilyCreated -> onFamilyCreated(event.familyId, event.userId)
            CreateFamilyEvent.NavigateBack -> onNavigateBack()
        }
    }

    CreateFamilyScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun CreateFamilyScreen(
    state: CreateFamilyState,
    onAction: (CreateFamilyAction) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.create_family_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = state.familyName,
                onValueChange = { onAction(CreateFamilyAction.OnFamilyNameChange(it)) },
                label = { Text(stringResource(Res.string.create_family_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.familyNameError != null,
                supportingText = state.familyNameError?.let { { Text(it.asString()) } }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.parentNickname,
                onValueChange = { onAction(CreateFamilyAction.OnParentNicknameChange(it)) },
                label = { Text(stringResource(Res.string.create_family_nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.nicknameError != null,
                supportingText = state.nicknameError?.let { { Text(it.asString()) } }
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onAction(CreateFamilyAction.OnCreateClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.create_family_action))
                }
            }
            if (state.error != null) {
                Text(
                    text = state.error.asString(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun CreateFamilyScreenPreview() {
    MaterialTheme {
        CreateFamilyScreen(
            state = CreateFamilyState(),
            onAction = {}
        )
    }
}

