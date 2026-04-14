package com.bbinxx.texspace.db

import kotlinx.coroutines.flow.Flow
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class TexSpaceRepository(private val database: TexSpaceDatabase) {
    private val queries = database.texSpaceQueries

    fun getAllFiles(): Flow<List<LatexFile>> {
        return queries.getAllFiles { id, name, content, lastModified ->
            LatexFile(id, name, content, lastModified)
        }.asFlow().mapToList(Dispatchers.Default)
    }

    suspend fun createFile(id: String, name: String, content: String) = withContext(Dispatchers.Default) {
        queries.insertFile(id, name, content, Clock.System.now().toEpochMilliseconds())
    }

    suspend fun updateFileContent(id: String, content: String) = withContext(Dispatchers.Default) {
        queries.updateFileContent(content, Clock.System.now().toEpochMilliseconds(), id)
    }

    suspend fun renameFile(id: String, name: String) = withContext(Dispatchers.Default) {
        queries.renameFile(name, Clock.System.now().toEpochMilliseconds(), id)
    }

    suspend fun deleteFile(id: String) = withContext(Dispatchers.Default) {
        queries.deleteFile(id)
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.Default) {
        queries.setSetting(key, value)
    }

    suspend fun getSetting(key: String): String? = withContext(Dispatchers.Default) {
        queries.getSetting(key).executeAsOneOrNull()
    }
}
