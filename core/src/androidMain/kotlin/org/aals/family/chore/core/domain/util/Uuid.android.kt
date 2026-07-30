package org.aals.family.chore.core.domain.util

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
