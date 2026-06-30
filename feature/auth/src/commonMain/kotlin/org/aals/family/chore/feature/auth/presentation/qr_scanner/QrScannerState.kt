package org.aals.family.chore.feature.auth.presentation.qr_scanner

sealed interface QrScannerState {
    data class NoPermission(val permissionRequestCount: Int = 0) : QrScannerState
    data class Scanning(val error: String? = null) : QrScannerState
}
