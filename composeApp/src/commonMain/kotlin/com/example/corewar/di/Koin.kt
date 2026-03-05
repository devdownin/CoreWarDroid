package com.example.corewar.di

import com.example.corewar.data.UserSettingsRepository
import com.example.corewar.data.WarriorRepository
import com.example.corewar.db.MarsDatabase
import com.example.corewar.engine.MarsEngine
import com.example.corewar.engine.RedcodeParser
import com.example.corewar.ui.viewmodel.BattleViewModel
import com.example.corewar.ui.viewmodel.EditorViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single { MarsEngine() }
    single { RedcodeParser() }

    single { MarsDatabase(get()) }
    single { WarriorRepository(get(), get()) }
    single { UserSettingsRepository(get()) }

    viewModel { BattleViewModel(get(), get(), get(), get()) }
    viewModel { EditorViewModel(get(), get(), get()) }
}

expect fun platformModule(): Module
