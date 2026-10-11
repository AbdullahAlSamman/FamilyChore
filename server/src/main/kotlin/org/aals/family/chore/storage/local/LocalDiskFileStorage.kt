package org.aals.family.chore.storage.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aals.family.chore.storage.FileStorage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class LocalDiskFileStorage(private val baseDirectory: File) : FileStorage {

    init {
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs()
        }
    }

    override suspend fun saveFile(familyId: String, choreId: String, fileName: String, inputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            val fileId = UUID.randomUUID().toString() + "_" + fileName
            val familyDir = File(baseDirectory, familyId)
            val choreDir = File(familyDir, choreId)
            if (!choreDir.exists()) {
                choreDir.mkdirs()
            }
            val destination = File(choreDir, fileId)
            inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            // Return relative path as ID for simplicity
            "$familyId/$choreId/$fileId"
        }
    }

    override suspend fun getFile(fileId: String): InputStream? {
        return withContext(Dispatchers.IO) {
            val targetFile = File(baseDirectory, fileId)
            if (targetFile.exists() && targetFile.isFile) {
                targetFile.inputStream()
            } else {
                null
            }
        }
    }

    override suspend fun deleteFile(fileId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val targetFile = File(baseDirectory, fileId)
            if (targetFile.exists() && targetFile.isFile) {
                targetFile.delete()
            } else {
                false
            }
        }
    }
}
