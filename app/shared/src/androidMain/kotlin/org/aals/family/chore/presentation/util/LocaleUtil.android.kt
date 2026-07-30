package org.aals.family.chore.presentation.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import org.aals.family.chore.core.domain.model.AppLanguage
import java.util.Locale

@Composable
actual fun SetLocale(language: AppLanguage) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = Locale(language.isoCode)
    
    LaunchedEffect(language) {
        if (configuration.locales[0] != locale) {
            Locale.setDefault(locale)
            val newConfig = Configuration(configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            
            val resources = context.resources
            @Suppress("DEPRECATION")
            resources.updateConfiguration(newConfig, resources.displayMetrics)
            
            // Also update Application context to ensure global strings (like notifications or shortcuts) sync
            val appResources = context.applicationContext.resources
            @Suppress("DEPRECATION")
            appResources.updateConfiguration(newConfig, appResources.displayMetrics)
        }
    }
}
