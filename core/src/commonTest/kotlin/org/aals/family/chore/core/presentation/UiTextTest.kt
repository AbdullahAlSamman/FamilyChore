package org.aals.family.chore.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class UiTextTest {

    @Test
    fun `DynamicString equality`() {
        val uiText1 = UiText.DynamicString("Hello")
        val uiText2 = UiText.DynamicString("Hello")
        assertThat(uiText1).isEqualTo(uiText2)
    }

    @Test
    fun `DynamicString value property`() {
        val uiText = UiText.DynamicString("World")
        assertThat(uiText.value).isEqualTo("World")
    }
}
