package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

/**
 * Validation rules for auth-related fields.
 *
 * Rules (MEMORY.md "Field Validation Rules"):
 * - `familyName`: letters (any language) + spaces, max 80, at least one letter.
 * - `nickname`: letters (any language) only, max 40, at least one letter.
 * - `pin`: digits only, length 4-8.
 */
sealed interface AuthValidationError : Error {
    enum class FamilyNameError : AuthValidationError {
        EMPTY,
        TOO_SHORT,
        TOO_LONG,
        INVALID_CHARACTERS
    }
    enum class NicknameError : AuthValidationError {
        EMPTY,
        TOO_SHORT,
        TOO_LONG,
        INVALID_CHARACTERS
    }
    enum class PinError : AuthValidationError {
        INVALID_LENGTH,
        NOT_DIGITS
    }
}

object AuthValidator {

    const val FAMILY_NAME_MAX_LENGTH = 80
    const val NICKNAME_MAX_LENGTH = 40
    const val PIN_MIN_LENGTH = 4
    const val PIN_MAX_LENGTH = 8

    private const val FAMILY_NAME_MIN_LENGTH = 3
    private const val NICKNAME_MIN_LENGTH = 2

    /** Validates a family name: letters (any language) + spaces, 3-80 length, at least one letter. */
    fun validateFamilyName(name: String): AuthValidationError.FamilyNameError? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> AuthValidationError.FamilyNameError.EMPTY
            trimmed.length < FAMILY_NAME_MIN_LENGTH -> AuthValidationError.FamilyNameError.TOO_SHORT
            trimmed.length > FAMILY_NAME_MAX_LENGTH -> AuthValidationError.FamilyNameError.TOO_LONG
            !trimmed.all { it.isLetter() || it.isWhitespace() } -> AuthValidationError.FamilyNameError.INVALID_CHARACTERS
            trimmed.none { it.isLetter() } -> AuthValidationError.FamilyNameError.INVALID_CHARACTERS
            else -> null
        }
    }

    /** Validates a nickname: letters (any language) only, 2-40 length, at least one letter. */
    fun validateNickname(nickname: String): AuthValidationError.NicknameError? {
        val trimmed = nickname.trim()
        return when {
            trimmed.isBlank() -> AuthValidationError.NicknameError.EMPTY
            trimmed.length < NICKNAME_MIN_LENGTH -> AuthValidationError.NicknameError.TOO_SHORT
            trimmed.length > NICKNAME_MAX_LENGTH -> AuthValidationError.NicknameError.TOO_LONG
            !trimmed.all { it.isLetter() } -> AuthValidationError.NicknameError.INVALID_CHARACTERS
            trimmed.none { it.isLetter() } -> AuthValidationError.NicknameError.INVALID_CHARACTERS
            else -> null
        }
    }

    /** Validates a PIN: digits only, length between 4 and 8. */
    fun validatePin(pin: String): AuthValidationError.PinError? {
        return when {
            pin.length !in PIN_MIN_LENGTH..PIN_MAX_LENGTH -> AuthValidationError.PinError.INVALID_LENGTH
            !pin.all { it.isDigit() } -> AuthValidationError.PinError.NOT_DIGITS
            else -> null
        }
    }

    fun validateHashedPin(hash: String): AuthValidationError.PinError? {
        val hexChars = "0123456789abcdefABCDEF"
        return when {
            hash.length != 64 -> AuthValidationError.PinError.INVALID_LENGTH
            !hash.all { it in hexChars } -> AuthValidationError.PinError.NOT_DIGITS
            else -> null
        }
    }
}
