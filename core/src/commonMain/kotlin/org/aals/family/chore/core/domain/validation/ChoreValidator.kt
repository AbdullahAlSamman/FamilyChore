package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

/**
 * Validation rules for chore fields.
 *
 * Rules (MEMORY.md "Field Validation Rules"):
 * - `name`: letters (any language) + numbers (+ spaces), max 80, at least one letter.
 * - `points`: integer, > 0 and <= 1000.
 * - `assignee`: a non-blank member id is required.
 */
sealed interface ChoreValidationError : Error {
    enum class NameError : ChoreValidationError {
        EMPTY,
        TOO_SHORT,
        TOO_LONG,
        INVALID_CHARACTERS
    }
    enum class PointsError : ChoreValidationError {
        INVALID_NUMBER,
        NEGATIVE,
        ZERO,
        TOO_HIGH
    }
    enum class AssigneeError : ChoreValidationError {
        MISSING
    }
}

object ChoreValidator {

    const val NAME_MAX_LENGTH = 80
    const val POINTS_MAX = 1000

    private const val NAME_MIN_LENGTH = 3

    /** Validates a chore name: letters/numbers (+ spaces), 3-80 length, at least one letter. */
    fun validateName(name: String): ChoreValidationError.NameError? {
        val trimmedName = name.trim()
        return when {
            trimmedName.isBlank() -> ChoreValidationError.NameError.EMPTY
            trimmedName.length < NAME_MIN_LENGTH -> ChoreValidationError.NameError.TOO_SHORT
            trimmedName.length > NAME_MAX_LENGTH -> ChoreValidationError.NameError.TOO_LONG
            !trimmedName.all { it.isLetterOrDigit() || it.isWhitespace() } -> ChoreValidationError.NameError.INVALID_CHARACTERS
            trimmedName.none { it.isLetter() && !it.isWhitespace() } -> ChoreValidationError.NameError.INVALID_CHARACTERS // at least one letter
            else -> null
        }
    }

    /** Validates points: must be a positive integer between 1 and 1000. */
    fun validatePoints(points: Int): ChoreValidationError.PointsError? {
        return when {
            points < 0 -> ChoreValidationError.PointsError.NEGATIVE
            points == 0 -> ChoreValidationError.PointsError.ZERO
            points > POINTS_MAX -> ChoreValidationError.PointsError.TOO_HIGH
            else -> null
        }
    }

    fun validateAssignee(assigneeId: String?): ChoreValidationError.AssigneeError? {
        return if (assigneeId.isNullOrBlank()) {
            ChoreValidationError.AssigneeError.MISSING
        } else {
            null
        }
    }
}
