package org.aals.family.chore.auth

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class PairingManager {
    private val tokens = ConcurrentHashMap<String, PairingSession>()

    data class PairingSession(
        val familyId: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    fun generateToken(familyId: String): String {
        val token = UUID.randomUUID().toString().take(6).uppercase() // Short code for QR
        tokens[token] = PairingSession(familyId)
        return token
    }

    fun validateToken(token: String): String? {
        val session = tokens[token] ?: return null
        // Expire after 10 minutes
        if (System.currentTimeMillis() - session.createdAt > 10.minutes.inWholeMilliseconds) {
            tokens.remove(token)
            return null
        }
        return session.familyId
    }

    fun consumeToken(token: String) {
        tokens.remove(token)
    }
}
