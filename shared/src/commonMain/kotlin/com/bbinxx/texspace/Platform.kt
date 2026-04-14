package com.bbinxx.texspace

import okio.FileSystem
import kotlinx.coroutines.CoroutineDispatcher

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val ioDispatcher: CoroutineDispatcher
expect val defaultFileSystem: FileSystem