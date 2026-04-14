package com.bbinxx.texspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        LaunchedEffect(Unit) {
            val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
            dialog.isVisible = true
            val directory = dialog.directory
            val file = dialog.file
            if (directory != null && file != null) {
                onFilePicked(directory + file)
            } else {
                onFilePicked(null)
            }
            onDismiss()
        }
    }
}
