package com.bbinxx.texspace

import kotlinx.serialization.Serializable

@Serializable
data class CompileRequest(
    val source: String
)

@Serializable
data class CompileResponse(
    val pdfBase64: String?,
    val log: String,
    val errors: List<LatexError>
)

@Serializable
data class LatexError(
    val line: Int?,
    val message: String
)
