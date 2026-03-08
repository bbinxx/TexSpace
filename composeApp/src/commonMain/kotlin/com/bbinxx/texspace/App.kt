package com.bbinxx.texspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Color Palette inspired by Overleaf Dark
val SidebarBg = Color(0xFF2A2E33)
val FileTreeBg = Color(0xFF1E2124)
val EditorHeaderBg = Color(0xFF2E3238)
val CompileGreen = Color(0xFF4C9F4C)
val SidebarRailBg = Color(0xFF131516)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF131516),
            surface = Color(0xFF1E2124),
            primary = CompileGreen
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val viewModel: LatexEditorViewModel = viewModel { LatexEditorViewModel() }
            MainLayout(viewModel)
        }
    }
}

@Composable
fun MainLayout(viewModel: LatexEditorViewModel) {
    val files by viewModel.files.collectAsState()
    val selectedFileId by viewModel.selectedFileId.collectAsState()
    val isFileTreeVisible by viewModel.isFileTreeVisible.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compilationLog by viewModel.compilationLog.collectAsState()
    val compiledPdfBase64 by viewModel.compiledPdfBase64.collectAsState()

    val currentFile = files.find { it.id == selectedFileId }
    var activeTab by remember { mutableStateOf(0) } // 0: Editor, 1: Preview, 2: Files

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        if (isMobile) {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = SidebarRailBg) {
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
                            label = { Text("Edit") }
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
                            onCreateFile = { viewModel.createFile() },
                            onDeleteFile = { viewModel.deleteFile(it) },
                            onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                            onMinimize = {},
                            modifier = Modifier.fillMaxSize()
                        )
                        0 -> Column(modifier = Modifier.fillMaxSize()) {
                            HeaderRow(title = currentFile?.name ?: "", isEditor = true)
                            EditorPanel(
                                source = currentFile?.content ?: "",
                                onSourceChange = { viewModel.updateSource(it) },
                                modifier = Modifier.weight(1f).fillMaxSize()
                            )
                        }
                        1 -> Column(modifier = Modifier.fillMaxSize()) {
                            HeaderRow(
                                title = "Preview", 
                                isEditor = false, 
                                onRecompile = { viewModel.compile() },
                                onExport = { viewModel.exportPdf() },
                                isCompiling = isCompiling
                            )
                            PdfPreviewPanel(
                                pdfBase64 = compiledPdfBase64,
                                modifier = Modifier.weight(1f).fillMaxSize().background(Color.White)
                            )
                            BottomLogPanel(compilationLog, modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        } else {
            // Desktop Layout
            Row(modifier = Modifier.fillMaxSize().onKeyEvent { 
                if (it.isCtrlPressed) {
                    when (it.key) {
                        Key.S -> { viewModel.save(); true }
                        Key.Enter -> { viewModel.compile(); true }
                        else -> false
                    }
                } else false
            }) {
                SidebarRail(onMenuClick = { viewModel.toggleFileTree() })

                if (isFileTreeVisible) {
                    FileTreePanel(
                        files = files,
                        selectedFileId = selectedFileId,
                        onFileSelected = { viewModel.selectFile(it) },
                        onCreateFile = { viewModel.createFile() },
                        onDeleteFile = { viewModel.deleteFile(it) },
                        onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                        onMinimize = { viewModel.toggleFileTree() }
                    )
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    HeaderRow(title = currentFile?.name ?: "", isEditor = true)
                    Column(modifier = Modifier.weight(1f)) {
                        EditorPanel(
                            source = currentFile?.content ?: "",
                            onSourceChange = { viewModel.updateSource(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    BottomLogPanel(compilationLog)
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    HeaderRow(
                        title = "Preview", 
                        isEditor = false, 
                        onRecompile = { viewModel.compile() },
                        onExport = { viewModel.exportPdf() },
                        isCompiling = isCompiling
                    )
                    PdfPreviewPanel(
                        pdfBase64 = compiledPdfBase64,
                        modifier = Modifier.weight(1f).fillMaxSize().background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarRail(onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier.width(48.dp).fillMaxHeight().background(SidebarRailBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        SidebarIcon(Icons.Default.Menu, onClick = onMenuClick)
        SidebarIcon(Icons.Default.AccountCircle)
        SidebarIcon(Icons.Default.Settings)
    }
}

@Composable
fun SidebarIcon(icon: ImageVector, onClick: () -> Unit = {}) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp).clickable { onClick() },
        tint = Color.Gray
    )
}

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
        modifier = Modifier.fillMaxWidth().height(40.dp).border(0.5.dp, Color.Black.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditor) {
                Text("Code Editor", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CompileGreen)
                Spacer(modifier = Modifier.weight(1f))
                Text(title, fontSize = 12.sp, color = Color.LightGray)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { onRecompile?.invoke() },
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CompileGreen),
                        enabled = !isCompiling
                    ) {
                        Text(if (isCompiling) "Compiling..." else "Recompile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onExport?.invoke() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.FileDownload, 
                            contentDescription = "Export PDF", 
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                Text("1 / 1", fontSize = 12.sp, color = Color.Gray)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun FileTreePanel(
    files: List<LatexFile>,
    selectedFileId: String,
    onFileSelected: (String) -> Unit,
    onCreateFile: () -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (String, String) -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier.width(220.dp)
) {
    Surface(
        color = SidebarBg,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("File tree", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.Add, 
                    null, 
                    modifier = Modifier.size(18.dp).clickable { onCreateFile() }, 
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Menu, 
                    null, 
                    modifier = Modifier.size(18.dp).clickable { onMinimize() }, 
                    tint = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            title = { Text("Rename File") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    onRename(newName)
                    showRenameDialog = false 
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF41474D) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { onSelect() }
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.White else Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(file.name, fontSize = 13.sp, color = if (isSelected) Color.White else Color.Gray, modifier = Modifier.weight(1f))
        
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { 
                        showMenu = false
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { 
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomLogPanel(log: String, modifier: Modifier = Modifier.fillMaxWidth().height(120.dp)) {
    Surface(
        color = Color(0xFF131516),
        modifier = modifier.border(0.5.dp, Color.Black.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("LOGS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF858585)
                ),
                modifier = Modifier.fillMaxSize()
            )
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