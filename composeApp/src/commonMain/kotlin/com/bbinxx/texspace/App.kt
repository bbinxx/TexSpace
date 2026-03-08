package com.bbinxx.texspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    val sourceCode by viewModel.sourceCode.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compilationLog by viewModel.compilationLog.collectAsState()
    val compiledPdfBase64 by viewModel.compiledPdfBase64.collectAsState()
    val autoCompile by viewModel.autoCompile.collectAsState()

    Column(modifier = Modifier.fillMaxSize().onKeyEvent { 
        if (it.isCtrlPressed) {
            when (it.key) {
                Key.S -> { viewModel.save(); true }
                Key.Enter -> { viewModel.compile(); true }
                else -> false
            }
        } else false
    }) {
        Row(modifier = Modifier.weight(1f)) {
            // 1. LEFT SIDEBAR RAIL
            SidebarRail()

            // 2. FILE TREE PANEL
            FileTreePanel()

            // 3. EDITOR PANEL (Center)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                HeaderRow(title = "main.tex", isEditor = true)
                Column(modifier = Modifier.weight(1f)) {
                    EditorPanel(
                        source = sourceCode,
                        onSourceChange = { viewModel.updateSource(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // BOTTOM LOG PANEL
                BottomLogPanel(compilationLog)
            }

            // 4. PREVIEW PANEL (Right)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                HeaderRow(
                    title = "Preview", 
                    isEditor = false, 
                    onRecompile = { viewModel.compile() },
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

@Composable
fun SidebarRail() {
    Column(
        modifier = Modifier.width(48.dp).fillMaxHeight().background(SidebarRailBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        SidebarIcon(Icons.Default.Menu)
        SidebarIcon(Icons.Default.AccountCircle)
        SidebarIcon(Icons.Default.Settings)
    }
}

@Composable
fun SidebarIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp).clickable {},
        tint = Color.Gray
    )
}

@Composable
fun HeaderRow(
    title: String, 
    isEditor: Boolean, 
    onRecompile: (() -> Unit)? = null,
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
                Spacer(modifier = Modifier.width(16.dp))
                Text("Visual Editor", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text(title, fontSize = 12.sp, color = Color.LightGray)
            } else {
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
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                Text("1 / 1", fontSize = 12.sp, color = Color.Gray)
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun FileTreePanel() {
    Surface(
        color = SidebarBg,
        modifier = Modifier.width(220.dp).fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("File tree", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Menu, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    FileItem(Icons.Default.Description, "main.tex", isSelected = true)
                }
                item {
                    FileItem(Icons.Default.Description, "bib_resume.tex", isSelected = false)
                }
            }
        }
    }
}

@Composable
fun FileItem(icon: ImageVector, name: String, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF41474D) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable {}
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.White else Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 13.sp, color = if (isSelected) Color.White else Color.Gray)
    }
}

@Composable
fun BottomLogPanel(log: String) {
    Surface(
        color = Color(0xFF131516),
        modifier = Modifier.fillMaxWidth().height(120.dp)
            .border(0.5.dp, Color.Black.copy(0.3f))
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