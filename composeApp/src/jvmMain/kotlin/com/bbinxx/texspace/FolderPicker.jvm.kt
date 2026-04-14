package com.bbinxx.texspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun FolderPicker(
    show: Boolean,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        LaunchedEffect(Unit) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true")
            val dialog = FileDialog(null as Frame?, "Select Project Folder", FileDialog.LOAD)
            dialog.isVisible = true
            val directory = dialog.directory
            if (directory != null) {
                onFolderPicked(directory)
            } else {
                onFolderPicked(null)
            }
            onDismiss()
        }
    }
}
