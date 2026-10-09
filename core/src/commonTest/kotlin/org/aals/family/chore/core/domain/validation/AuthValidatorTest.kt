package org.aals.family.chore.core.domain.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class AuthValidatorTest {

    // --- validateFamilyName ---

    @Test
    fun `validateFamilyName returns null for valid name`() {
        assertThat(AuthValidator.validateFamilyName("The Smiths")).isNull()
    }

    @Test
    fun `validateFamilyName returns EMPTY for blank name`() {
        assertThat(AuthValidator.validateFamilyName("")).isEqualTo(AuthValidationError.FamilyNameError.EMPTY)
    }

    @Test
    fun `validateFamilyName returns TOO_SHORT for 2 char name`() {
        assertThat(AuthValidator.validateFamilyName("AB")).isEqualTo(AuthValidationError.FamilyNameError.TOO_SHORT)
    }

    @Test
    fun `validateFamilyName returns TOO_LONG for 81 char name`() {
        assertThat(AuthValidator.validateFamilyName("a".repeat(81))).isEqualTo(AuthValidationError.FamilyNameError.TOO_LONG)
    }

    @Test
    fun `validateFamilyName returns null for 80 char name`() {
        assertThat(AuthValidator.validateFamilyName("a".repeat(80))).isNull()
    }

    @Test
    fun `validateFamilyName accepts non-latin letters`() {
        // Arabic letters and spaces
        assertThat(AuthValidator.validateFamilyName("عائلة السميث")).isNull()
    }

    @Test
    fun `validateFamilyName returns INVALID_CHARACTERS for digits`() {
        assertThat(AuthValidator.validateFamilyName("Family 123")).isEqualTo(AuthValidationError.FamilyNameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateFamilyName returns INVALID_CHARACTERS for punctuation`() {
        assertThat(AuthValidator.validateFamilyName("The Smith!")).isEqualTo(AuthValidationError.FamilyNameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateFamilyName returns INVALID_CHARACTERS for numbers only`() {
        assertThat(AuthValidator.validateFamilyName("12345")).isEqualTo(AuthValidationError.FamilyNameError.INVALID_CHARACTERS)
    }

    // --- validateNickname ---

    @Test
    fun `validateNickname returns null for valid nickname`() {
        assertThat(AuthValidator.validateNickname("Dad")).isNull()
    }

    @Test
    fun `validateNickname returns TOO_SHORT for 1 char nickname`() {
        assertThat(AuthValidator.validateNickname("D")).isEqualTo(AuthValidationError.NicknameError.TOO_SHORT)
    }

    @Test
    fun `validateNickname returns TOO_LONG for 41 char nickname`() {
        assertThat(AuthValidator.validateNickname("a".repeat(41))).isEqualTo(AuthValidationError.NicknameError.TOO_LONG)
    }

    @Test
    fun `validateNickname returns null for 40 char nickname`() {
        assertThat(AuthValidator.validateNickname("a".repeat(40))).isNull()
    }

    @Test
    fun `validateNickname accepts non-latin letters`() {
        assertThat(AuthValidator.validateNickname("أب")).isNull()
    }

    @Test
    fun `validateNickname returns INVALID_CHARACTERS for digits`() {
        assertThat(AuthValidator.validateNickname("Dad2")).isEqualTo(AuthValidationError.NicknameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateNickname returns INVALID_CHARACTERS for special chars`() {
        assertThat(AuthValidator.validateNickname("Dad!")).isEqualTo(AuthValidationError.NicknameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateNickname returns INVALID_CHARACTERS for spaces`() {
        assertThat(AuthValidator.validateNickname("Dad O")).isEqualTo(AuthValidationError.NicknameError.INVALID_CHARACTERS)
    }

    // --- validatePin ---

    @Test
    fun `validatePin returns null for 4 digit pin`() {
        assertThat(AuthValidator.validatePin("1234")).isNull()
    }

    @Test
    fun `validatePin returns null for 5 digit pin`() {
        assertThat(AuthValidator.validatePin("12345")).isNull()
    }

    @Test
    fun `validatePin returns null for 8 digit pin`() {
        assertThat(AuthValidator.validatePin("12345678")).isNull()
    }

    @Test
    fun `validatePin returns INVALID_LENGTH for 3 digit pin`() {
        assertThat(AuthValidator.validatePin("123")).isEqualTo(AuthValidationError.PinError.INVALID_LENGTH)
    }

    @Test
    fun `validatePin returns INVALID_LENGTH for 9 digit pin`() {
        assertThat(AuthValidator.validatePin("123456789")).isEqualTo(AuthValidationError.PinError.INVALID_LENGTH)
    }

    @Test
    fun `validatePin returns NOT_DIGITS for alpha pin`() {
        assertThat(AuthValidator.validatePin("12a4")).isEqualTo(AuthValidationError.PinError.NOT_DIGITS)
    }

    @Test
    fun `validatePin returns INVALID_LENGTH for empty pin`() {
        assertThat(AuthValidator.validatePin("")).isEqualTo(AuthValidationError.PinError.INVALID_LENGTH)
    }
}
