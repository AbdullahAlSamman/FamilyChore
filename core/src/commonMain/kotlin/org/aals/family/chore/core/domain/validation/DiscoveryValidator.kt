package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

/**
 * Validation rules for server discovery/connection fields.
 *
 * Rules (MEMORY.md "Field Validation Rules"):
 * - `manualUrl`: IPv4 with a port, e.g. `192.168.1.2:2222`.
 *   An optional `http://` or `https://` scheme is also accepted.
 */
sealed interface DiscoveryValidationError : Error {
    enum class UrlError : DiscoveryValidationError {
        EMPTY,
        INVALID_FORMAT
    }
}

object DiscoveryValidator {

    const val DEFAULT_PORT = 8080
    const val MAX_PORT = 65535

    // Matches an IPv4 address (each octet 0-255), optionally with a scheme prefix.
    private val IPV4_REGEX = Regex(
        """^(?:https?://)?""" +
            """(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\.""" +
            """(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\.""" +
            """(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\.""" +
            """(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$"""
    )

    /**
     * Validates a URL of the form `IPv4:port` (e.g. `192.168.1.2:2222`).
     * An optional `http://`/`https://` scheme is accepted. Port must be 1-65535.
     */
    fun validateUrl(url: String): DiscoveryValidationError.UrlError? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return DiscoveryValidationError.UrlError.EMPTY

        // Strip optional scheme before parsing.
        val withoutScheme = trimmed
            .removePrefix("https://")
            .removePrefix("http://")

        // Must be exactly IPv4:port with nothing else (no path/query).
        if (withoutScheme.contains('/')) return DiscoveryValidationError.UrlError.INVALID_FORMAT

        val lastColon = withoutScheme.lastIndexOf(':')
        if (lastColon < 0) return DiscoveryValidationError.UrlError.INVALID_FORMAT

        val host = withoutScheme.substring(0, lastColon)
        val portStr = withoutScheme.substring(lastColon + 1)

        val port = portStr.toIntOrNull()
            ?: return DiscoveryValidationError.UrlError.INVALID_FORMAT

        if (port !in 1..MAX_PORT) return DiscoveryValidationError.UrlError.INVALID_FORMAT
        if (!IPV4_REGEX.matches(host)) return DiscoveryValidationError.UrlError.INVALID_FORMAT

        return null
    }

    /** Ensures a host string has a scheme and port, e.g. "192.168.1.2" → "http://192.168.1.2:8080". */
    fun normalizeServerUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
        // Append default port if none present (last colon before a digit sequence).
        return if (Regex(""":\d+$""").containsMatchIn(withScheme)) {
            withScheme
        } else {
            "$withScheme:$DEFAULT_PORT"
        }
    }
}
