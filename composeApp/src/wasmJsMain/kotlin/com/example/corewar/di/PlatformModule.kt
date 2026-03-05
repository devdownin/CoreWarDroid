package com.example.corewar.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.example.corewar.db.MarsDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Worker
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        WebWorkerDriver(Worker("sqldelight-worker.js"))
    }
    single<DataStore<Preferences>> {
        // Use an in-memory or simpler storage for WasmJs if persistent path is problematic
        // In real KMP Wasm, a custom factory might be needed.
        PreferenceDataStoreFactory.createWithPath {
             "settings.preferences_pb".toPath()
        }
    }
}
