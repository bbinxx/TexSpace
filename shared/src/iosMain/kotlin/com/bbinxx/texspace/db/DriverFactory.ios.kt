package com.bbinxx.texspace.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(TexSpaceDatabase.Schema, "texspace.db")
    }
}
