package org.aals.family.chore.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.entity.ChoreEntity

@Dao
interface ChoreDao {
    @Query("SELECT * FROM ChoreEntity WHERE familyId = :familyId")
    fun getChores(familyId: String): Flow<List<ChoreEntity>>

    @Query("SELECT * FROM ChoreEntity WHERE assignedTo = :userId")
    fun getChoresForUser(userId: String): Flow<List<ChoreEntity>>

    @Query("SELECT * FROM ChoreEntity WHERE id = :choreId AND familyId = :familyId")
    suspend fun getChoreById(choreId: String, familyId: String): ChoreEntity?
    
    @Upsert
    suspend fun upsertChore(chore: ChoreEntity)

    @Query("UPDATE ChoreEntity SET iconName = :iconName WHERE id = :choreId AND familyId = :familyId")
    suspend fun updateChoreIcon(choreId: String, familyId: String, iconName: String)

    @Query("SELECT iconName FROM ChoreEntity WHERE id = :choreId AND familyId = :familyId")
    suspend fun getChoreIcon(choreId: String, familyId: String): String?
}
