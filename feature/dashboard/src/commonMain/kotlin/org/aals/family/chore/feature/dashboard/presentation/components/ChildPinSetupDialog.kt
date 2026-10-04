package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.child_pin_setup_instructions
import familychore.core.generated.resources.child_pin_setup_title
import familychore.core.generated.resources.member_pin_change_instructions
import familychore.core.generated.resources.member_pin_change_title
import familychore.core.generated.resources.pin_label
import familychore.core.generated.resources.save
import org.aals.family.chore.core.presentation.UiText
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChildPinSetupDialog(
    childName: String,
    pin: String,
    pinVisible: Boolean,
    error: UiText?,
    isSaving: Boolean,
    isChangeMode: Boolean = false,
    onPinChange: (String) -> Unit,
    onPinVisibleToggle: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isChangeMode) {
                        stringResource(Res.string.member_pin_change_title, childName)
                    } else {
                        stringResource(Res.string.child_pin_setup_title, childName)
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = if (isChangeMode) {
                        stringResource(Res.string.member_pin_change_instructions)
                    } else {
                        stringResource(Res.string.child_pin_setup_instructions)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) onPinChange(it) },
                    label = { Text(stringResource(Res.string.pin_label)) },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        val image = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = onPinVisibleToggle) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    enabled = !isSaving,
                    singleLine = true
                )

                error?.let {
                    Text(
                        text = it.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(
                        onClick = { onConfirm(pin) },
                        enabled = pin.length in 4..8 && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text(stringResource(Res.string.save))
                        }
                    }
                }
            }
        }
    }
}
