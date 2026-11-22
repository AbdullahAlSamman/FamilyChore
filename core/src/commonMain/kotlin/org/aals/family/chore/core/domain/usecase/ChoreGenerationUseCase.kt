package org.aals.family.chore.core.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.model.Recurrence
import org.aals.family.chore.core.domain.repository.ChoreRepository
import org.aals.family.chore.core.domain.util.TimeProvider

class ChoreGenerationUseCase(
    private val choreRepository: ChoreRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(familyId: String) {
        val chores = choreRepository.getChoresForFamily(familyId).first()
        val now = timeProvider.now()

        val instant = Instant.fromEpochMilliseconds(now)
        val tz = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(tz)

        val isoDayOfWeek = localDateTime.dayOfWeek.ordinal + 1 // 1=Monday, 7=Sunday

        val startOfDay = kotlinx.datetime.LocalDateTime(
            localDateTime.year, localDateTime.month, localDateTime.dayOfMonth, 0, 0, 0
        )
        val startOfDayMillis = startOfDay.toInstant(tz).toEpochMilliseconds()
        val tomorrowSample = Instant.fromEpochMilliseconds(startOfDayMillis + 28 * 3600 * 1000L).toLocalDateTime(tz)
        val startOfTomorrow = kotlinx.datetime.LocalDateTime(
            tomorrowSample.year, tomorrowSample.month, tomorrowSample.dayOfMonth, 0, 0, 0
        )
        val startOfTomorrowMillis = startOfTomorrow.toInstant(tz).toEpochMilliseconds()

        val templates = chores.filter { it.parentChoreId == null && it.recurrence != Recurrence.NONE && it.recurrence != Recurrence.CUSTOM }
        val instances = chores.filter { it.parentChoreId != null }

        // 1. Mark past pending/in-progress instances as REJECTED and apply penalty
        instances.filter {
            (it.status == ChoreStatus.PENDING || it.status == ChoreStatus.IN_PROGRESS || it.status == ChoreStatus.PENDING_APPROVAL) &&
            (it.dueDate ?: 0L) < startOfDayMillis
        }.forEach { missedInstance ->
            choreRepository.updateChoreStatus(
                familyId = familyId,
                choreId = missedInstance.id,
                newStatus = ChoreStatus.REJECTED,
                adminId = "system"
            )
        }

        // 2. Generate instances for today and tomorrow if they don't exist
        templates.forEach { template ->
            val isActiveToday = template.activeDays.isEmpty() || template.activeDays.contains(isoDayOfWeek)
            val tomorrowIsoDayOfWeek = if (isoDayOfWeek == 7) 1 else isoDayOfWeek + 1
            val isActiveTomorrow = template.activeDays.isEmpty() || template.activeDays.contains(tomorrowIsoDayOfWeek)

            if (isActiveToday) {
                val hasInstanceToday = instances.any { it.parentChoreId == template.id && it.dueDate == startOfDayMillis }
                if (!hasInstanceToday) {
                    val newInstance = template.copy(
                        id = "${template.id}_$startOfDayMillis",
                        parentChoreId = template.id,
                        status = ChoreStatus.PENDING,
                        dueDate = startOfDayMillis,
                        createdAt = now,
                        updatedAt = now
                    )
                    choreRepository.createChore(newInstance)
                }
            }

            if (isActiveTomorrow) {
                val hasInstanceTomorrow = instances.any { it.parentChoreId == template.id && it.dueDate == startOfTomorrowMillis }
                if (!hasInstanceTomorrow) {
                    val newInstance = template.copy(
                        id = "${template.id}_$startOfTomorrowMillis",
                        parentChoreId = template.id,
                        status = ChoreStatus.PENDING,
                        dueDate = startOfTomorrowMillis,
                        createdAt = now,
                        updatedAt = now
                    )
                    choreRepository.createChore(newInstance)
                }
            }
        }
    }
}
