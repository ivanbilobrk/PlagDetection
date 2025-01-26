package com.fer.projekt.controller

import com.fer.projekt.service.JPlagService
import com.fer.projekt.service.SolutionUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/solutions")
class SolutionUploadController(
    private val solutionUploadService: SolutionUploadService,
    private val jPlagService: JPlagService
) {

    @PostMapping("/upload")
    suspend fun uploadSolution(
        @ModelAttribute solutionUploadRequest: SolutionUploadRequest
    ): ResponseEntity<*> {
        return try {
            val solutionId = solutionUploadService.uploadSolution(
                solutionUploadRequest.zipFile,
                solutionUploadRequest.repoUrl,
                solutionUploadRequest.branch,
                solutionUploadRequest.folderPath,
                solutionUploadRequest.solutionProvider,
                solutionUploadRequest.typeOfResource,
                solutionUploadRequest.filter,
            )
            ResponseEntity.ok(solutionId)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/run")
    fun runJPlag(
        @RequestBody jPlagRunRequest: JPlagRunRequest
    ): ResponseEntity<*> {
        return try {
            jPlagService.runJplag(
                jPlagRunRequest.solutions,
                jPlagRunRequest.language,
                jPlagRunRequest.subject,
                jPlagRunRequest.fileKey
            )
            ResponseEntity.ok("JPlag run started")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}