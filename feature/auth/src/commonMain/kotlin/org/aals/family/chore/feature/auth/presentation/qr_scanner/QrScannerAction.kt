package org.aals.family.chore.feature.auth.presentation.qr_scanner

sealed interface QrScannerAction {
    data class OnQrCodeScanned(val content: String) : QrScannerAction
    data object OnBackClick : QrScannerAction
    data class OnPermissionResult(val granted: Boolean) : QrScannerAction
    data object OnRetryPermissionClick : QrScannerAction
}
