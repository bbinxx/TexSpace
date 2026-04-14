package com.bbinxx.texspace

import platform.UIKit.UIDevice
import okio.FileSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
actual val defaultFileSystem: FileSystem = FileSystem.SYSTEM