package org.aals.family.chore.core.domain.util

import okio.ByteString.Companion.toByteString

/**
 * Extension to hash a string using SHA-256 for secure local storage.
 */
fun String.toSha256(): String {
    return this.encodeToByteArray().toByteString().sha256().hex()
}
