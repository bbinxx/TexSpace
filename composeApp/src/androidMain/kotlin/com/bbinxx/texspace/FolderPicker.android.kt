package com.bbinxx.texspace

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FolderPicker(
    show: Boolean,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val path = getPathFromUri(context, uri)
            onFolderPicked(path)
        } else {
            onFolderPicked(null)
        }
        onDismiss()
    }

    if (show) {
        LaunchedEffect(Unit) {
            launcher.launch(null)
        }
    }
}

private fun getPathFromUri(context: Context, uri: Uri): String? {
    // Basic conversion for common cases
    if ("com.android.externalstorage.documents" == uri.authority) {
        val docId = try { DocumentsContract.getTreeDocumentId(uri) } catch (e: Exception) { return uri.path }
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
            return Environment.getExternalStorageDirectory().toString() + "/" + if (split.size > 1) split[1] else ""
        }
    }
    return uri.path
}
