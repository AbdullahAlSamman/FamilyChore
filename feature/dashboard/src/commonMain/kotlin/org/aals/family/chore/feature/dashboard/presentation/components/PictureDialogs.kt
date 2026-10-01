package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.choose_preset
import familychore.core.generated.resources.pick_from_gallery
import familychore.core.generated.resources.picture_source_title
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.presentation.components.MemberAvatarPresets
import org.aals.family.chore.core.presentation.components.PresetAvatar
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.jetbrains.compose.resources.stringResource

/**
 * Source-choice dialog for a member's profile picture.
 * Offers picking from the gallery (FileKit) or choosing a bundled preset.
 * Camera capture is intentionally deferred (Android-only follow-up).
 */
@Composable
fun PictureSourceDialog(
    target: User,
    onAction: (DashboardAction) -> Unit,
) {
    val picker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Gallery",
        onResult = { file ->
            val path = file?.path
            if (path != null) {
                onAction(DashboardAction.OnPickedImage(target.id, path))
            }
        },
    )

    Dialog(onDismissRequest = { onAction(DashboardAction.DismissPictureSourceDialog) }) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.picture_source_title),
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
                    label = stringResource(Res.string.choose_preset),
                    onClick = { onAction(DashboardAction.ShowPresetPicker) },
                )

                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onAction(DashboardAction.DismissPictureSourceDialog) }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

/**
 * Grid dialog to pick a bundled preset avatar for a member.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetPickerDialog(
    target: User,
    onAction: (DashboardAction) -> Unit,
) {
    Dialog(onDismissRequest = { onAction(DashboardAction.DismissPresetPicker) }) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.choose_preset),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 4,
                ) {
                    MemberAvatarPresets.forEach { key ->
                        PresetAvatar(
                            presetKey = key,
                            size = 64.dp,
                            modifier = Modifier
                                .clickable {
                                    onAction(DashboardAction.SelectPresetAvatar(target.id, key))
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
                    TextButton(onClick = { onAction(DashboardAction.DismissPresetPicker) }) {
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
