package com.bbinxx.texspace

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun EditorPanel(
    source: String,
    onSourceChange: (String) -> Unit,
    modifier: Modifier
) {
    TextFieldEditorPanel(source, onSourceChange, modifier)
}

@Composable
actual fun PdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier
) {
    FallbackPdfPreviewPanel(pdfBase64, modifier)
}
