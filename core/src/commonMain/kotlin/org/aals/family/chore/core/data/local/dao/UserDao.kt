package org.aals.family.chore.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity WHERE familyId = :familyId")
    fun getUsers(familyId: String): Flow<List<UserEntity>>
    
    @Upsert
    suspend fun upsertUser(user: UserEntity)
}
