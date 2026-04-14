package com.bbinxx.texspace

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.datetime.*

// Modern Color Palette
val SidebarRailBg = Color(0xFF0F111A)
val FileTreeBg = Color(0xFF131524)
val EditorHeaderBg = Color(0xFF131524)
val SidebarBg = Color(0xFF131524)
val CompileGreen = Color(0xFF00C853)
val AccentColor = Color(0xFF448AFF)
val CardGradientStart = Color(0xFF1E213A)
val CardGradientEnd = Color(0xFF161930)

@Composable
fun App(viewModel: LatexEditorViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF0A0C16), 
            surface = Color(0xFF131524),     
            primary = CompileGreen,
            secondary = AccentColor,
            onSurface = Color(0xFFE0E0E0)
        ),
        typography = Typography(
            bodyLarge = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        )
    ) {
        val currentScreen by viewModel.currentScreen.collectAsState()

        Surface(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val enter = fadeIn() + slideInHorizontally { if (targetState == Screen.EDITOR) it else -it }
                    val exit = fadeOut() + slideOutHorizontally { if (targetState == Screen.EDITOR) -it else it }
                    enter togetherWith exit
                }
            ) { screen ->
                when (screen) {
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.EDITOR -> MainLayout(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: LatexEditorViewModel) {
    val projects by viewModel.projects.collectAsState()
    val rootPath by viewModel.rootPath.collectAsState()
    val downloadPath by viewModel.downloadPath.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showFolderPickerRoot by remember { mutableStateOf(false) }
    var showFolderPickerDownload by remember { mutableStateOf(false) }
    
    var projectToDelete by remember { mutableStateOf<LatexProject?>(null) }

    FolderPicker(
        show = showFolderPickerRoot,
        onFolderPicked = { if (it != null) viewModel.updateRootPath(it) },
        onDismiss = { showFolderPickerRoot = false }
    )

    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()

    if (isFirstLaunch) {
        AlertDialog(
            onDismissRequest = { viewModel.markFirstLaunchComplete() },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Celebration, null, modifier = Modifier.size(64.dp), tint = AccentColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Welcome to TexSpace!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your ultimate cross-platform LaTeX IDE is ready. Start by picking a workspace folder in Settings.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🚀 Fast compilation\n📂 File-based projects\n📄 Real-time PDF preview", fontSize = 14.sp, color = AccentColor)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.markFirstLaunchComplete() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Get Started") }
            }
        )
    }

    FolderPicker(
        show = showFolderPickerDownload,
        onFolderPicked = { if (it != null) viewModel.updateDownloadPath(it) },
        onDismiss = { showFolderPickerDownload = false }
    )

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Project") },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    placeholder = { Text("Enter project name...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newProjectName.isNotBlank()) {
                        viewModel.createProject(newProjectName)
                        showCreateDialog = false
                        newProjectName = ""
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", color = Color.Red) },
            text = { Text("This will permanently delete the folder '${project.name}' and all its files.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProject(project); projectToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingItem("Project Workspace", rootPath) { showFolderPickerRoot = true }
                    SettingItem("Export PDF Folder", downloadPath) { showFolderPickerDownload = true }
                    
                    val serverAddress by viewModel.serverAddress.collectAsState()
                    Column {
                        Text("Remote Compiler", style = MaterialTheme.typography.labelMedium, color = AccentColor)
                        OutlinedTextField(
                            value = serverAddress,
                            onValueChange = { viewModel.updateServerAddress(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSettings = false }) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("TexSpace", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color.White)
                        Text("Manage your LaTeX universe", fontSize = 12.sp, color = AccentColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CompileGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (projects.isEmpty()) {
                EmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(280.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(projects) { project ->
                        ProjectCard(
                            project = project, 
                            onClick = { viewModel.openProject(project) },
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(label: String, value: String, onPickerRequest: () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AccentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                .clickable { onPickerRequest() }
                .padding(12.dp)
        ) {
            Icon(Icons.Default.Folder, null, tint = AccentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value.ifBlank { "Select Location..." },
                style = MaterialTheme.typography.bodySmall,
                color = if (value.isBlank()) Color.Gray else Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.LibraryAdd, null, modifier = Modifier.size(120.dp), tint = Color.White.copy(0.05f))
            Spacer(modifier = Modifier.height(20.dp))
            Text("Your workspace is empty", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            Text(
                "Pick a root folder in settings or create a new project to start typesetting.", 
                fontSize = 14.sp, 
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ProjectCard(project: LatexProject, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(listOf(CardGradientStart, CardGradientEnd))
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AccentColor.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Article, null, tint = AccentColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White, maxLines = 1)
                        Text("${project.fileCount} files", fontSize = 12.sp, color = AccentColor)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, null, tint = Color.Gray.copy(0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoStamp("MODIFIED", formatTimestamp(project.lastModified))
                    InfoStamp("CREATED", formatTimestamp(project.createdAt))
                }
            }
        }
    }
}

@Composable
fun InfoStamp(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.7f))
    }
}

fun formatTimestamp(ms: Long): String {
    if (ms <= 0L) return "---"
    try {
        val instant = Instant.fromEpochMilliseconds(ms)
        val tz = TimeZone.currentSystemDefault()
        val dt = instant.toLocalDateTime(tz)
        
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val month = monthNames[dt.monthNumber - 1]
        
        return "$month ${dt.dayOfMonth}, ${dt.year}"
    } catch (e: Exception) {
        return "---"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: LatexEditorViewModel) {
    val files by viewModel.files.collectAsState()
    val selectedFileId by viewModel.selectedFileId.collectAsState()
    val isFileTreeVisible by viewModel.isFileTreeVisible.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compilationLog by viewModel.compilationLog.collectAsState()
    val compiledPdfBase64 by viewModel.compiledPdfBase64.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()

    val currentFile = files.find { it.id == selectedFileId }
    var activeTab by remember { mutableStateOf(0) } // 0: Editor, 1: Preview, 2: Files
    var isLogVisible by remember { mutableStateOf(false) }
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember(selectedProject?.name) { mutableStateOf(selectedProject?.name ?: "") }
    
    var showFilePicker by remember { mutableStateOf(false) }
    FilePicker(
        show = showFilePicker,
        onFilePicked = { if (it != null) viewModel.addExistingFile(it) },
        onDismiss = { showFilePicker = false }
    )

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.renameProject(renameValue)
                    showRenameDialog = false 
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 800.dp

        if (isMobile) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = selectedProject?.name ?: "Editor", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showRenameDialog = true }
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.goBackToDashboard() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (activeTab == 1) {
                                IconButton(onClick = { isLogVisible = !isLogVisible }) {
                                    Icon(Icons.Default.Terminal, null, tint = if (isLogVisible) AccentColor else Color.White)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SidebarRailBg)
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = SidebarRailBg, tonalElevation = 8.dp) {
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            icon = { Icon(Icons.Default.Folder, null) },
                            label = { Text("Files") }
                        )
                        NavigationBarItem(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            icon = { Icon(Icons.Default.Edit, null) },
                            label = { Text("Code") }
                        )
                        NavigationBarItem(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            icon = { Icon(Icons.Default.Visibility, null) },
                            label = { Text("Preview") }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    when (activeTab) {
                        2 -> FileTreePanel(
                            files = files,
                            selectedFileId = selectedFileId,
                            onFileSelected = { viewModel.selectFile(it); activeTab = 0 },
                            onCreateFile = { viewModel.createFile(it) },
                            onAddExisting = { showFilePicker = true },
                            onDeleteFile = { viewModel.deleteFile(it) },
                            onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                            onMinimize = {},
                            modifier = Modifier.fillMaxSize()
                        )
                        0 -> Column(modifier = Modifier.fillMaxSize()) {
                            HeaderRow(title = currentFile?.name ?: "No file", isEditor = true)
                            EditorPanel(
                                source = currentFile?.content ?: "",
                                onSourceChange = { viewModel.updateSource(it) },
                                modifier = Modifier.weight(1f).fillMaxSize()
                            )
                        }
                        1 -> Column(modifier = Modifier.fillMaxSize()) {
                            HeaderRow(
                                title = "Result", 
                                isEditor = false, 
                                onRecompile = { viewModel.compile() },
                                onExport = { viewModel.exportPdf() },
                                isCompiling = isCompiling
                            )
                            Box(modifier = Modifier.weight(1f).background(Color.White)) {
                                PdfPreviewPanel(pdfBase64 = compiledPdfBase64, modifier = Modifier.fillMaxSize())
                                
                                if (isLogVisible) {
                                    BottomLogPanel(
                                        compilationLog, 
                                        modifier = Modifier.align(Alignment.BottomCenter).height(160.dp).background(Color.Black.copy(0.9f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Desktop IDE Layout
            Row(modifier = Modifier.fillMaxSize()) {
                SidebarRail(
                    onMenuClick = { viewModel.toggleFileTree() },
                    onBackClick = { viewModel.goBackToDashboard() }
                )

                if (isFileTreeVisible) {
                    FileTreePanel(
                        files = files,
                        selectedFileId = selectedFileId,
                        onFileSelected = { viewModel.selectFile(it) },
                        onCreateFile = { viewModel.createFile(it) },
                        onAddExisting = { showFilePicker = true },
                        onDeleteFile = { viewModel.deleteFile(it) },
                        onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                        onMinimize = { viewModel.toggleFileTree() }
                    )
                }

                // Main Editor Area
                Column(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                    HeaderRow(
                        title = currentFile?.name ?: "Select a file", 
                        isEditor = true,
                        onTitleClick = { showRenameDialog = true }
                    )
                    EditorPanel(
                        source = currentFile?.content ?: "",
                        onSourceChange = { viewModel.updateSource(it) },
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                    
                    if (isLogVisible) {
                        BottomLogPanel(compilationLog, modifier = Modifier.height(180.dp))
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(24.dp).clickable { isLogVisible = true },
                            color = SidebarRailBg
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Icon(Icons.Default.Terminal, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SHOW LOGS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }
                    }
                }

                // Live Preview Area
                Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF252525))) {
                    HeaderRow(
                        title = "Live Preview", 
                        isEditor = false, 
                        onRecompile = { viewModel.compile() },
                        onExport = { viewModel.exportPdf() },
                        isCompiling = isCompiling
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color.White)) {
                        PdfPreviewPanel(pdfBase64 = compiledPdfBase64, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarRail(onMenuClick: () -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.width(56.dp).fillMaxHeight().background(SidebarRailBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SidebarIcon(Icons.AutoMirrored.Filled.ArrowBack, tint = Color.White, onClick = onBackClick)
        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(horizontal = 12.dp))
        SidebarIcon(Icons.Default.Menu, onClick = onMenuClick)
        SidebarIcon(Icons.Default.AccountCircle)
        Spacer(modifier = Modifier.weight(1f))
        SidebarIcon(Icons.Default.HelpOutline)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SidebarIcon(icon: ImageVector, tint: Color = Color.Gray, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = tint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderRow(
    title: String, 
    isEditor: Boolean, 
    onRecompile: (() -> Unit)? = null,
    onExport: (() -> Unit)? = null,
    isCompiling: Boolean = false,
    onTitleClick: () -> Unit = {}
) {
    Surface(
        color = EditorHeaderBg,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isEditor) AccentColor else Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).clickable { onTitleClick() }
                )
                
                if (!isEditor) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = { onRecompile?.invoke() },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(32.dp),
                            enabled = !isCompiling,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CompileGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compile", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { onExport?.invoke() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.FileDownload, "Download PDF", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            if (isCompiling) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter),
                    color = CompileGreen,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun FileTreePanel(
    files: List<LatexFile>,
    selectedFileId: String,
    onFileSelected: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onAddExisting: () -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (String, String) -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier.width(260.dp)
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    
    var fileToDelete by remember { mutableStateOf<String?>(null) }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(value = newFileName, onValueChange = { newFileName = it }, placeholder = { Text("filename.tex") })
            },
            confirmButton = {
                Button(onClick = { 
                    if (newFileName.isNotBlank()) {
                        onCreateFile(newFileName)
                        showCreateDialog = false
                        newFileName = ""
                    }
                }) { Text("Create") }
            }
        )
    }
    
    fileToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File?") },
            text = { Text("This will permanently delete the file.") },
            confirmButton = {
                Button(onClick = { onDeleteFile(id); fileToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Surface(
        color = FileTreeBg,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("EXPLORER", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.NoteAdd, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onAddExisting, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.FileUpload, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(files) { file ->
                    FileItem(
                        file = file,
                        isSelected = file.id == selectedFileId,
                        onSelect = { onFileSelected(file.id) },
                        onDelete = { fileToDelete = file.id },
                        onRename = { onRenameFile(file.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: LatexFile, 
    isSelected: Boolean, 
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(file.name) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = {
                Button(onClick = { 
                    onRename(newName)
                    showRenameDialog = false 
                }) { Text("Rename") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(if (isSelected) AccentColor.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                file.name.endsWith(".tex") -> Icons.Default.Description
                file.name.endsWith(".png") || file.name.endsWith(".jpg") -> Icons.Default.Image
                else -> Icons.Default.Article
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isSelected) AccentColor else Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = file.name, 
            fontSize = 13.sp, 
            color = if (isSelected) Color.White else Color.Gray, 
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        
        if (isSelected) {
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { showMenu = false; showRenameDialog = true })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}

@Composable
fun BottomLogPanel(log: String, modifier: Modifier = Modifier.fillMaxWidth().height(100.dp)) {
    Surface(
        color = SidebarRailBg,
        modifier = modifier.border(0.5.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("ENGINE LOGS", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AccentColor, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            color = Color(0xFFB0B0B0),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
expect fun EditorPanel(
    source: String,
    onSourceChange: (String) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun PdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier = Modifier
)