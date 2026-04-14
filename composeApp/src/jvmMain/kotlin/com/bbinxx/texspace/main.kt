package com.bbinxx.texspace

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.bbinxx.texspace.db.DriverFactory
import com.bbinxx.texspace.db.TexSpaceRepository
import com.bbinxx.texspace.db.createDatabase

fun main() = application {
    val repository = remember { 
        TexSpaceRepository(createDatabase(DriverFactory())) 
    }
    val viewModel = remember { 
        LatexEditorViewModel(repository, ProjectRepository(), "https://texcompiler.onrender.com")
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "TexSpace",
    ) {
        App(viewModel)
    }
}