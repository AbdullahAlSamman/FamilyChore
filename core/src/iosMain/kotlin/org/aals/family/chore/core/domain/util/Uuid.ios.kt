package org.aals.family.chore.core.domain.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
