package org.aals.family.chore.core.data.remote.dto

import kotlinx.serialization.Serializable
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole

@Serializable
data class CreateFamilyRequest(
    val familyName: String,
    val parentNickname: String
)

@Serializable
data class CreateFamilyResponse(
    val familyId: String,
    val parentUser: User,
    val token: String
)

@Serializable
data class GeneratePairingTokenRequest(
    val familyId: String
)

@Serializable
data class GeneratePairingTokenResponse(
    val pairingToken: String
)

@Serializable
data class PairingUsersResponse(
    val familyName: String?,
    val users: List<User>
)

@Serializable
data class ConfirmPairingRequest(
    val pairingToken: String,
    val userId: String
)

@Serializable
data class ConfirmPairingResponse(
    val familyId: String,
    val user: User,
    val token: String
)

@Serializable
data class SetupPinRequest(
    val userId: String,
    val pin: String
)

@Serializable
data class VerifyPinRequest(
    val userId: String,
    val pin: String
)

@Serializable
data class AddUserRequest(
    val nickname: String,
    val role: UserRole,
    val requiresPin: Boolean = true
)

@Serializable
data class UpdateUserSettingsRequest(
    val requiresPin: Boolean
)
