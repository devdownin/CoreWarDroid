package com.example.corewar

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.example.corewar.di.commonModule
import com.example.corewar.di.platformModule
import com.example.corewar.ui.App
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(commonModule, platformModule())
    }
    CanvasBasedWindow("CoreWar KMP") {
        App()
    }
}
