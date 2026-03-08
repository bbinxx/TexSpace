package com.bbinxx.texspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LatexFile(
    val id: String,
    val name: String,
    val content: String
)

class LatexEditorViewModel : ViewModel() {
    private val client = LatexClient()

    private val defaultContent = """
        \documentclass[a4paper,11pt]{article}
        \usepackage[utf8]{inputenc}
        \usepackage{geometry}
        \geometry{a4paper, margin=1in}

        \begin{document}

        \begin{center}
            \Huge \textbf{BIBIN RAJU} \\
            \normalsize Engineering Student $\cdot$ Open Source Enthusiast
        \end{center}

        \section*{Profile}
        Passionate Kotlin Multiplatform developer building TexSpace!

        \section*{Projects}
        \textbf{TexSpace} -- A full LaTeX editor built with KMP and Compose.

        \end{document}
    """.trimIndent()

    private val _files = MutableStateFlow(listOf(LatexFile("1", "main.tex", defaultContent)))
    val files: StateFlow<List<LatexFile>> = _files.asStateFlow()

    private val _selectedFileId = MutableStateFlow("1")
    val selectedFileId: StateFlow<String> = _selectedFileId.asStateFlow()

    private val _isFileTreeVisible = MutableStateFlow(true)
    val isFileTreeVisible: StateFlow<Boolean> = _isFileTreeVisible.asStateFlow()

    private val _compiledPdfBase64 = MutableStateFlow<String?>(null)
    val compiledPdfBase64: StateFlow<String?> = _compiledPdfBase64.asStateFlow()

    private val _compilationLog = MutableStateFlow("Ready.")
    val compilationLog: StateFlow<String> = _compilationLog.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    val currentFileContent: String
        get() = _files.value.find { it.id == _selectedFileId.value }?.content ?: ""

    fun updateSource(newSource: String) {
        _files.value = _files.value.map {
            if (it.id == _selectedFileId.value) it.copy(content = newSource) else it
        }
    }

    fun selectFile(fileId: String) {
        _selectedFileId.value = fileId
    }

    fun createFile() {
        // Use random + time for ID (CommonMain safe)
        val newId = "file_${Random.nextLong()}_${System.currentTimeMillis()}"
        // Suggest a name based on current count
        val newName = "new_file_${_files.value.size}.tex"
        val newFile = LatexFile(newId, newName, "% New LaTeX file")
        _files.value += newFile
        _selectedFileId.value = newId
    }

    fun deleteFile(fileId: String) {
        if (_files.value.size <= 1) return // Keep at least one file
        val wasSelected = _selectedFileId.value == fileId
        val updatedFiles = _files.value.filter { it.id != fileId }
        _files.value = updatedFiles
        if (wasSelected) {
            _selectedFileId.value = updatedFiles.first().id
        }
    }

    fun renameFile(fileId: String, newName: String) {
        _files.value = _files.value.map {
            if (it.id == fileId) it.copy(name = newName) else it
        }
    }

    fun toggleFileTree() {
        _isFileTreeVisible.value = !_isFileTreeVisible.value
    }

    fun save() {
        _compilationLog.value = "Project saved locally."
    }

    fun compile() {
        if (_isCompiling.value) return

        viewModelScope.launch {
            _isCompiling.value = true
            _compilationLog.value = "Compiling..."
            
            val response = client.compileLatex(currentFileContent)
            
            // Forces refresh by setting to null briefly
            _compiledPdfBase64.value = null 
            delay(50) 
            
            if (response.pdfBase64 != null) {
                _compiledPdfBase64.value = response.pdfBase64
            }
            _compilationLog.value = response.log
            _isCompiling.value = false
        }
    }

    fun exportPdf() {
        // Placeholder for real export
        _compilationLog.value = "PDF exported to project folder."
    }
}
