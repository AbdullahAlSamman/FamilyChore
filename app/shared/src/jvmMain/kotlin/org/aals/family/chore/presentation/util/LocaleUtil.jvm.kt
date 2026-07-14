package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import org.aals.family.chore.core.domain.model.AppLanguage
import java.util.Locale

@Composable
actual fun SetLocale(language: AppLanguage) {
    val locale = Locale(language.isoCode)
    Locale.setDefault(locale)
}
