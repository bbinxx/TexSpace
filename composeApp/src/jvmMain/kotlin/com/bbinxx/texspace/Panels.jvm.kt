package com.bbinxx.texspace

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import java.util.Base64
import androidx.compose.ui.text.PlatformTextStyle

@Composable
actual fun getPlatformTextStyle(): PlatformTextStyle? = null

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
    if (pdfBase64 == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No PDF to display.")
        }
        return
    }

    val images = remember(pdfBase64) {
        try {
            val bytes = Base64.getDecoder().decode(pdfBase64)
            Loader.loadPDF(bytes).use { document ->
                val renderer = PDFRenderer(document)
                (0 until document.numberOfPages).map { pageIndex ->
                    renderer.renderImageWithDPI(pageIndex, 150f).toComposeImageBitmap()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    if (images.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error rendering PDF preview.")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(images.size) { index ->
                Image(
                    bitmap = images[index],
                    contentDescription = "PDF Page ${index + 1}",
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                )
            }
        }
    }
}
