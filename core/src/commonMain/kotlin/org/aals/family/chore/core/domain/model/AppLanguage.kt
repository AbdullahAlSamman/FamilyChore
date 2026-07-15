package org.aals.family.chore.core.domain.model

enum class AppLanguage(val isoCode: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    ARABIC("ar", "العربية", true)
}
