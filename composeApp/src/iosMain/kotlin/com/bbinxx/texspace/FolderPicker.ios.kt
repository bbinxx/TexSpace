package com.bbinxx.texspace

import androidx.compose.runtime.Composable

@Composable
actual fun FolderPicker(
    show: Boolean,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    // iOS implementation would use UIDocumentPickerViewController
    // Placeholder for now
}
