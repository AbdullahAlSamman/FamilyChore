package org.aals.family.chore.feature.auth.presentation.qr_scanner

import org.aals.family.chore.core.presentation.UiText

/**
 * Represents the possible states of the QR Scanner screen.
 * Using a sealed interface ensures that we only handle valid scanner states.
 */
sealed interface QrScannerState {
    /** Camera permission is not granted yet. */
    data class NoPermission(val permissionRequestCount: Int = 0) : QrScannerState
    
    /** The scanner is active and looking for a QR code. */
    data class Scanning(val error: UiText? = null) : QrScannerState
}
