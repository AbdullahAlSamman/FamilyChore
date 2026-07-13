package org.aals.family.chore.core.presentation

import org.aals.family.chore.core.domain.validation.AuthValidationError
import org.aals.family.chore.core.domain.validation.ChoreValidationError

fun ChoreValidationError.NameError.toUiText(): UiText {
    return when (this) {
        ChoreValidationError.NameError.EMPTY -> UiText.DynamicString("Chore name cannot be empty")
        ChoreValidationError.NameError.TOO_SHORT -> UiText.DynamicString("Chore name is too short (min 3 chars)")
        ChoreValidationError.NameError.INVALID_CHARACTERS -> UiText.DynamicString("Chore name must contain letters and only alphanumeric characters")
    }
}

fun ChoreValidationError.PointsError.toUiText(): UiText {
    return when (this) {
        ChoreValidationError.PointsError.NEGATIVE -> UiText.DynamicString("Points cannot be negative")
        ChoreValidationError.PointsError.ZERO -> UiText.DynamicString("Points must be greater than zero")
        ChoreValidationError.PointsError.TOO_HIGH -> UiText.DynamicString("Points too high (max 1000)")
        ChoreValidationError.PointsError.INVALID_NUMBER -> UiText.DynamicString("Invalid number")
    }
}

fun AuthValidationError.FamilyNameError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.FamilyNameError.EMPTY -> UiText.DynamicString("Family name cannot be empty")
        AuthValidationError.FamilyNameError.TOO_SHORT -> UiText.DynamicString("Family name is too short (min 3 chars)")
        AuthValidationError.FamilyNameError.INVALID_CHARACTERS -> UiText.DynamicString("Family name must contain letters and only alphanumeric characters")
    }
}

fun AuthValidationError.NicknameError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.NicknameError.EMPTY -> UiText.DynamicString("Nickname cannot be empty")
        AuthValidationError.NicknameError.TOO_SHORT -> UiText.DynamicString("Nickname is too short (min 2 chars)")
        AuthValidationError.NicknameError.INVALID_CHARACTERS -> UiText.DynamicString("Nickname must contain letters and only alphanumeric characters")
    }
}

fun AuthValidationError.PinError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.PinError.INVALID_LENGTH -> UiText.DynamicString("PIN must be 4 digits")
        AuthValidationError.PinError.NOT_DIGITS -> UiText.DynamicString("PIN must contain only digits")
    }
}
