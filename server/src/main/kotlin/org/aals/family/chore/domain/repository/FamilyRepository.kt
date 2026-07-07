package org.aals.family.chore.domain.repository

import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User

interface FamilyRepository {
    suspend fun createFamily(name: String): Family
    suspend fun getFamily(id: String): Family?
    suspend fun addUserToFamily(familyId: String, nickname: String, role: org.aals.family.chore.core.domain.model.UserRole): User
    suspend fun getUsersInFamily(familyId: String): List<User>
    suspend fun getAllFamilies(): List<Family>
    suspend fun getUser(id: String): User?
    suspend fun setPin(userId: String, pin: String)
    suspend fun verifyPin(userId: String, pin: String): Boolean
}
