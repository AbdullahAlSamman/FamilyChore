package org.aals.family.chore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Family Chore ",
    ) {
        App()
    }
}