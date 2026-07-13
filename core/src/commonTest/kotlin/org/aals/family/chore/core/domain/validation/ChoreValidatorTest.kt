package org.aals.family.chore.core.domain.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ChoreValidatorTest {

    @Test
    fun `validateName returns null for valid name`() {
        val result = ChoreValidator.validateName("Clean Room")
        assertThat(result).isNull()
    }

    @Test
    fun `validateName returns EMPTY for blank name`() {
        val result = ChoreValidator.validateName("")
        assertThat(result).isEqualTo(ChoreValidationError.NameError.EMPTY)
    }

    @Test
    fun `validateName returns TOO_SHORT for short name`() {
        val result = ChoreValidator.validateName("AB")
        assertThat(result).isEqualTo(ChoreValidationError.NameError.TOO_SHORT)
    }

    @Test
    fun `validateName returns INVALID_CHARACTERS for special characters`() {
        val result = ChoreValidator.validateName("Clean!")
        assertThat(result).isEqualTo(ChoreValidationError.NameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateName returns INVALID_CHARACTERS for numbers only`() {
        val result = ChoreValidator.validateName("12345")
        assertThat(result).isEqualTo(ChoreValidationError.NameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateName returns null for name with numbers and letters`() {
        val result = ChoreValidator.validateName("Chore 101")
        assertThat(result).isNull()
    }

    @Test
    fun `validatePoints returns null for valid points`() {
        val result = ChoreValidator.validatePoints(50)
        assertThat(result).isNull()
    }

    @Test
    fun `validatePoints returns ZERO for 0 points`() {
        val result = ChoreValidator.validatePoints(0)
        assertThat(result).isEqualTo(ChoreValidationError.PointsError.ZERO)
    }

    @Test
    fun `validatePoints returns NEGATIVE for negative points`() {
        val result = ChoreValidator.validatePoints(-10)
        assertThat(result).isEqualTo(ChoreValidationError.PointsError.NEGATIVE)
    }

    @Test
    fun `validateAssignee returns null for valid assignee`() {
        val result = ChoreValidator.validateAssignee("user123")
        assertThat(result).isNull()
    }

    @Test
    fun `validateAssignee returns MISSING for null assignee`() {
        val result = ChoreValidator.validateAssignee(null)
        assertThat(result).isEqualTo(ChoreValidationError.AssigneeError.MISSING)
    }
}
