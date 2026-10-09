package org.aals.family.chore.core.domain.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ChoreValidatorTest {

    // --- validateName ---

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
    fun `validateName returns TOO_LONG for 81 char name`() {
        val result = ChoreValidator.validateName("a".repeat(81))
        assertThat(result).isEqualTo(ChoreValidationError.NameError.TOO_LONG)
    }

    @Test
    fun `validateName returns null for 80 char name`() {
        val result = ChoreValidator.validateName("a".repeat(80))
        assertThat(result).isNull()
    }

    @Test
    fun `validateName returns INVALID_CHARACTERS for special characters`() {
        val result = ChoreValidator.validateName("Clean!")
        assertThat(result).isEqualTo(ChoreValidationError.NameError.INVALID_CHARACTERS)
    }

    @Test
    fun `validateName accepts non-latin letters`() {
        val result = ChoreValidator.validateName("تنظيف الغرفة")
        assertThat(result).isNull()
    }

    @Test
    fun `validateName returns null for name with numbers and letters`() {
        val result = ChoreValidator.validateName("Chore 101")
        assertThat(result).isNull()
    }

    // --- validatePoints ---

    @Test
    fun `validatePoints returns null for valid points`() {
        val result = ChoreValidator.validatePoints(50)
        assertThat(result).isNull()
    }

    @Test
    fun `validatePoints returns null for 1 point`() {
        val result = ChoreValidator.validatePoints(1)
        assertThat(result).isNull()
    }

    @Test
    fun `validatePoints returns null for 1000 points`() {
        val result = ChoreValidator.validatePoints(1000)
        assertThat(result).isNull()
    }

    @Test
    fun `validatePoints returns TOO_HIGH for 1001 points`() {
        val result = ChoreValidator.validatePoints(1001)
        assertThat(result).isEqualTo(ChoreValidationError.PointsError.TOO_HIGH)
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

    // --- validateAssignee ---

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

    @Test
    fun `validateAssignee returns MISSING for blank assignee`() {
        val result = ChoreValidator.validateAssignee("")
        assertThat(result).isEqualTo(ChoreValidationError.AssigneeError.MISSING)
    }
}
