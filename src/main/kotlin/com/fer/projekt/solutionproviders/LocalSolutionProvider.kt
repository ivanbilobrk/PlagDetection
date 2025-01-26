package com.fer.projekt.solutionproviders

import com.fer.projekt.annotations.SolutionProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

@Component
@SolutionProvider("local")
class LocalSolutionProvider: RepositorySolutionProvider {

    val log = KotlinLogging.logger {}

    override fun saveRepoToResources(
        repoUrl: String?,
        branch: String?,
        folderPath: String,
        zipFile: MultipartFile?,
        typeOfResource: String,
        filter: List<String>?
    ) {
        if (resourceTypes.contains(typeOfResource).not()) {
            throw IllegalArgumentException("Invalid type of resource")
        }
        unzipAndSaveFile(zipFile!!.inputStream, folderPath, typeOfResource)
        log.info { "Saved solutions locally, path: $folderPath" }
    }

    override fun getRepoAsFile(fileKey: String, resourceType: String): File {
        val filePrefix = getSavePath(resourceType)
        return File("$filePrefix$fileKey")
    }

    override fun deleteRepo(fileKey: String, resourceType: String) {
        val filePrefix = getSavePath(resourceType)
        val solutionsDir = File("$filePrefix$fileKey")
        if (solutionsDir.exists()) {
            solutionsDir.deleteRecursively()
            log.info { "Deleted solutions for $fileKey" }
        }
    }

    private fun unzipAndSaveFile(
        inputStream: InputStream,
        folderPath: String,
        typeOfResource: String
    ) {
        val destinationDir = File("${getSavePath(typeOfResource)}$folderPath").apply { if (!exists()) mkdirs() }
        val buffer = ByteArray(1024)
        ZipInputStream(inputStream).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val newFile = File(destinationDir, zipEntry.name)
                if (disallowedExtensions.any { zipEntry?.name == it }) {
                    if (zipEntry.isDirectory) {
                        zis.closeEntry()
                        zipEntry = zis.nextEntry
                        while (zipEntry != null && zipEntry.name.startsWith(newFile.name)) {
                            zis.closeEntry()
                            zipEntry = zis.nextEntry
                        }
                    } else {
                        zis.closeEntry()
                        zipEntry = zis.nextEntry
                    }
                    continue
                } else if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    val extensionName = zipEntry.name.substringAfterLast('.').lowercase()
                    if (!disallowedExtensions.contains(".$extensionName")) {
                        if (!newFile.parentFile.exists()) newFile.parentFile.mkdirs()

                        newFile.outputStream().use { fos ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }

                        if (extensionName == "zip") {
                            newFile.inputStream().use { nestedInputStream ->
                                unzipAndSaveFile(nestedInputStream, "${folderPath}/${zipEntry.name.substringBeforeLast(".")}", typeOfResource)
                            }
                            newFile.delete()
                        }
                    }
                }
                zipEntry = zis.nextEntry
            }
        }
    }

    private fun getSavePath(typeOfResource: String): String {
        return when (typeOfResource) {
            "students" -> STUDENTS_PATH
            "repos" -> REPOS_PATH
            "whitelist" -> WHITELIST_PATH
            else -> throw IllegalArgumentException("Invalid type of resource")
        }
    }

    companion object {
        const val WHITELIST_PATH = "src/main/resources/local/whitelist/"
        const val STUDENTS_PATH = "src/main/resources/local/students/"
        const val REPOS_PATH = "src/main/resources/local/repos/"
        val resourceTypes = listOf("students", "repos", "whitelist")
    }
}