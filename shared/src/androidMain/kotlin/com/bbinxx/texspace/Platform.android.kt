package com.bbinxx.texspace

import android.os.Build
import okio.FileSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
actual val defaultFileSystem: FileSystem = FileSystem.SYSTEM