package com.bbinxx.texspace.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): TexSpaceDatabase {
    return TexSpaceDatabase(driverFactory.createDriver())
}
