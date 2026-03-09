package com.bbinxx.texspace

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import kotlinx.serialization.decodeFromString

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Post)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
    }

    val jsonConfig = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    install(ContentNegotiation) {
        json(jsonConfig)
    }
    
    routing {
        get("/") {
            call.respondText("TexSpace Backend is running.")
        }
        post("/compile") {
            try {
                // Manually receive as text if receive<T> fails due to Ktor 3 transform issues
                val bodyText = call.receiveText()
                val request = jsonConfig.decodeFromString<CompileRequest>(bodyText)
                
                val response = LatexCompiler.compile(request)
                call.respond(response)
            } catch (e: Exception) {
                // If it fails, maybe it's missing pdflatex or packages
                call.respond(HttpStatusCode.OK, CompileResponse(
                    pdfBase64 = null,
                    log = "Server Logic Error: ${e.message}\n${e.stackTraceToString()}",
                    errors = listOf(LatexError(null, e.message ?: "Unknown error"))
                ))
            }
        }
    }
}