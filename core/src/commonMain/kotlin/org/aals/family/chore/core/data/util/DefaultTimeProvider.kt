package org.aals.family.chore.core.data.util

import kotlinx.datetime.Clock
import org.aals.family.chore.core.domain.util.TimeProvider

class DefaultTimeProvider : TimeProvider {
    override fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
