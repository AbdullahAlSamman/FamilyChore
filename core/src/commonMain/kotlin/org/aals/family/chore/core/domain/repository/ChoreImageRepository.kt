package org.aals.family.chore.core.domain.repository

import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.EmptyResult
import org.aals.family.chore.core.domain.util.Result

/**
 * Stores and retrieves the local chore images or infographics (icons) for a chore.
 *
 * A "path" stored in the DB (iconName) may be:
 *  - a `file://...` absolute path to a copied image in app-private storage, or
 *  - a bundled preset marker like `bundled:<name>` referencing a compose resource.
 */
interface ChoreImageRepository {
    /** Returns the stored chore image reference for [choreId], or null if none. */
    suspend fun getChoreImage(familyId: String, choreId: String): String?

    /** Saves a custom uploaded image locally and uploads to the server. */
    suspend fun saveCustomPicture(familyId: String, choreId: String, imageBytes: ByteArray): EmptyResult<DataError>

    /** Stores a bundled preset infographic reference for [choreId]. */
    suspend fun savePreset(familyId: String, choreId: String, presetKey: String): EmptyResult<DataError>

    /** Downloads an image from the server and stores it locally. */
    suspend fun downloadImage(familyId: String, choreId: String): EmptyResult<DataError>

    /** Saves raw image bytes to a temporary local file and returns the file path. */
    suspend fun saveTempImage(imageBytes: ByteArray): Result<String, DataError>

    /** Removes a temporary image file if it exists. */
    suspend fun clearTempImage(filePath: String)

    /** Saves a custom picture for [choreId] from a local file path. */
    suspend fun saveCustomPictureFromPath(familyId: String, choreId: String, filePath: String): EmptyResult<DataError>
}
