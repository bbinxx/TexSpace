package com.bbinxx.texspace

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.Json

class LatexClient(var baseUrl: String = "https://texcompiler.onrender.com") {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun compileLatex(mainFile: String, files: Map<String, String>): CompileResponse {
        return try {
            val response = client.post("$baseUrl/compile") {
                contentType(ContentType.Application.Json)
                setBody(ApiCompileRequest(
                    main_file = mainFile,
                    compiler = "pdflatex",
                    files = files
                ))
            }

            if (response.status.isSuccess()) {
                val bytes = response.readRawBytes()
                CompileResponse(
                    pdfBase64 = bytes.encodeBase64(),
                    log = "Compilation successful. PDF received (${bytes.size} bytes).",
                    errors = emptyList()
                )
            } else {
                val errorMsg = try { response.body<String>() } catch (e: Exception) { "Status: ${response.status}" }
                CompileResponse(
                    pdfBase64 = null,
                    log = "Compilation failed: $errorMsg",
                    errors = listOf(LatexError(null, "Server returned error status ${response.status}"))
                )
            }
        } catch (e: Exception) {
            CompileResponse(
                pdfBase64 = null,
                log = "Connection error to $baseUrl: ${e.message}",
                errors = listOf(LatexError(null, "Failed to connect to compilation server."))
            )
        }
    }
}
