package org.aals.family.chore.core.domain.repository

/**
 * Reports hardware capabilities available on the current device.
 *
 * Kept in the domain layer so business logic (e.g. deciding whether to offer
 * camera capture) can depend on it without reaching into platform APIs.
 */
interface HardwareAvailabilityRepository {
    /** Whether a camera is available for capture on this device. */
    fun hasCamera(): Boolean
}
