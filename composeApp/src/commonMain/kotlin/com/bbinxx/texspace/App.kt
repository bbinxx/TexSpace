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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Clean & Professional Color Palette
val SidebarRailBg = Color(0xFF0F111A)
val FileTreeBg = Color(0xFF1A1C2E)
val EditorHeaderBg = Color(0xFF1A1C2E)
val SidebarBg = Color(0xFF1A1C2E)
val CompileGreen = Color(0xFF4CAF50)
val AccentColor = Color(0xFF64B5F6)

@Composable
fun App(viewModel: LatexEditorViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF0F111A), // Deep Night
            surface = Color(0xFF1A1C2E),     // Dark Slate
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
                    (fadeIn() + scaleIn()) togetherWith (fadeOut() + scaleOut())
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

    FolderPicker(
        show = showFolderPickerRoot,
        onFolderPicked = { if (it != null) viewModel.updateRootPath(it) },
        onDismiss = { showFolderPickerRoot = false }
    )

    FolderPicker(
        show = showFolderPickerDownload,
        onFolderPicked = { if (it != null) viewModel.updateDownloadPath(it) },
        onDismiss = { showFolderPickerDownload = false }
    )

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New LaTeX Project") },
            text = {
                TextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    placeholder = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newProjectName.isNotBlank()) {
                        viewModel.createProject(newProjectName)
                        showCreateDialog = false
                        newProjectName = ""
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Global Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingItem("Project Root", rootPath) { showFolderPickerRoot = true }
                    SettingItem("Download Location", downloadPath) { showFolderPickerDownload = true }
                    
                    val serverAddress by viewModel.serverAddress.collectAsState()
                    Column {
                        Text("Compile Server", style = MaterialTheme.typography.labelMedium, color = AccentColor)
                        TextField(
                            value = serverAddress,
                            onValueChange = { viewModel.updateServerAddress(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("TexSpace", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color.White)
                        Text("Cross-Platform LaTeX IDE", fontSize = 12.sp, color = AccentColor)
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
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CompileGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Project") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (projects.isEmpty()) {
                EmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(projects) { project ->
                        ProjectCard(project, 
                            onClick = { viewModel.openProject(project) },
                            onDelete = { viewModel.deleteProject(project) }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.ifBlank { "Not set" },
                style = MaterialTheme.typography.bodySmall,
                color = if (value.isBlank()) Color.Gray else Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            IconButton(onClick = onPickerRequest) {
                Icon(Icons.Default.FolderOpen, null, tint = AccentColor, modifier = Modifier.size(20.dp))
            }
        }
        HorizontalDivider(color = Color.White.copy(0.1f))
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LibraryAdd, null, modifier = Modifier.size(100.dp), tint = Color.Gray.copy(0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Projects Found", fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Configure a root path or create a new project", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProjectCard(project: LatexProject, onClick: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Description, null, tint = CompileGreen, modifier = Modifier.size(36.dp))
                Column {
                    Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White, maxLines = 1)
                    Text("Last modified: Just now", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete Project", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
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
    
    var showFilePicker by remember { mutableStateOf(false) }
    FilePicker(
        show = showFilePicker,
        onFilePicked = { if (it != null) viewModel.addExistingFile(it) },
        onDismiss = { showFilePicker = false }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 800.dp

        if (isMobile) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(selectedProject?.name ?: "Editor", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.goBackToDashboard() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                            }
                            BottomLogPanel(compilationLog, modifier = Modifier.height(120.dp))
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
                    HeaderRow(title = currentFile?.name ?: "Select a file", isEditor = true)
                    EditorPanel(
                        source = currentFile?.content ?: "",
                        onSourceChange = { viewModel.updateSource(it) },
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                    BottomLogPanel(compilationLog)
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
    isCompiling: Boolean = false
) {
    Surface(
        color = EditorHeaderBg,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold, 
                color = if (isEditor) AccentColor else Color.White,
                maxLines = 1,
                modifier = Modifier.weight(1f)
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
                        if (isCompiling) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compiling", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compile", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onExport?.invoke() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.FileDownload, "Download PDF", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
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

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New File") },
            text = {
                TextField(value = newFileName, onValueChange = { newFileName = it }, placeholder = { Text("filename.tex") })
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (newFileName.isNotBlank()) {
                        onCreateFile(newFileName)
                        showCreateDialog = false
                        newFileName = ""
                    }
                }) { Text("Create") }
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
                        onDelete = { onDeleteFile(file.id) },
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
            text = { TextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = {
                TextButton(onClick = { 
                    onRename(newName)
                    showRenameDialog = false 
                }) { Text("Rename") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(if (isSelected) AccentColor.copy(0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.name.endsWith(".tex")) Icons.Default.Description else Icons.Default.Article,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isSelected) AccentColor else Color.Gray
        )
        Spacer(modifier = Modifier.width(10.dp))
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
        color = Color(0xFF0A0C10),
        modifier = modifier.border(0.5.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("OUTPUT LOG", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AccentColor)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            color = Color(0xFFB0B0B0)
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