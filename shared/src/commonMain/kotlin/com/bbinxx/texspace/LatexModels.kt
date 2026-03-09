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
data class CompileRequest(
    val source: String
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

const val SERVER_PORT = 8080
