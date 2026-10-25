package org.aals.family.chore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

interface ChoreRepository {
    fun getChoresForFamily(familyId: String): Flow<List<Chore>>
    fun getChoresForUser(userId: String): Flow<List<Chore>>
    
    suspend fun createChore(chore: Chore): Result<Unit, DataError>
    suspend fun uploadPhotoProof(
        familyId: String,
        choreId: String,
        fileName: String,
        photoBytes: ByteArray
    ): Result<String, DataError>
    suspend fun updateChoreStatus(
        familyId: String,
        choreId: String, 
        newStatus: ChoreStatus, 
        adminId: String? = null // Optional adminId if approval is needed
    ): Result<Unit, DataError>
    
    suspend fun syncChores(familyId: String): Result<Unit, DataError>
    
    fun observeRemoteChanges(familyId: String): Flow<Unit>
}
