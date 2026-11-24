package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.choose_chore_preset
import familychore.core.generated.resources.chore_image_source_title
import familychore.core.generated.resources.pick_from_gallery
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.aals.family.chore.core.presentation.components.ChoreImage
import org.aals.family.chore.core.presentation.components.ChorePresets
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.jetbrains.compose.resources.stringResource

/**
 * Source-choice dialog for a chore's image / infographic.
 * Offers picking from the gallery (FileKit) or choosing a bundled preset.
 */
@Composable
fun ChoreImageSourceDialog(
    choreId: String,
    onAction: (DashboardAction) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val picker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Gallery",
        onResult = { file ->
            if (file != null) {
                scope.launch {
                    try {
                        val bytes = file.readBytes()
                        onAction(DashboardAction.OnChoreImagePicked(choreId, bytes))
                    } catch (e: Exception) {
                        co.touchlab.kermit.Logger.e(e) { "Gallery readBytes failed for ${file.name}" }
                    }
                }
            }
        },
    )
    Dialog(onDismissRequest = { onAction(DashboardAction.DismissChoreImageSourceDialog) }) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.chore_image_source_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(8.dp))

                SourceRow(
                    icon = { Icon(Icons.Default.Collections, contentDescription = null) },
                    label = stringResource(Res.string.pick_from_gallery),
                    onClick = { picker.launch() },
                )
                HorizontalDivider()
                SourceRow(
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    label = stringResource(Res.string.choose_chore_preset),
                    onClick = { onAction(DashboardAction.ShowChorePresetPicker) },
                )
                HorizontalDivider()
                SourceRow(
                    icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    label = "Remove Image",
                    onClick = { onAction(DashboardAction.ClearChoreImage(choreId)) },
                )

                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onAction(DashboardAction.DismissChoreImageSourceDialog) }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

/**
 * Grid dialog to pick a bundled preset infographic for a chore.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChorePresetPickerDialog(
    choreId: String,
    onAction: (DashboardAction) -> Unit,
) {
    Dialog(onDismissRequest = { onAction(DashboardAction.DismissChorePresetPicker) }) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.choose_chore_preset),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 4,
                ) {
                    ChorePresets.forEach { key ->
                        ChoreImage(
                            imagePath = "bundled:$key",
                            size = 64.dp,
                            modifier = Modifier
                                .clickable {
                                    onAction(DashboardAction.SelectChorePreset(choreId, key))
                                }
                                .padding(4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onAction(DashboardAction.DismissChorePresetPicker) }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceRow(
    label: String,
    onClick: (() -> Unit)?,
    icon: @Composable () -> Unit,
) {
    val rowModifier = if (onClick != null) {
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)
    } else {
        Modifier.fillMaxWidth().padding(vertical = 12.dp)
    }
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (onClick != null) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
