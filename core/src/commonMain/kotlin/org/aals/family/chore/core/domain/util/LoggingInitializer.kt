package org.aals.family.chore.core.domain.util

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter

object LoggingInitializer {
    fun init() {
        Logger.setLogWriters(platformLogWriter())
        Logger.setTag("FC-General")
    }

    /**
     * Helper to create a logger with the project standard tag: FC-FileName
     */
    fun createLogger(tag: String): Logger {
        return Logger.withTag("FC-$tag")
    }
}
