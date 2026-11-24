package org.aals.family.chore.storage

import java.io.InputStream
import java.io.OutputStream

interface FileStorage {
    suspend fun saveFile(familyId: String, choreId: String, fileName: String, inputStream: InputStream): String
    suspend fun getFile(fileId: String): InputStream?
    suspend fun deleteFile(fileId: String): Boolean
}
