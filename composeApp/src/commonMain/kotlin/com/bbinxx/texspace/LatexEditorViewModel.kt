package com.bbinxx.texspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LatexEditorViewModel : ViewModel() {
    private val client = LatexClient()

    private val _sourceCode = MutableStateFlow(
        """
        \documentclass{article}
        \begin{document}
        Hello, \textbf{TexSpace}!
        \end{document}
        """.trimIndent()
    )
    val sourceCode: StateFlow<String> = _sourceCode.asStateFlow()

    private val _compiledPdfBase64 = MutableStateFlow<String?>(null)
    val compiledPdfBase64: StateFlow<String?> = _compiledPdfBase64.asStateFlow()

    private val _compilationLog = MutableStateFlow("Ready.")
    val compilationLog: StateFlow<String> = _compilationLog.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _autoCompile = MutableStateFlow(true)
    val autoCompile: StateFlow<Boolean> = _autoCompile.asStateFlow()

    private var compileJob: Job? = null
    private var debounceJob: Job? = null

    fun updateSource(newSource: String) {
        _sourceCode.value = newSource
        if (_autoCompile.value) {
            debounceJob?.cancel()
            debounceJob = viewModelScope.launch {
                delay(1000)
                compile()
            }
        }
    }

    fun toggleAutoCompile() {
        _autoCompile.value = !_autoCompile.value
    }

    fun save() {
        // Mock save for now
        _compilationLog.value = "Project saved locally."
    }

    fun compile() {
        if (_isCompiling.value) return

        compileJob = viewModelScope.launch {
            _isCompiling.value = true
            _compilationLog.value = "Compiling..."
            
            val response = client.compileLatex(_sourceCode.value)
            
            if (response.pdfBase64 != null) {
                _compiledPdfBase64.value = response.pdfBase64
            }
            _compilationLog.value = response.log
            _isCompiling.value = false
        }
    }
}
