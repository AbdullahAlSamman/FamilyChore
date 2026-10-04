package org.aals.family.chore.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A platform-specific container that renders the live camera preview.
 *
 * This must be a `@Composable` because it participates in the Compose UI tree
 * to display the camera feed (e.g., embedding an Android `PreviewView` or an
 * iOS `UIViewController` directly into the composition).
 */
@Composable
expect fun CameraCaptureView(
    onImageCaptured: (ByteArray) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
