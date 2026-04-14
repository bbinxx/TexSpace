package com.bbinxx.texspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.texspace.db.TexSpaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Screen {
    DASHBOARD,
    EDITOR
}

class LatexEditorViewModel(
    private val repository: TexSpaceRepository,
    private val projectRepository: ProjectRepository,
    initialBaseUrl: String
) : ViewModel() {
    private val client = LatexClient(initialBaseUrl)

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _projects = projectRepository.projects
    val projects: StateFlow<List<LatexProject>> = _projects

    private val _selectedProject = MutableStateFlow<LatexProject?>(null)
    val selectedProject: StateFlow<LatexProject?> = _selectedProject.asStateFlow()

    private val _rootPath = MutableStateFlow("")
    val rootPath: StateFlow<String> = _rootPath.asStateFlow()

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

    init {
        viewModelScope.launch {
            try {
                val savedRoot = repository.getSetting("root_path")
                if (savedRoot != null) {
                    _rootPath.value = savedRoot
                    projectRepository.setRootPath(savedRoot)
                } else {
                    // Default path for demo/first run
                    val default = ""
                    _rootPath.value = default
                }
            } catch (e: Exception) {
                println("ViewModel Init Error: ${e.message}")
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

    fun updateRootPath(path: String) {
        viewModelScope.launch {
            _rootPath.value = path
            repository.setSetting("root_path", path)
            projectRepository.setRootPath(path)
        }
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            val project = projectRepository.createProject(name)
            if (project != null) {
                openProject(project)
            }
        }
    }

    fun openProject(project: LatexProject) {
        viewModelScope.launch {
            _selectedProject.value = project
            val projectFiles = projectRepository.getFiles(project)
            _files.value = projectFiles
            if (projectFiles.isNotEmpty()) {
                val mainFile = projectFiles.find { it.name == "main.tex" }
                _selectedFileId.value = mainFile?.id ?: projectFiles.first().id
            }
            _currentScreen.value = Screen.EDITOR
        }
    }

    fun goBackToDashboard() {
        _currentScreen.value = Screen.DASHBOARD
        _selectedProject.value = null
        _files.value = emptyList()
    }

    fun updateSource(newSource: String) {
        _files.value = _files.value.map {
            if (it.id == _selectedFileId.value) it.copy(content = newSource) else it
        }
        viewModelScope.launch {
            val currentFile = _files.value.find { it.id == _selectedFileId.value }
            if (currentFile != null) {
                projectRepository.saveFile(currentFile)
            }
        }
    }

    fun selectFile(fileId: String) {
        _selectedFileId.value = fileId
    }

    fun createFile() {
        val currentProject = _selectedProject.value ?: return
        val newName = "new_file_${_files.value.size}.tex"
        viewModelScope.launch {
            // In a real app we'd prompt for name, but here we just create it
            // We need to update ProjectRepository to handle file creation within a project better
            // For now, let's just use the file path
            val path = "${currentProject.path}/$newName"
            val newFile = LatexFile(path, newName, "% New LaTeX file")
            projectRepository.saveFile(newFile)
            _files.value = projectRepository.getFiles(currentProject)
            _selectedFileId.value = path
        }
    }

    fun deleteFile(fileId: String) {
        val file = _files.value.find { it.id == fileId } ?: return
        viewModelScope.launch {
            projectRepository.deleteFile(file)
            val currentProject = _selectedProject.value ?: return@launch
            _files.value = projectRepository.getFiles(currentProject)
            if (_selectedFileId.value == fileId && _files.value.isNotEmpty()) {
                _selectedFileId.value = _files.value.first().id
            }
        }
    }

    fun renameFile(fileId: String, newName: String) {
        val file = _files.value.find { it.id == fileId } ?: return
        viewModelScope.launch {
            projectRepository.renameFile(file, newName)
            val currentProject = _selectedProject.value ?: return@launch
            _files.value = projectRepository.getFiles(currentProject)
            _selectedFileId.value = _files.value.find { it.name == newName }?.id ?: ""
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
        _compilationLog.value = "Project saved to folder."
    }

    fun compile() {
        if (_isCompiling.value) return

        viewModelScope.launch {
            _isCompiling.value = true
            _compilationLog.value = "Compiling at ${client.baseUrl}..."
            
            val allFiles = _files.value.associate { it.name to it.content }
            val mainFileName = _files.value.find { it.id == _selectedFileId.value }?.name ?: "main.tex"
            
            val response = client.compileLatex(mainFileName, allFiles)
            
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
        _compilationLog.value = "PDF export simulated."
    }
}
