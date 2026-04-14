package com.bbinxx.texspace

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(
    show: Boolean,
    onFilePicked: (String?) -> Unit,
    onDismiss: () -> Unit
)
