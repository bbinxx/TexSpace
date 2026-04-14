package com.bbinxx.texspace

import androidx.compose.runtime.Composable

@Composable
expect fun FolderPicker(
    show: Boolean,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
)
