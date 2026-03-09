package com.bbinxx.texspace

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class LatexClient(var baseUrl: String) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun compileLatex(source: String): CompileResponse {
        return try {
            val response = client.post("$baseUrl/compile") {
                contentType(ContentType.Application.Json)
                setBody(CompileRequest(source))
            }
            response.body()
        } catch (e: Exception) {
            CompileResponse(
                pdfBase64 = null,
                log = "Connection error to $baseUrl: ${e.message}",
                errors = listOf(LatexError(null, "Failed to connect to compilation server."))
            )
        }
    }
}
