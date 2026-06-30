package org.aals.family.chore.data.repository

import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.domain.repository.FamilyRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryFamilyRepository : FamilyRepository {
    private val families = ConcurrentHashMap<String, Family>()
    private val users = ConcurrentHashMap<String, MutableList<User>>()

    override suspend fun createFamily(name: String): Family {
        val family = Family(
            id = UUID.randomUUID().toString(),
            name = name
        )
        families[family.id] = family
        users[family.id] = mutableListOf()
        return family
    }

    override suspend fun getFamily(id: String): Family? {
        return families[id]
    }

    override suspend fun addUserToFamily(familyId: String, nickname: String, role: UserRole): User {
        val user = User(
            id = UUID.randomUUID().toString(),
            familyId = familyId,
            nickname = nickname,
            role = role
        )
        users[familyId]?.add(user)
        return user
    }

    override suspend fun getUsersInFamily(familyId: String): List<User> {
        return users[familyId] ?: emptyList()
    }

    override suspend fun getUser(id: String): User? {
        return users.values.flatten().find { it.id == id }
    }
}
