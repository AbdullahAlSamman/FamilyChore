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
    
    @Upsert
    suspend fun upsertChore(chore: ChoreEntity)
}
