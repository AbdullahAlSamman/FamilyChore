package org.aals.family.chore.core.presentation

import familychore.core.generated.resources.Res
import familychore.core.generated.resources.auth_family_name_empty_error
import familychore.core.generated.resources.auth_family_name_invalid_error
import familychore.core.generated.resources.auth_family_name_too_short_error
import familychore.core.generated.resources.auth_nickname_empty_error
import familychore.core.generated.resources.auth_nickname_invalid_error
import familychore.core.generated.resources.auth_nickname_too_short_error
import familychore.core.generated.resources.auth_pin_invalid_length_error
import familychore.core.generated.resources.auth_pin_not_digits_error
import familychore.core.generated.resources.chore_name_empty_error
import familychore.core.generated.resources.chore_name_invalid_error
import familychore.core.generated.resources.chore_name_too_short_error
import familychore.core.generated.resources.chore_points_negative_error
import familychore.core.generated.resources.chore_points_too_high_error
import familychore.core.generated.resources.chore_points_zero_error
import familychore.core.generated.resources.discovery_error_unreachable
import familychore.core.generated.resources.error_invalid_number
import familychore.core.generated.resources.error_unknown
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Error
import org.aals.family.chore.core.domain.validation.AuthValidationError
import org.aals.family.chore.core.domain.validation.ChoreValidationError

fun ChoreValidationError.NameError.toUiText(): UiText {
    return when (this) {
        ChoreValidationError.NameError.EMPTY -> UiText.StringResource(Res.string.chore_name_empty_error)
        ChoreValidationError.NameError.TOO_SHORT -> UiText.StringResource(Res.string.chore_name_too_short_error)
        ChoreValidationError.NameError.INVALID_CHARACTERS -> UiText.StringResource(Res.string.chore_name_invalid_error)
    }
}

fun ChoreValidationError.PointsError.toUiText(): UiText {
    return when (this) {
        ChoreValidationError.PointsError.NEGATIVE -> UiText.StringResource(Res.string.chore_points_negative_error)
        ChoreValidationError.PointsError.ZERO -> UiText.StringResource(Res.string.chore_points_zero_error)
        ChoreValidationError.PointsError.TOO_HIGH -> UiText.StringResource(Res.string.chore_points_too_high_error)
        ChoreValidationError.PointsError.INVALID_NUMBER -> UiText.StringResource(Res.string.error_invalid_number)
    }
}

fun AuthValidationError.FamilyNameError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.FamilyNameError.EMPTY -> UiText.StringResource(Res.string.auth_family_name_empty_error)
        AuthValidationError.FamilyNameError.TOO_SHORT -> UiText.StringResource(Res.string.auth_family_name_too_short_error)
        AuthValidationError.FamilyNameError.INVALID_CHARACTERS -> UiText.StringResource(Res.string.auth_family_name_invalid_error)
    }
}

fun AuthValidationError.NicknameError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.NicknameError.EMPTY -> UiText.StringResource(Res.string.auth_nickname_empty_error)
        AuthValidationError.NicknameError.TOO_SHORT -> UiText.StringResource(Res.string.auth_nickname_too_short_error)
        AuthValidationError.NicknameError.INVALID_CHARACTERS -> UiText.StringResource(Res.string.auth_nickname_invalid_error)
    }
}

fun AuthValidationError.PinError.toUiText(): UiText {
    return when (this) {
        AuthValidationError.PinError.INVALID_LENGTH -> UiText.StringResource(Res.string.auth_pin_invalid_length_error)
        AuthValidationError.PinError.NOT_DIGITS -> UiText.StringResource(Res.string.auth_pin_not_digits_error)
    }
}

fun Error.toUiText(): UiText {
    return when (this) {
        is DataError.Network -> when (this) {
            DataError.Network.NO_INTERNET -> UiText.StringResource(Res.string.discovery_error_unreachable)
            else -> UiText.StringResource(Res.string.error_unknown)
        }
        is DataError.Local -> UiText.StringResource(Res.string.error_unknown)
        else -> UiText.StringResource(Res.string.error_unknown)
    }
}
