package org.aals.family.chore.core.domain.repository

import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.EmptyResult
import org.aals.family.chore.core.domain.util.Result

/**
 * Stores and retrieves the local (per-device) profile picture reference for a
 * user. Pictures are local-only and never uploaded to the server.
 *
 * A "path" stored in the DB may be:
 *  - a `file://...` absolute path to a copied image in app-private storage, or
 *  - a bundled preset marker like `bundled:<name>` referencing a compose resource.
 */
interface ProfilePictureRepository {
    /** Returns the stored picture reference for [userId], or null if none. */
    suspend fun getProfilePicture(userId: String): String?
    suspend fun saveCustomPicture(userId: String, imageBytes: ByteArray): EmptyResult<DataError.Local>
    /** Stores a custom image (from gallery/camera) copied into app-private storage. */
    suspend fun saveCustomPicture(userId: String, sourcePath: String): EmptyResult<DataError.Local>
    /** Stores a bundled preset avatar reference for [userId]. */
    suspend fun savePreset(userId: String, presetKey: String): EmptyResult<DataError.Local>
}
