package org.aals.family.chore.storage.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aals.family.chore.storage.FileStorage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class LocalDiskFileStorage(baseDirectory: File) : FileStorage {

    private val canonicalBase = baseDirectory.canonicalFile

    init {
        if (!canonicalBase.exists()) {
            canonicalBase.mkdirs()
        }
    }

    override suspend fun saveFile(familyId: String, choreId: String, fileName: String, inputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            val safeFileName = File(fileName).name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val fileIdName = UUID.randomUUID().toString() + "_" + safeFileName
            val familyDir = File(canonicalBase, File(familyId).name)
            val choreDir = File(familyDir, File(choreId).name)
            if (!choreDir.exists()) {
                choreDir.mkdirs()
            }
            val destination = File(choreDir, fileIdName).canonicalFile
            if (!destination.path.startsWith(canonicalBase.path)) {
                throw IllegalArgumentException("Invalid destination file path")
            }
            inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            "${File(familyId).name}/${File(choreId).name}/$fileIdName"
        }
    }

    override suspend fun getFile(fileId: String): InputStream? {
        return withContext(Dispatchers.IO) {
            val parts = fileId.split("/").map { File(it).name }
            val safePath = parts.joinToString(File.separator)
            val targetFile = File(canonicalBase, safePath).canonicalFile
            if (targetFile.exists() && targetFile.isFile && targetFile.path.startsWith(canonicalBase.path)) {
                targetFile.inputStream()
            } else {
                null
            }
        }
    }

    override suspend fun deleteFile(fileId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val parts = fileId.split("/").map { File(it).name }
            val safePath = parts.joinToString(File.separator)
            val targetFile = File(canonicalBase, safePath).canonicalFile
            if (targetFile.exists() && targetFile.isFile && targetFile.path.startsWith(canonicalBase.path)) {
                targetFile.delete()
            } else {
                false
            }
        }
    }
}
