package com.fer.projekt.fileproviders

import com.fer.projekt.solutionproviders.disallowedExtensions
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File

@Component
class FileUtils {

    private val log = KotlinLogging.logger {}

    fun deleteByPath(path: String) {
        val solutionsDir = File(path)
        if (solutionsDir.exists()) {
            solutionsDir.deleteRecursively()
            log.info { "Deleted solutions for $path" }
        }
    }

    fun filterInsideFolder(folderPath: String, filter: List<String>) {
        filter.forEach{
            val fileToRemove = File(folderPath, it)
            if (fileToRemove.exists()) {
                fileToRemove.deleteRecursively()
                log.info { "Deleted file $it" }
            }
        }
    }

    fun deleteNotNeededFiles(folderToFilter: File) {
        folderToFilter.listFiles()?.forEach { file ->
            if (file == folderToFilter || folderToFilter.startsWith(file) || file.startsWith(folderToFilter)) {
                if (file.isFile && file.extension in disallowedExtensions) {
                    file.delete()
                } else if (file.isDirectory) {
                    deleteNotNeededFiles(file)
                }
            } else {
                file.deleteRecursively()
            }
        }
    }
}