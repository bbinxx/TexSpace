package com.bbinxx.texspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
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
        TopBar(
            onCompile = { viewModel.compile() },
            autoCompile = autoCompile,
            onToggleAutoCompile = { viewModel.toggleAutoCompile() },
            isCompiling = isCompiling
        )
        Row(modifier = Modifier.weight(1f)) {
            // LEFT PANEL
            FileTreePanel()

            // CENTER PANEL
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                EditorPanel(
                    source = sourceCode,
                    onSourceChange = { viewModel.updateSource(it) },
                    modifier = Modifier.weight(1f)
                )
                // BOTTOM PANEL
                BottomLogPanel(compilationLog)
            }

            // RIGHT PANEL
            PdfPreviewPanel(
                pdfBase64 = compiledPdfBase64,
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White)
            )
        }
    }
}

@Composable
fun TopBar(
    onCompile: () -> Unit,
    autoCompile: Boolean,
    onToggleAutoCompile: () -> Unit,
    isCompiling: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TexSpace", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(32.dp))
            Button(onClick = onCompile, enabled = !isCompiling) {
                Text(if (isCompiling) "Compiling..." else "Compile")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = autoCompile, onCheckedChange = { onToggleAutoCompile() })
                Text("Auto Compile")
            }
        }
    }
}

@Composable
fun FileTreePanel() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.width(200.dp).fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Project Files", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                item {
                    Text("main.tex", modifier = Modifier.fillMaxWidth().clickable {}.padding(4.dp))
                }
            }
        }
    }
}

@Composable
fun BottomLogPanel(log: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Compilation Log", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = log,
                onValueChange = {},
                readOnly = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace),
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