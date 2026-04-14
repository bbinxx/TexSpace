package com.bbinxx.texspace.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val userHome = System.getProperty("user.home")
        val databaseDir = File(userHome, ".texspace")
        if (!databaseDir.exists()) {
            databaseDir.mkdirs()
        }
        val databaseFile = File(databaseDir, "texspace.db")
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        try {
            TexSpaceDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Already initialized or something else
        }
        return driver
    }
}
