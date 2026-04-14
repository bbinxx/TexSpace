package com.bbinxx.texspace

import okio.FileSystem
import okio.Path
import okio.FileMetadata
import okio.Sink
import okio.Source
import okio.FileHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default

actual val defaultFileSystem: FileSystem = object : FileSystem() {
    override fun appendingSink(path: Path, mustExist: Boolean): Sink = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun atomicMove(source: Path, target: Path): Unit = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun canonicalize(path: Path): Path = path
    override fun createDirectory(dir: Path, mustCreate: Boolean): Unit = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun createSymlink(source: Path, target: Path): Unit = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun delete(path: Path, mustExist: Boolean): Unit = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun list(dir: Path): List<Path> = emptyList()
    override fun listOrNull(dir: Path): List<Path>? = null
    override fun metadataOrNull(path: Path): FileMetadata? = null
    override fun openReadOnly(file: Path): FileHandle = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun sink(path: Path, mustExist: Boolean): Sink = throw UnsupportedOperationException("FileSystem not supported on Wasm")
    override fun source(path: Path): Source = throw UnsupportedOperationException("FileSystem not supported on Wasm")
}