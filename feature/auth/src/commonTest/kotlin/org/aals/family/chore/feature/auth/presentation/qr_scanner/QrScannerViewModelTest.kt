package org.aals.family.chore.feature.auth.presentation.qr_scanner

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.qr_error_invalid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrScannerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: QrScannerViewModel
    private lateinit var tokenStorage: FakeTokenStorage

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tokenStorage = FakeTokenStorage()
        viewModel = QrScannerViewModel(tokenStorage, Logger.withTag("Test"))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `valid QR code saves all credentials and sends event`() = runTest {
        val qrContent = """
            {
                "serverIp": "192.168.1.10",
                "token": "session_token",
                "familyId": "family_123",
                "userId": "user_456"
            }
        """.trimIndent()
        
        viewModel.onAction(QrScannerAction.OnPermissionResult(granted = true))

        viewModel.events.test {
            viewModel.onAction(QrScannerAction.OnQrCodeScanned(qrContent))
            assertThat(awaitItem()).isEqualTo(QrScannerEvent.QrCodeDetected("192.168.1.10", "session_token"))
            assertThat(tokenStorage.getServerUrl()).isEqualTo("192.168.1.10")
            assertThat(tokenStorage.getToken()).isEqualTo("session_token")
            assertThat(tokenStorage.getFamilyId()).isEqualTo("family_123")
            assertThat(tokenStorage.getUserId()).isEqualTo("user_456")
        }
    }

    @Test
    fun `invalid QR code sets error state`() = runTest {
        val qrContent = "invalid_json"
        
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(QrScannerState.NoPermission()) // Initial state
            viewModel.onAction(QrScannerAction.OnPermissionResult(granted = true))
            assertThat(awaitItem()).isEqualTo(QrScannerState.Scanning())
            
            viewModel.onAction(QrScannerAction.OnQrCodeScanned(qrContent))
            val state = awaitItem() as QrScannerState.Scanning
            val error = state.error as? UiText.StringResource
            assertThat(error?.id).isEqualTo(Res.string.qr_error_invalid)
        }
    }

    @Test
    fun `permission result updates state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(QrScannerState.NoPermission())
            viewModel.onAction(QrScannerAction.OnPermissionResult(granted = true))
            assertThat(awaitItem()).isEqualTo(QrScannerState.Scanning())
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
