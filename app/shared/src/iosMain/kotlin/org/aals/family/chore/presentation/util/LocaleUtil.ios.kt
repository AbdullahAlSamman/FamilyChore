package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import org.aals.family.chore.core.domain.model.AppLanguage

@Composable
actual fun LanguageProvider(language: AppLanguage, content: @Composable () -> Unit) {
    content()
}
