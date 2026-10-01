package org.aals.family.chore.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraCaptureView(
    onImageCaptured: (ByteArray) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
