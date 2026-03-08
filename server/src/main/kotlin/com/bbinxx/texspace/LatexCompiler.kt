package com.bbinxx.texspace

import java.io.File
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories

object LatexCompiler {

    fun compile(request: CompileRequest): CompileResponse {
        val tempDir = Files.createTempDirectory("texspace_").toFile()
        try {
            val mainTex = File(tempDir, "main.tex")
            mainTex.writeText(request.source)

            // Make directory readable/writable for docker
            tempDir.setReadable(true, false)
            tempDir.setWritable(true, false)
            tempDir.setExecutable(true, false)

            mainTex.setReadable(true, false)
            mainTex.setWritable(true, false)

            // Run docker
            // We use texlive/texlive:latest for now to avoid requiring manual build of the local Dockerfile unless necessary,
            // or we could use a custom tag. Wait, the user asked to "Use Docker with TeXLive", "Create a Dockerfile that installs...".
            // So normally we'd build it once or just use an existing texlive image.
            // Let's use an existing base image like `texlive/texlive:latest` or `blang/latex` in real life,
            // but for safety, let's just run default pdflatex if docker is not installed?
            // The prompt says: "Ensure compilation runs inside a sandbox with timeout limit, restricted filesystem, no shell execution."
            
            // Using Docker:
            // docker run --rm --net none -v /tempDir:/workspace -w /workspace texlive/texlive:latest pdflatex -interaction=nonstopmode -halt-on-error main.tex
            val processBuilder = ProcessBuilder(
                "docker", "run", "--rm",
                "--net", "none", // sandbox network
                "-v", "${tempDir.absolutePath}:/workspace",
                "-w", "/workspace",
                "texlive/texlive:latest",
                "pdflatex", "-interaction=nonstopmode", "-halt-on-error", "main.tex"
            )

            // timeout limit
            val process = processBuilder.start()
            val finished = process.waitFor(10, TimeUnit.SECONDS)

            if (!finished) {
                process.destroyForcibly()
                return CompileResponse(
                    pdfBase64 = null,
                    log = "Compilation timed out after 10 seconds.",
                    errors = listOf(LatexError(null, "Compilation timeout"))
                )
            }

            val logOutput = process.inputStream.bufferedReader().readText() + 
                            process.errorStream.bufferedReader().readText()

            val pdfFile = File(tempDir, "main.pdf")
            val base64Pdf = if (pdfFile.exists()) {
                Base64.getEncoder().encodeToString(pdfFile.readBytes())
            } else null

            // Parse errors roughly: look for "^! " or "l.\d+"
            // This is a simplest possible error parser:
            val errors = parseLogForErrors(logOutput)

            return CompileResponse(
                pdfBase64 = base64Pdf,
                log = logOutput,
                errors = errors
            )
        } catch (e: Exception) {
            return CompileResponse(
                pdfBase64 = null,
                log = "Server Error: ${e.message}\n${e.stackTraceToString()}",
                errors = listOf(LatexError(null, e.message ?: "Unknown server error"))
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun parseLogForErrors(log: String): List<LatexError> {
        val lines = log.lines()
        val errors = mutableListOf<LatexError>()
        var currentErrorMsg = ""
        var lineNum: Int? = null

        for (line in lines) {
            if (line.startsWith("! ")) {
                currentErrorMsg = line.removePrefix("! ").trim()
            } else if (line.startsWith("l.")) {
                // e.g. "l.5 \textbf{error}"
                val numPart = line.substringAfter("l.").substringBefore(" ").toIntOrNull()
                if (numPart != null && currentErrorMsg.isNotEmpty()) {
                    errors.add(LatexError(numPart, currentErrorMsg))
                    currentErrorMsg = ""
                }
            }
        }
        if (currentErrorMsg.isNotEmpty()) {
            errors.add(LatexError(lineNum, currentErrorMsg))
        }

        return errors
    }
}
