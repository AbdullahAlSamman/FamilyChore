package org.aals.family.chore.presentation.util

import androidx.compose.runtime.Composable
import org.aals.family.chore.core.domain.model.AppLanguage

@Composable
actual fun SetLocale(language: AppLanguage) {
    // For iOS, locale change usually requires app restart or complex workarounds if not using system settings
    // But for Compose Resources, it should ideally respect a ResourceEnvironment
}
