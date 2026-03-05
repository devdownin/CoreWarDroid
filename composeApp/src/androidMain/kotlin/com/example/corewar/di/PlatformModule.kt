package com.example.corewar.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.corewar.db.MarsDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(MarsDatabase.Schema, get(), "mars.db")
    }
    single<DataStore<Preferences>> {
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            get<Context>().preferencesDataStoreFile("settings")
        }
    }
}
