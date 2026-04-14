package com.bbinxx.texspace

import androidx.compose.runtime.Composable

@Composable
actual fun FolderPicker(
    show: Boolean,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    // Web implementation (File System Access API is complex to bridge here)
    // Placeholder
}
