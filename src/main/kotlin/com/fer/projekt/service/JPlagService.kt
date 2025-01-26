package com.fer.projekt.service

import com.fer.projekt.controller.JPlagSolutionRun
import com.fer.projekt.controller.SolutionProviderName
import com.fer.projekt.solutionproviders.RepositorySolutionProvider
import de.jplag.JPlag
import de.jplag.Language
import de.jplag.c.CLanguage
import de.jplag.cpp.CPPLanguage
import de.jplag.exceptions.ExitException
import de.jplag.java.JavaLanguage
import de.jplag.javascript.JavaScriptLanguage
import de.jplag.options.JPlagOptions
import de.jplag.python3.PythonLanguage
import de.jplag.reporting.reportobject.ReportObjectFactory
import de.jplag.text.NaturalLanguage
import de.jplag.typescript.TypeScriptLanguage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ExecutorService

@Service
class JPlagService(
    @Qualifier("solutionProvidersMap") private val solutionProvidersMap: Map<SolutionProviderName, RepositorySolutionProvider>,
    private val jPlagExecutorService: ExecutorService,
) {

    private val log = KotlinLogging.logger {  }

    fun runJplag(
        solutions: List<JPlagSolutionRun>,
        language: String,
        subject: String,
        fileKey: String
    ) {
        val jPlagLanguage = languageToLanguageObject(language)
        val solutionPaths = getSolutionPaths(solutions)
        val whitelistFile = getWhitelistFile(solutions)
        val oldSubmissionsPaths = getOldSubmissionsPaths(solutions)
        val jPlagOptions = JPlagOptions(jPlagLanguage, solutionPaths, emptySet())
            .withFileSuffixes(listOf(".js"))
            .withBaseCodeSubmissionDirectory(whitelistFile)
            .withOldSubmissionDirectories(oldSubmissionsPaths)
        val resultsPath = getFileToSaveResults(subject, fileKey)
        val fileExists = resultsPath.exists()
        if (fileExists) {
            throw IllegalArgumentException("Results file already exists")
        }

        jPlagExecutorService.submit {
            try {
                val result = JPlag.run(jPlagOptions)
                val reportObjectFactory = ReportObjectFactory(resultsPath)
                reportObjectFactory.createAndSaveReport(result)
            } catch (e: ExitException) {
                log.info { "Exception while comparing solutions: ${e.message}" }
                resultsPath.deleteRecursively()
            }
        }
    }

    private fun getWhitelistFile(solutions: List<JPlagSolutionRun>): File? {
        return solutions.filter { it.typeOfResource == "whitelist" && !it.useAsOldSubmission }.map { solution ->
            val provider = solutionProvidersMap[solution.solutionProvider] ?: throw IllegalArgumentException("Solution provider ${solution.solutionProvider} not found")
            provider.getRepoAsFile(solution.path, solution.typeOfResource)
        }.firstOrNull()
    }

    private fun getOldSubmissionsPaths(solutions: List<JPlagSolutionRun>): Set<File> {
        return solutions.filter { it.typeOfResource != "whitelist" && it.useAsOldSubmission  }.map { solution ->
            val provider = solutionProvidersMap[solution.solutionProvider] ?: throw IllegalArgumentException("Solution provider ${solution.solutionProvider} not found")
            provider.getRepoAsFile(solution.path, solution.typeOfResource)
        }.toSet()
    }

    private fun getSolutionPaths(solutions: List<JPlagSolutionRun>): Set<File> {
        return solutions.filter { it.typeOfResource != "whitelist" && !it.useAsOldSubmission }.map { solution ->
            val provider = solutionProvidersMap[solution.solutionProvider] ?: throw IllegalArgumentException("Solution provider ${solution.solutionProvider} not found")
            provider.getRepoAsFile(solution.path, solution.typeOfResource)
        }.toSet()
    }

    private fun languageToLanguageObject(language: String): Language {
        return when (language) {
            "java" -> JavaLanguage()
            "c" -> CLanguage()
            "cpp" -> CPPLanguage()
            "python" -> PythonLanguage()
            "javascript" -> JavaScriptLanguage()
            "typescript" -> TypeScriptLanguage()
            "sql", "csymple", "text" -> NaturalLanguage()
            else -> throw IllegalArgumentException("Language not supported")
        }
    }

    private fun getFileToSaveResults(subject: String, fileKey: String): File {
        val resultsPath = File(RESULTS_PATH, subject)
        resultsPath.mkdirs()
        return File(resultsPath, "$fileKey.zip")
    }

    private companion object {
        const val RESULTS_PATH = "src/main/resources/results/"
    }
}