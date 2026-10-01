package org.aals.family.chore.core.domain.repository

import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.EmptyResult

interface ProfilePictureRepository {
    suspend fun getProfilePicture(userId: String): String?
    suspend fun saveCustomPicture(userId: String, imageBytes: ByteArray): EmptyResult<DataError.Local>
    suspend fun savePreset(userId: String, presetKey: String): EmptyResult<DataError.Local>
}
