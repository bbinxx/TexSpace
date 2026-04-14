package com.bbinxx.texspace

import kotlinx.serialization.Serializable

@Serializable
data class LatexFile(
    val id: String,
    val name: String,
    val content: String,
    val lastModified: Long = 0L
)

@Serializable
data class LatexProject(
    val id: String,
    val name: String,
    val path: String,
    val lastModified: Long = 0L,
    val createdAt: Long = 0L,
    val fileCount: Int = 0
)

@Serializable
data class ApiCompileRequest(
    val main_file: String,
    val compiler: String = "pdflatex",
    val files: Map<String, String>
)

@Serializable
data class CompileResponse(
    val pdfBase64: String?,
    val log: String,
    val errors: List<LatexError> = emptyList()
)

@Serializable
data class LatexError(
    val line: Int?,
    val message: String
)
