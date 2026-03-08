package com.bbinxx.texspace

import java.io.File
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.TimeUnit

object LatexCompiler {

    fun compile(request: CompileRequest): CompileResponse {
        // Check if pdflatex is installed
        val hasPdflatex = try {
            ProcessBuilder("pdflatex", "--version").start().waitFor() == 0
        } catch (e: Exception) {
            false
        }

        if (!hasPdflatex) {
            return CompileResponse(
                pdfBase64 = null,
                log = "Error: 'pdflatex' not found on system.\n\nPlease install it using:\nsudo apt install texlive-latex-base texlive-latex-extra texlive-fonts-recommended",
                errors = listOf(LatexError(null, "pdflatex not found"))
            )
        }

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

            val processBuilder = ProcessBuilder(
                "pdflatex", "-interaction=nonstopmode", "-halt-on-error", "main.tex"
            )
            processBuilder.directory(tempDir)

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

            val errors = parseLogForErrors(logOutput)

            val exitCode = process.exitValue()
            if (exitCode != 0 && base64Pdf == null) {
                if (logOutput.contains("! LaTeX Error: File")) {
                    val missingPackage = logOutput.substringAfter("! LaTeX Error: File `").substringBefore(".sty' not found.")
                    val enhancedLog = logOutput + "\n\n--- SUGESTION ---\n" +
                        "It looks like you're missing the package: $missingPackage.sty\n" +
                        "Try installing extra packages:\n" +
                        "sudo apt install texlive-latex-extra texlive-fonts-recommended"
                    return CompileResponse(null, enhancedLog, parseLogForErrors(logOutput))
                }
            }

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
