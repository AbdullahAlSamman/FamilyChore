package org.aals.family.chore.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        val id: org.jetbrains.compose.resources.StringResource,
        val args: Array<Any> = emptyArray()
    ) : UiText

    class PluralStringResource(
        val id: org.jetbrains.compose.resources.PluralStringResource,
        val quantity: Int,
        val args: Array<Any> = emptyArray()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(id, *args)
            is PluralStringResource -> pluralStringResource(id, quantity, *args)
        }
    }
}
