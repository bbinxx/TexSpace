package com.bbinxx.texspace

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ProjectRepository(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val _projects = MutableStateFlow<List<LatexProject>>(emptyList())
    val projects = _projects.asStateFlow()

    private var rootPath: Path? = null

    suspend fun setRootPath(path: String) {
        if (path.isBlank()) return
        try {
            rootPath = path.toPath()
            refreshProjects()
        } catch (e: Exception) {
            println("Error setting root path: ${e.message}")
            _projects.value = emptyList()
        }
    }

    suspend fun refreshProjects() = withContext(Dispatchers.IO) {
        val root = rootPath ?: return@withContext
        try {
            if (!fileSystem.exists(root)) {
                fileSystem.createDirectories(root)
            }
            
            val items = fileSystem.list(root)
            
            val projectFolders = items.filter { 
                try { 
                    fileSystem.metadata(it).isDirectory 
                } catch (e: Exception) { false }
            }
            
            _projects.value = projectFolders.map { folder ->
                val metadata = fileSystem.metadata(folder)
                val files = try { fileSystem.list(folder).filter { fileSystem.metadata(it).isRegularFile } } catch (e: Exception) { emptyList() }
                
                LatexProject(
                    id = folder.name,
                    name = folder.name,
                    path = folder.toString(),
                    lastModified = metadata.lastModifiedAtMillis ?: 0L,
                    createdAt = metadata.lastModifiedAtMillis ?: 0L, // Placeholder
                    fileCount = files.size
                )
            }.sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            println("Error refreshing projects: ${e.message}")
            _projects.value = emptyList()
        }
    }

    suspend fun createProject(name: String): LatexProject? = withContext(Dispatchers.IO) {
        val root = rootPath ?: return@withContext null
        try {
            val projectPath = root / name
            if (!fileSystem.exists(projectPath)) {
                fileSystem.createDirectories(projectPath)
                val mainTex = projectPath / "main.tex"
                val starterCode = """
                    \documentclass[12pt, a4paper]{article}
                    
                    \usepackage[utf8]{inputenc}
                    \usepackage[T1]{fontenc}
                    \usepackage{amsmath}
                    \usepackage{graphicx}
                    
                    \title{$name}
                    \author{TexSpace User}
                    \date{\today}
                    
                    \begin{document}
                    
                    \maketitle
                    
                    \section{Introduction}
                    Welcome to your new LaTeX project in \textbf{TexSpace}! 
                    
                    \section{Sample Section}
                    This is a sample project created automatically by the IDE. You can start editing this file or create new ones in the sidebar.
                    
                    \subsection{Mathematics}
                    Here is a sample equation:
                    \begin{equation}
                        E = mc^2
                    \end{equation}
                    
                    \section{Conclusion}
                    Happy typesetting!
                    
                    \end{document}
                """.trimIndent()
                
                fileSystem.write(mainTex) {
                    writeUtf8(starterCode)
                }
                refreshProjects()
            }
            _projects.value.find { it.name == name }
        } catch (e: Exception) {
            println("Error creating project: ${e.message}")
            null
        }
    }

    suspend fun deleteProject(project: LatexProject) = withContext(Dispatchers.IO) {
        try {
            val path = project.path.toPath()
            fileSystem.deleteRecursively(path)
            refreshProjects()
        } catch (e: Exception) {
            println("Error deleting project: ${e.message}")
        }
    }

    suspend fun renameProject(project: LatexProject, newName: String) = withContext(Dispatchers.IO) {
        try {
            val oldPath = project.path.toPath()
            val newPath = oldPath.parent!! / newName
            fileSystem.atomicMove(oldPath, newPath)
            refreshProjects()
        } catch (e: Exception) {
            println("Error renaming project: ${e.message}")
        }
    }

    suspend fun copyFileToProject(project: LatexProject, sourcePath: String) = withContext(Dispatchers.IO) {
        try {
            val projectDir = project.path.toPath()
            val sourceFile = sourcePath.toPath()
            val destFile = projectDir / sourceFile.name
            fileSystem.copy(sourceFile, destFile)
            refreshProjects()
        } catch (e: Exception) {
            println("Error copying file: ${e.message}")
        }
    }

    suspend fun getFiles(project: LatexProject): List<LatexFile> = withContext(Dispatchers.IO) {
        try {
            val path = project.path.toPath()
            if (!fileSystem.exists(path)) return@withContext emptyList()
            
            fileSystem.list(path).filter { 
                try {
                    val metadata = fileSystem.metadata(it)
                    metadata.isRegularFile && (it.name.endsWith(".tex") || it.name.endsWith(".bib") || it.name.endsWith(".png") || it.name.endsWith(".jpg"))
                } catch (e: Exception) { false }
            }.map { file ->
                LatexFile(
                    id = file.toString(),
                    name = file.name,
                    content = if (file.name.endsWith(".tex") || file.name.endsWith(".bib")) {
                        try { fileSystem.read(file) { readUtf8() } } catch (e: Exception) { "" }
                    } else {
                        "Binary File"
                    },
                    lastModified = try { fileSystem.metadata(file).lastModifiedAtMillis ?: 0L } catch (e: Exception) { 0L }
                )
            }
        } catch (e: Exception) {
            println("Error getting files: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveFile(file: LatexFile) = withContext(Dispatchers.IO) {
        try {
            val path = file.id.toPath()
            fileSystem.write(path) {
                writeUtf8(file.content)
            }
        } catch (e: Exception) {
            println("Error saving file: ${e.message}")
        }
    }

    suspend fun deleteFile(file: LatexFile) = withContext(Dispatchers.IO) {
        try {
            val path = file.id.toPath()
            fileSystem.delete(path)
        } catch (e: Exception) {
            println("Error deleting file: ${e.message}")
        }
    }

    suspend fun renameFile(file: LatexFile, newName: String): LatexFile = withContext(Dispatchers.IO) {
        try {
            val oldPath = file.id.toPath()
            val newPath = oldPath.parent!! / newName
            fileSystem.atomicMove(oldPath, newPath)
            file.copy(id = newPath.toString(), name = newName)
        } catch (e: Exception) {
            println("Error renaming file: ${e.message}")
            file
        }
    }
}
