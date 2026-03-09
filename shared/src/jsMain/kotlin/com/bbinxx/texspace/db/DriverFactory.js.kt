package com.bbinxx.texspace.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        // Placeholder for web worker setup
        return WebWorkerDriver(Worker("sqldelight.worker.js"))
    }
}
