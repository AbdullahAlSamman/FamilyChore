package org.aals.family.chore.feature.auth.presentation.qr_scanner

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class QrScannerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: QrScannerViewModel
    private lateinit var tokenStorage: FakeTokenStorage

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tokenStorage = FakeTokenStorage()
        viewModel = QrScannerViewModel(tokenStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `valid QR code saves server url and sends event`() = runTest {
        val qrContent = "{\"serverIp\": \"192.168.1.10\", \"token\": \"pairing_token\"}"
        
        viewModel.events.test {
            viewModel.onAction(QrScannerAction.OnQrCodeScanned(qrContent))
            assertThat(awaitItem()).isEqualTo(QrScannerEvent.QrCodeDetected("192.168.1.10", "pairing_token"))
            assertThat(tokenStorage.getServerUrl()).isEqualTo("192.168.1.10")
        }
    }

    @Test
    fun `invalid QR code sets error state`() = runTest {
        val qrContent = "invalid_json"
        
        viewModel.state.test {
            assertThat(awaitItem().error).isEqualTo(null) // Initial state
            viewModel.onAction(QrScannerAction.OnQrCodeScanned(qrContent))
            assertThat(awaitItem().error).isEqualTo("Invalid QR code")
        }
    }

    @Test
    fun `permission result updates state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem().hasCameraPermission).isEqualTo(false)
            viewModel.onAction(QrScannerAction.OnPermissionResult(granted = true))
            assertThat(awaitItem().hasCameraPermission).isEqualTo(true)
        }
    }

    @Test
    fun `back click sends navigate back event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(QrScannerAction.OnBackClick)
            assertThat(awaitItem()).isEqualTo(QrScannerEvent.NavigateBack)
        }
    }
}
