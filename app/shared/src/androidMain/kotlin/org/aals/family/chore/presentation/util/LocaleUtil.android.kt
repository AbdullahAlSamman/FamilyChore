package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.aals.family.chore.core.domain.model.AppLanguage
import java.util.Locale

@Composable
actual fun SetLocale(language: AppLanguage) {
    val context = LocalContext.current
    val locale = Locale(language.isoCode)
    
    remember(language) {
        val resources = context.resources
        val config = resources.configuration
        if (config.locales[0] != locale) {
            Locale.setDefault(locale)
            val newConfig = android.content.res.Configuration(config).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            @Suppress("DEPRECATION")
            resources.updateConfiguration(newConfig, resources.displayMetrics)
        }
        true
    }
}
