package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

sealed interface AuthValidationError : Error {
    enum class FamilyNameError : AuthValidationError {
        EMPTY,
        TOO_SHORT,
        INVALID_CHARACTERS
    }
    enum class NicknameError : AuthValidationError {
        EMPTY,
        TOO_SHORT,
        INVALID_CHARACTERS
    }
    enum class PinError : AuthValidationError {
        INVALID_LENGTH,
        NOT_DIGITS
    }
}

object AuthValidator {
    
    fun validateFamilyName(name: String): AuthValidationError.FamilyNameError? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> AuthValidationError.FamilyNameError.EMPTY
            trimmed.length < 3 -> AuthValidationError.FamilyNameError.TOO_SHORT
            !trimmed.all { it.isLetterOrDigit() || it.isWhitespace() } -> AuthValidationError.FamilyNameError.INVALID_CHARACTERS
            trimmed.none { it.isLetter() } -> AuthValidationError.FamilyNameError.INVALID_CHARACTERS
            else -> null
        }
    }
    
    fun validateNickname(nickname: String): AuthValidationError.NicknameError? {
        val trimmed = nickname.trim()
        return when {
            trimmed.isBlank() -> AuthValidationError.NicknameError.EMPTY
            trimmed.length < 2 -> AuthValidationError.NicknameError.TOO_SHORT
            !trimmed.all { it.isLetterOrDigit() || it.isWhitespace() } -> AuthValidationError.NicknameError.INVALID_CHARACTERS
            trimmed.none { it.isLetter() } -> AuthValidationError.NicknameError.INVALID_CHARACTERS
            else -> null
        }
    }
    
    fun validatePin(pin: String): AuthValidationError.PinError? {
        return when {
            pin.length != 4 -> AuthValidationError.PinError.INVALID_LENGTH
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
