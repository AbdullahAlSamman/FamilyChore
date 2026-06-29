package org.aals.family.chore.core.domain.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class ResultTest {

    @Test
    fun `map transforms success value`() {
        val result: Result<Int, DataError.Local> = Result.Success(5)
        val mapped = result.map { it * 2 }

        assertThat(mapped).isInstanceOf(Result.Success::class)
        assertThat((mapped as Result.Success).data).isEqualTo(10)
    }

    @Test
    fun `map preserves error`() {
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)
        val mapped = result.map { it * 2 }

        assertThat(mapped).isInstanceOf(Result.Error::class)
        assertThat((mapped as Result.Error).error).isEqualTo(DataError.Local.DISK_FULL)
    }

    @Test
    fun `onSuccess is called for success`() {
        var calledValue: Int? = null
        val result: Result<Int, DataError.Local> = Result.Success(5)
        result.onSuccess { calledValue = it }

        assertThat(calledValue).isEqualTo(5)
    }

    @Test
    fun `onSuccess is NOT called for error`() {
        var called = false
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)
        result.onSuccess { called = true }

        assertThat(called).isEqualTo(false)
    }

    @Test
    fun `onFailure is called for error`() {
        var calledError: DataError.Local? = null
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)
        result.onFailure { calledError = it }

        assertThat(calledError).isEqualTo(DataError.Local.DISK_FULL)
    }

    @Test
    fun `onFailure is NOT called for success`() {
        var called = false
        val result: Result<Int, DataError.Local> = Result.Success(5)
        result.onFailure { called = true }

        assertThat(called).isEqualTo(false)
    }
}
