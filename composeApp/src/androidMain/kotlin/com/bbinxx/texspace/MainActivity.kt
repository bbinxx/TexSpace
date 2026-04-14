package com.bbinxx.texspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bbinxx.texspace.db.DriverFactory
import com.bbinxx.texspace.db.TexSpaceRepository
import com.bbinxx.texspace.db.createDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request full file access on Android 11+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        setContent {
            val repository = remember { 
                TexSpaceRepository(createDatabase(DriverFactory(this@MainActivity))) 
            }
            val viewModel: LatexEditorViewModel = viewModel { 
                LatexEditorViewModel(repository, ProjectRepository(), "https://texcompiler.onrender.com")
            }
            App(viewModel)
        }
    }
}

// @Preview
// @Composable
// fun AppAndroidPreview() {
//     App()
// }