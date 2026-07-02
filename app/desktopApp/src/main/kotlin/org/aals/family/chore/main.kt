package org.aals.family.chore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.aals.family.chore.core.domain.util.LoggingInitializer
import org.aals.family.chore.di.initKoin

fun main() {
    LoggingInitializer.init()
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Family Chore ",
        ) {
            App()
        }
    }
}
