package com.fer.projekt.solutionproviders

import org.springframework.web.multipart.MultipartFile
import java.io.File

interface RepositorySolutionProvider {
    fun saveRepoToResources(
        repoUrl: String?,
        branch: String?,
        folderPath: String,
        zipFile: MultipartFile?,
        typeOfResource: String,
        filter: List<String>?,
        )
    fun getRepoAsFile(fileKey: String, resourceType: String): File
    fun deleteRepo(fileKey: String, resourceType: String)
}