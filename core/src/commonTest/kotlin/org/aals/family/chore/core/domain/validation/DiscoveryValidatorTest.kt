package org.aals.family.chore.core.domain.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class DiscoveryValidatorTest {

    // --- validateUrl ---

    @Test
    fun `validateUrl returns null for valid IPv4 with port`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2:2222")).isNull()
    }

    @Test
    fun `validateUrl returns null for valid IPv4 with default port`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2:8080")).isNull()
    }

    @Test
    fun `validateUrl accepts scheme prefix`() {
        assertThat(DiscoveryValidator.validateUrl("http://192.168.1.2:2222")).isNull()
        assertThat(DiscoveryValidator.validateUrl("https://10.0.2.2:8080")).isNull()
    }

    @Test
    fun `validateUrl returns null for boundary IPv4 values`() {
        assertThat(DiscoveryValidator.validateUrl("0.0.0.0:2222")).isNull()
        assertThat(DiscoveryValidator.validateUrl("255.255.255.255:2222")).isNull()
    }

    @Test
    fun `validateUrl returns EMPTY for blank`() {
        assertThat(DiscoveryValidator.validateUrl("")).isEqualTo(DiscoveryValidationError.UrlError.EMPTY)
        assertThat(DiscoveryValidator.validateUrl("   ")).isEqualTo(DiscoveryValidationError.UrlError.EMPTY)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for missing port`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for hostname instead of IPv4`() {
        assertThat(DiscoveryValidator.validateUrl("my.server:8080")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for out-of-range IP octet`() {
        assertThat(DiscoveryValidator.validateUrl("256.1.1.1:8080")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for out-of-range port`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2:65536")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for non-numeric port`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2:port")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }

    @Test
    fun `validateUrl returns INVALID_FORMAT for trailing path`() {
        assertThat(DiscoveryValidator.validateUrl("192.168.1.2:8080/path")).isEqualTo(DiscoveryValidationError.UrlError.INVALID_FORMAT)
    }
}
