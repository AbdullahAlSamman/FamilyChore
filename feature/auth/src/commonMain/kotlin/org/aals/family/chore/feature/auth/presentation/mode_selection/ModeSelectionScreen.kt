package org.aals.family.chore.feature.auth.presentation.mode_selection

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.mode_selection_multiple_devices
import familychore.core.generated.resources.mode_selection_multiple_devices_desc
import familychore.core.generated.resources.mode_selection_single_device
import familychore.core.generated.resources.mode_selection_single_device_desc
import familychore.core.generated.resources.mode_selection_title
import familychore.core.generated.resources.welcome_change_language
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModeSelectionRoot(
    onNavigateToWelcome: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    viewModel: ModeSelectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ModeSelectionEvent.NavigateToWelcome -> onNavigateToWelcome()
            ModeSelectionEvent.NavigateToDiscovery -> onNavigateToDiscovery()
        }
    }

    ModeSelectionScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ModeSelectionScreen(
    state: ModeSelectionState,
    onAction: (ModeSelectionAction) -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val nextLang = if (state.currentLanguage == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                        onAction(ModeSelectionAction.OnChangeLanguage(nextLang))
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = stringResource(Res.string.welcome_change_language)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.mode_selection_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            ModeCard(
                title = stringResource(Res.string.mode_selection_single_device),
                description = stringResource(Res.string.mode_selection_single_device_desc),
                icon = Icons.Default.Smartphone,
                onClick = { onAction(ModeSelectionAction.OnSingleDeviceClick) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeCard(
                title = stringResource(Res.string.mode_selection_multiple_devices),
                description = stringResource(Res.string.mode_selection_multiple_devices_desc),
                icon = Icons.Default.Cloud,
                onClick = { onAction(ModeSelectionAction.OnMultipleDevicesClick) }
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
