package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

sealed interface ChoreValidationError : Error {
    enum class NameError : ChoreValidationError {
        EMPTY,
        TOO_SHORT,
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
    
    fun validateName(name: String): ChoreValidationError.NameError? {
        val trimmedName = name.trim()
        return when {
            trimmedName.isBlank() -> ChoreValidationError.NameError.EMPTY
            trimmedName.length < 3 -> ChoreValidationError.NameError.TOO_SHORT
            !trimmedName.all { it.isLetterOrDigit() || it.isWhitespace() } -> ChoreValidationError.NameError.INVALID_CHARACTERS
            trimmedName.none { it.isLetter() } -> ChoreValidationError.NameError.INVALID_CHARACTERS // Requirement: "should accept letters" usually means at least one letter
            else -> null
        }
    }
    
    fun validatePoints(points: Int): ChoreValidationError.PointsError? {
        return when {
            points < 0 -> ChoreValidationError.PointsError.NEGATIVE
            points == 0 -> ChoreValidationError.PointsError.ZERO
            points > 1000 -> ChoreValidationError.PointsError.TOO_HIGH
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
