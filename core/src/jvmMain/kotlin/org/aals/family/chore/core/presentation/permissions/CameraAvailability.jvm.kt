package org.aals.family.chore.core.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraAvailability(): Boolean {
    return remember { false }
}
