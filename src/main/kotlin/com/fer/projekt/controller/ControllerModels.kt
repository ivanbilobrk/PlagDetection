package com.fer.projekt.controller

import org.springframework.web.multipart.MultipartFile

data class SolutionUploadRequest(
    val zipFile: MultipartFile?,
    val repoUrl: String?,
    val branch: String?,
    val folderPath: String,
    val solutionProvider: SolutionProviderName,
    val typeOfResource: String,
    val filter: List<String>?
)

data class JPlagSolutionRun(
    val path: String,
    val solutionProvider: SolutionProviderName,
    val typeOfResource: String,
    val useAsOldSubmission: Boolean
)

data class JPlagRunRequest(
    val solutions: List<JPlagSolutionRun>,
    val language: String,
    val subject: String,
    val fileKey: String
)