package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import org.aals.family.chore.core.domain.model.AppLanguage
import java.util.Locale

@Composable
actual fun SetLocale(language: AppLanguage) {
    val context = LocalContext.current
    SideEffect {
        val locale = Locale(language.isoCode)
        if (Locale.getDefault() != locale) {
            Locale.setDefault(locale)
            val resources = context.resources
            val config = resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}
