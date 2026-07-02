package org.aals.family.chore

import androidx.compose.ui.window.ComposeUIViewController
import org.aals.family.chore.core.domain.util.LoggingInitializer

fun MainViewController() = ComposeUIViewController {
    LoggingInitializer.init()
    App()
}
