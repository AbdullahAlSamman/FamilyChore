package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.mode_selection_multiple_devices
import familychore.core.generated.resources.mode_selection_multiple_devices_desc
import familychore.core.generated.resources.ok
import familychore.core.generated.resources.welcome_change_language
import familychore.core.generated.resources.welcome_join_family
import familychore.core.generated.resources.welcome_login_existing
import familychore.core.generated.resources.welcome_or
import familychore.core.generated.resources.welcome_setup_family
import familychore.core.generated.resources.welcome_subtitle
import familychore.core.generated.resources.welcome_title_generic
import familychore.core.generated.resources.welcome_title_to
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.aals.family.chore.feature.auth.presentation.components.ConnectivityBanner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeRoot(
    onNavigateToSetupFamily: () -> Unit,
    onNavigateToJoinFamily: () -> Unit,
    onNavigateToUserSelection: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            WelcomeEvent.NavigateToSetupFamily -> onNavigateToSetupFamily()
            WelcomeEvent.NavigateToJoinFamily -> onNavigateToJoinFamily()
            is WelcomeEvent.NavigateToUserSelection -> onNavigateToUserSelection(event.familyId)
            WelcomeEvent.NavigateToModeSelection -> onNavigateBack()
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
    var showOfflineJoinDialog by remember { mutableStateOf(false) }

    if (showOfflineJoinDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineJoinDialog = false },
            title = { Text(stringResource(Res.string.mode_selection_multiple_devices)) },
            text = { Text(stringResource(Res.string.mode_selection_multiple_devices_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showOfflineJoinDialog = false
                    onAction(WelcomeAction.OnBackClick)
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineJoinDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { onAction(WelcomeAction.OnBackClick) },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = {
                            val nextLang = if (state.currentLanguage == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                            onAction(WelcomeAction.OnChangeLanguage(nextLang))
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(Res.string.welcome_change_language)
                        )
                    }
                }
                if (!state.isOfflineMode) {
                    ConnectivityBanner(isReachable = state.isServerReachable)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.serverName != null) {
                    stringResource(Res.string.welcome_title_to, state.serverName)
                } else {
                    stringResource(Res.string.welcome_title_generic)
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(Res.string.welcome_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.error?.let { err ->
                Text(
                    text = err.asString(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (state.families.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.welcome_login_existing),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.families.forEach { family ->
                        OutlinedButton(
                            onClick = { onAction(WelcomeAction.OnFamilyClick(family)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(family.name)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(Res.string.welcome_or),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { onAction(WelcomeAction.OnSetupNewFamilyClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.welcome_setup_family))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { 
                    if (state.isOfflineMode) {
                        showOfflineJoinDialog = true
                    } else {
                        onAction(WelcomeAction.OnJoinFamilyClick)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.welcome_join_family))
            }
        }
    }
}

@Composable
@Preview
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            state = WelcomeState(
                serverName = "My Family Hub",
                families = (1..10).map { Family(it.toString(), "Family $it") }
            ),
            onAction = {}
        )
    }
}
