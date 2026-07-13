package org.aals.family.chore.domain.repository

import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus

interface ChoreRepository {
    suspend fun getChoresByFamily(familyId: String): List<Chore>
    suspend fun getChoresByUser(userId: String): List<Chore>
    suspend fun createChore(chore: Chore): Chore
    suspend fun updateChoreStatus(choreId: String, familyId: String, newStatus: ChoreStatus, adminId: String? = null): Boolean
}
