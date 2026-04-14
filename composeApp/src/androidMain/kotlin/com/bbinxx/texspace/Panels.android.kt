package com.bbinxx.texspace

<<<<<<< HEAD
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
=======
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
>>>>>>> dev

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
<<<<<<< HEAD
    FallbackPdfPreviewPanel(pdfBase64, modifier)
=======
    if (pdfBase64.isNullOrEmpty()) {
        FallbackPdfPreviewPanel(null, modifier)
        return
    }

    val context = LocalContext.current
    val bitmaps = remember(pdfBase64) {
        try {
            val bytes = Base64.decode(pdfBase64, Base64.DEFAULT)
            val tempFile = File(context.cacheDir, "preview.pdf")
            val outputStream = FileOutputStream(tempFile)
            outputStream.write(bytes)
            outputStream.close()

            val result = mutableListOf<Bitmap>()
            val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                result.add(bitmap)
                page.close()
            }
            renderer.close()
            fd.close()
            result
        } catch (e: Exception) {
            emptyList<Bitmap>()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF525659))) {
        if (bitmaps.isEmpty()) {
            FallbackPdfPreviewPanel(pdfBase64, modifier)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(bitmaps) { bitmap ->
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
>>>>>>> dev
}
