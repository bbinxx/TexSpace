package com.bbinxx.texspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
=======
import com.bbinxx.texspace.db.TexSpaceRepository
>>>>>>> dev
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
<<<<<<< HEAD

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
=======
import io.ktor.util.decodeBase64Bytes
import okio.FileSystem
import okio.Path.Companion.toPath
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

    private val _downloadPath = MutableStateFlow("")
    val downloadPath: StateFlow<String> = _downloadPath.asStateFlow()

    private val _files = MutableStateFlow<List<LatexFile>>(emptyList())
    val files: StateFlow<List<LatexFile>> = _files.asStateFlow()

    private val _selectedFileId = MutableStateFlow("")
    val selectedFileId: StateFlow<String> = _selectedFileId.asStateFlow()

    private val _isFileTreeVisible = MutableStateFlow(true)
    val isFileTreeVisible: StateFlow<Boolean> = _isFileTreeVisible.asStateFlow()
>>>>>>> dev

    private val _compiledPdfBase64 = MutableStateFlow<String?>(null)
    val compiledPdfBase64: StateFlow<String?> = _compiledPdfBase64.asStateFlow()

    private val _compilationLog = MutableStateFlow("Ready.")
    val compilationLog: StateFlow<String> = _compilationLog.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

<<<<<<< HEAD
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
=======
    private val _serverAddress = MutableStateFlow(initialBaseUrl)
    val serverAddress: StateFlow<String> = _serverAddress.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(false)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    fun markFirstLaunchComplete() {
        _isFirstLaunch.value = false
        viewModelScope.launch {
            repository.setSetting("first_launch", "false")
        }
    }

    init {
        viewModelScope.launch {
            val firstLaunch = repository.getSetting("first_launch")
            if (firstLaunch == null) {
                _isFirstLaunch.value = true
            }
        }
        viewModelScope.launch {
            try {
                val savedRoot = repository.getSetting("root_path")
                if (savedRoot != null) {
                    _rootPath.value = savedRoot
                    projectRepository.setRootPath(savedRoot)
                }
            } catch (e: Exception) {
                println("ViewModel Init Error: ${e.message}")
            }
        }

        viewModelScope.launch {
            val savedDownload = repository.getSetting("download_path")
            if (savedDownload != null) {
                _downloadPath.value = savedDownload
            }
        }

        viewModelScope.launch {
            val savedServer = repository.getSetting("server_address")
            if (savedServer != null) {
                _serverAddress.value = savedServer
                client.baseUrl = savedServer
>>>>>>> dev
            }
        }
    }

<<<<<<< HEAD
    fun toggleAutoCompile() {
        _autoCompile.value = !_autoCompile.value
    }

    fun save() {
        // Mock save for now
        _compilationLog.value = "Project saved locally."
=======
    fun updateRootPath(path: String) {
        viewModelScope.launch {
            _rootPath.value = path
            repository.setSetting("root_path", path)
            projectRepository.setRootPath(path)
        }
    }

    fun updateDownloadPath(path: String) {
        viewModelScope.launch {
            _downloadPath.value = path
            repository.setSetting("download_path", path)
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

    fun deleteProject(project: LatexProject) {
        viewModelScope.launch {
            projectRepository.deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                goBackToDashboard()
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

    fun renameProject(newName: String) {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            projectRepository.renameProject(project, newName)
            // Re-open project with new path/name
            val updatedProjects = projectRepository.projects.value
            val updatedProject = updatedProjects.find { it.name == newName }
            if (updatedProject != null) {
                _selectedProject.value = updatedProject
            }
        }
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

    fun createFile(name: String = "") {
        val currentProject = _selectedProject.value ?: return
        val newName = if (name.isBlank()) "new_file_${_files.value.size}.tex" else name
        viewModelScope.launch {
            val path = "${currentProject.path}/$newName"
            val newFile = LatexFile(path, newName, "% New LaTeX file\n\\documentclass{article}\n\\begin{document}\n\n\\end{document}")
            projectRepository.saveFile(newFile)
            _files.value = projectRepository.getFiles(currentProject)
            _selectedFileId.value = path
        }
    }

    fun addExistingFile(sourcePath: String) {
        val currentProject = _selectedProject.value ?: return
        viewModelScope.launch {
            projectRepository.copyFileToProject(currentProject, sourcePath)
            _files.value = projectRepository.getFiles(currentProject)
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
            } else if (_files.value.isEmpty()) {
                _selectedFileId.value = ""
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
>>>>>>> dev
    }

    fun compile() {
        if (_isCompiling.value) return

<<<<<<< HEAD
        compileJob = viewModelScope.launch {
            _isCompiling.value = true
            _compilationLog.value = "Compiling..."
            
            val response = client.compileLatex(_sourceCode.value)
=======
        viewModelScope.launch {
            _isCompiling.value = true
            _compilationLog.value = "Compiling at ${client.baseUrl}..."
            
            val allFiles = _files.value.associate { it.name to it.content }
            val mainFileName = _files.value.find { it.id == _selectedFileId.value }?.name ?: "main.tex"
            
            val response = client.compileLatex(mainFileName, allFiles)
            
            _compiledPdfBase64.value = null 
            delay(50) 
>>>>>>> dev
            
            if (response.pdfBase64 != null) {
                _compiledPdfBase64.value = response.pdfBase64
            }
            _compilationLog.value = response.log
            _isCompiling.value = false
        }
    }
<<<<<<< HEAD
=======

    fun exportPdf() {
        val pdfBase64 = _compiledPdfBase64.value ?: return
        val currentProject = _selectedProject.value ?: return
        val downloadPath = _downloadPath.value.ifBlank { _rootPath.value }
        if (downloadPath.isBlank()) {
            _compilationLog.value = "Error: Set download path in settings."
            return
        }

        viewModelScope.launch {
            try {
                val fs = FileSystem.SYSTEM
                val destDir = downloadPath.toPath()
                if (!fs.exists(destDir)) fs.createDirectories(destDir)
                
                val fileName = "${currentProject.name}_export.pdf"
                val destFile = destDir / fileName
                
                // Decode base64
                val bytes = pdfBase64.decodeBase64Bytes()
                
                fs.write(destFile) {
                    write(bytes)
                }
                _compilationLog.value = "PDF exported to: $destFile"
            } catch (e: Exception) {
                _compilationLog.value = "Export failed: ${e.message}"
            }
        }
    }
>>>>>>> dev
}
