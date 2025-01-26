package com.fer.projekt.service

import com.fer.projekt.controller.SolutionProviderName
import com.fer.projekt.solutionproviders.RepositorySolutionProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class SolutionUploadService(
    @Qualifier("solutionProvidersMap") private val solutionProvidersMap: Map<SolutionProviderName, RepositorySolutionProvider>
) {

    fun uploadSolution(
        zipFile: MultipartFile?,
        repoUrl: String?,
        branch: String?,
        folderPath: String,
        solutionProvider: SolutionProviderName,
        typeOfResource: String,
        filter: List<String>?
    ): Long {
        val provider = solutionProvidersMap[solutionProvider]
            ?: throw IllegalArgumentException("Solution provider $solutionProvider not found")

        provider.saveRepoToResources(repoUrl, branch, folderPath, zipFile, typeOfResource, filter)

        return 1L
    }
}