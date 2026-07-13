package org.aals.family.chore.core.domain.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class AuthValidatorTest {

    @Test
    fun `validateFamilyName returns null for valid name`() {
        assertThat(AuthValidator.validateFamilyName("The Smiths")).isNull()
    }

    @Test
    fun `validateFamilyName returns EMPTY for blank name`() {
        assertThat(AuthValidator.validateFamilyName("")).isEqualTo(AuthValidationError.FamilyNameError.EMPTY)
    }

    @Test
    fun `validateNickname returns null for valid nickname`() {
        assertThat(AuthValidator.validateNickname("Dad")).isNull()
    }

    @Test
    fun `validateNickname returns TOO_SHORT for 1 char nickname`() {
        assertThat(AuthValidator.validateNickname("D")).isEqualTo(AuthValidationError.NicknameError.TOO_SHORT)
    }

    @Test
    fun `validatePin returns null for 4 digit pin`() {
        assertThat(AuthValidator.validatePin("1234")).isNull()
    }

    @Test
    fun `validatePin returns INVALID_LENGTH for 3 digit pin`() {
        assertThat(AuthValidator.validatePin("123")).isEqualTo(AuthValidationError.PinError.INVALID_LENGTH)
    }

    @Test
    fun `validatePin returns NOT_DIGITS for alpha pin`() {
        assertThat(AuthValidator.validatePin("12a4")).isEqualTo(AuthValidationError.PinError.NOT_DIGITS)
    }

    @Test
    fun `validateFamilyName returns INVALID_CHARACTERS for numbers only`() {
        assertThat(AuthValidator.validateFamilyName("12345")).isEqualTo(AuthValidationError.FamilyNameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateNickname returns INVALID_CHARACTERS for special chars`() {
        assertThat(AuthValidator.validateNickname("Dad!")).isEqualTo(AuthValidationError.NicknameError.INVALID_CHARACTERS)
    }
}
