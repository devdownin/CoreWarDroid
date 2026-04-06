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
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okio.Path.Companion.toPath

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        WebWorkerDriver(Worker("sqldelight-worker.js"))
    }
    single<DataStore<Preferences>> {
        object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flowOf(emptyPreferences())
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                val current = emptyPreferences()
                return transform(current)
            }
        }
    }
}
