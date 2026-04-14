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
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val path = getFilePathFromUri(context, uri)
            onFilePicked(path)
        } else {
            onFilePicked(null)
        }
        onDismiss()
    }

    if (show) {
        LaunchedEffect(Unit) {
            launcher.launch(arrayOf("*/*"))
        }
    }
}

private fun getFilePathFromUri(context: Context, uri: Uri): String? {
    if ("com.android.externalstorage.documents" == uri.authority) {
        val docId = try { DocumentsContract.getDocumentId(uri) } catch (e: Exception) { return uri.path }
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
            return Environment.getExternalStorageDirectory().toString() + "/" + if (split.size > 1) split[1] else ""
        }
    }
    return uri.path
}
