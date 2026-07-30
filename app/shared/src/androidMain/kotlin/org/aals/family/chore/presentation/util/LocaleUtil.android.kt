package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.aals.family.chore.core.domain.model.AppLanguage
import java.util.Locale

@Composable
actual fun LanguageProvider(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = Locale(language.isoCode)

    if (configuration.locales[0] != locale) {
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        val resources = context.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        val appResources = context.applicationContext.resources
        @Suppress("DEPRECATION")
        appResources.updateConfiguration(configuration, appResources.displayMetrics)
    }

    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalConfiguration provides configuration
    ) {
        key(language) {
            content()
        }
    }
}
