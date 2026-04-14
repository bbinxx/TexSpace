package com.bbinxx.texspace

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import com.bbinxx.texspace.db.DriverFactory
import com.bbinxx.texspace.db.TexSpaceRepository
import com.bbinxx.texspace.db.createDatabase

fun MainViewController() = ComposeUIViewController { 
    val repository = remember { 
        TexSpaceRepository(createDatabase(DriverFactory())) 
    }
    val viewModel = remember { 
        LatexEditorViewModel(repository, ProjectRepository(), "https://texcompiler.onrender.com")
    }
    App(viewModel)
}