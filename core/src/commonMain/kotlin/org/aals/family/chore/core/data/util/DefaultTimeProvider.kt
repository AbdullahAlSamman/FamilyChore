package org.aals.family.chore.core.data.util

import org.aals.family.chore.core.domain.util.TimeProvider
import kotlin.time.Clock

class DefaultTimeProvider : TimeProvider {
    override fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
