package org.aals.family.chore.feature.auth.presentation.qr_scanner

sealed interface QrScannerEvent {
    data class QrCodeDetected(val serverUrl: String, val pairingToken: String) : QrScannerEvent
    data object NavigateBack : QrScannerEvent
}
