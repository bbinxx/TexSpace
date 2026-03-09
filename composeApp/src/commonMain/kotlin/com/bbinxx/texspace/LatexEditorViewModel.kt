package com.bbinxx.texspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.texspace.db.TexSpaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class LatexEditorViewModel(
    private val repository: TexSpaceRepository,
    initialBaseUrl: String
) : ViewModel() {
    private val client = LatexClient(initialBaseUrl)

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

    private val _files = MutableStateFlow<List<LatexFile>>(emptyList())
    val files: StateFlow<List<LatexFile>> = _files.asStateFlow()

    private val _selectedFileId = MutableStateFlow("")
    val selectedFileId: StateFlow<String> = _selectedFileId.asStateFlow()

    private val _isFileTreeVisible = MutableStateFlow(true)
    val isFileTreeVisible: StateFlow<Boolean> = _isFileTreeVisible.asStateFlow()

    private val _compiledPdfBase64 = MutableStateFlow<String?>(null)
    val compiledPdfBase64: StateFlow<String?> = _compiledPdfBase64.asStateFlow()

    private val _compilationLog = MutableStateFlow("Ready.")
    val compilationLog: StateFlow<String> = _compilationLog.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _serverAddress = MutableStateFlow(initialBaseUrl)
    val serverAddress: StateFlow<String> = _serverAddress.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    val currentFileContent: String
        get() = _files.value.find { it.id == _selectedFileId.value }?.content ?: ""

    init {
        viewModelScope.launch {
            // Load files from repo
            repository.getAllFiles().collect {
                if (it.isEmpty()) {
                    val firstId = "1"
                    repository.createFile(firstId, "main.tex", defaultContent)
                    _selectedFileId.value = firstId
                } else {
                    _files.value = it
                    if (_selectedFileId.value.isEmpty()) {
                        _selectedFileId.value = it.first().id
                    }
                }
            }
        }
        
        viewModelScope.launch {
            val savedServer = repository.getSetting("server_address")
            if (savedServer != null) {
                _serverAddress.value = savedServer
                client.baseUrl = savedServer
            }
        }
    }

    fun updateSource(newSource: String) {
        _files.value = _files.value.map {
            if (it.id == _selectedFileId.value) it.copy(content = newSource) else it
        }
        // Auto-save debounced would be better, but for now manual or on change
        viewModelScope.launch {
            repository.updateFileContent(_selectedFileId.value, newSource)
        }
    }

    fun selectFile(fileId: String) {
        _selectedFileId.value = fileId
    }

    fun createFile() {
        val newId = "file_${Random.nextLong()}_${System.currentTimeMillis()}"
        val newName = "new_file_${_files.value.size}.tex"
        viewModelScope.launch {
            repository.createFile(newId, newName, "% New LaTeX file")
            _selectedFileId.value = newId
        }
    }

    fun deleteFile(fileId: String) {
        if (_files.value.size <= 1) return
        viewModelScope.launch {
            repository.deleteFile(fileId)
            if (_selectedFileId.value == fileId) {
                _selectedFileId.value = _files.value.first { it.id != fileId }.id
            }
        }
    }

    fun renameFile(fileId: String, newName: String) {
        viewModelScope.launch {
            repository.renameFile(fileId, newName)
        }
    }

    fun updateServerAddress(newAddress: String) {
        _serverAddress.value = newAddress
        client.baseUrl = newAddress
        viewModelScope.launch {
            repository.setSetting("server_address", newAddress)
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
            _compilationLog.value = "Compiling at ${client.baseUrl}..."
            
            val response = client.compileLatex(currentFileContent)
            
            _compiledPdfBase64.value = null 
            delay(50) 
            
            if (response.pdfBase64 != null) {
                _compiledPdfBase64.value = response.pdfBase64
                // Mocking page count detection (Real logic would parse PDF)
                // For now, let's assume 1 page unless we want to do something smarter
                _totalPages.value = 1 
            }
            _compilationLog.value = response.log
            _isCompiling.value = false
        }
    }

    fun setPage(page: Int) {
        if (page in 1.._totalPages.value) {
            _currentPage.value = page
        }
    }

    fun exportPdf() {
        // Real implementation would depend on platform
        _compilationLog.value = "PDF exported. Path: [Local Project Folder]/output.pdf"
    }
}
