package com.fer.projekt.solutionproviders

import com.fer.projekt.annotations.SolutionProvider
import com.fer.projekt.fileproviders.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.RuntimeException

@Component
@SolutionProvider("git")
class GitRepositorySolutionProvider(
    @Value("\${github.token}") private val token: String,
    private val fileUtils: FileUtils,
): RepositorySolutionProvider {
    val credentialsProvider = UsernamePasswordCredentialsProvider(token, "")

    override fun saveRepoToResources(
        repoUrl: String?,
        branch: String?,
        folderPath: String,
        zipFile: MultipartFile?,
        typeOfResource: String,
        filter: List<String>?,
    ) {
        val repoDir = File("${getSavePath(typeOfResource)}$folderPath")
        if (!repoDir.exists()) {
            repoDir.mkdirs()
        }

        val git = Git.cloneRepository()
            .setURI(repoUrl)
            .setBranch(branch)
            .setDirectory(repoDir)
            .setCredentialsProvider(credentialsProvider)
            .call()
        git.close()

        fileUtils.deleteNotNeededFiles(repoDir)
        fileUtils.filterInsideFolder(repoDir.absolutePath, filter ?: emptyList())
    }

    override fun getRepoAsFile(fileKey: String, resourceType: String): File {
        val filePrefix = getSavePath(resourceType)
        return File("$filePrefix$fileKey")
    }

    override fun deleteRepo(fileKey: String, resourceType: String) {
        TODO("Not yet implemented")
    }

    private fun getSavePath(typeOfResource: String): String {
        return when (typeOfResource) {
            "students" -> STUDENTS_PATH
            "repos" -> REPOS_PATH
            "whitelist" -> WHITELIST_PATH
            else -> throw RuntimeException("Invalid type of resource.")
        }
    }

    companion object {
        const val STUDENTS_PATH = "src/main/resources/git/students/"
        const val REPOS_PATH = "src/main/resources/git/repos/"
        const val WHITELIST_PATH = "src/main/resources/git/whitelist/"
    }
}