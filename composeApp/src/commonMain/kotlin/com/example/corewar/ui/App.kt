package com.example.corewar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.corewar.data.UserSettingsRepository
import com.example.corewar.data.WarriorRepository
import com.example.corewar.ui.screens.*
import com.example.corewar.ui.viewmodel.BattleViewModel
import com.example.corewar.ui.viewmodel.EditorViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object HomeRoute

@Serializable
data class BattleRoute(val warriors: List<Pair<String, String>>, val chaosMode: Boolean)

@Serializable
data class EditorRoute(val initialName: String? = null, val initialCode: String? = null)

@Serializable
object SettingsRoute

@Serializable
object HelpRoute

@Composable
fun App() {
    val userSettingsRepository: UserSettingsRepository = koinInject()
    val theme by userSettingsRepository.theme.collectAsState("STANDARD")

    CoreWarTheme(themeName = theme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val warriorRepository: WarriorRepository = koinInject()

            NavHost(
                navController = navController,
                startDestination = HomeRoute
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        warriorRepository = warriorRepository,
                        userSettingsRepository = userSettingsRepository,
                        onStartBattle = { warriors, chaosMode ->
                            navController.navigate(BattleRoute(warriors, chaosMode))
                        },
                        onOpenEditor = { name, code ->
                            navController.navigate(EditorRoute(name, code))
                        },
                        onOpenSettings = {
                            navController.navigate(SettingsRoute)
                        },
                        onOpenHelp = {
                            navController.navigate(HelpRoute)
                        }
                    )
                }

                composable<BattleRoute> { backStackEntry ->
                    val route: BattleRoute = backStackEntry.toRoute()
                    val viewModel: BattleViewModel = koinViewModel()
                    BattleScreen(
                        viewModel = viewModel,
                        warriors = route.warriors,
                        chaosMode = route.chaosMode,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<EditorRoute> { backStackEntry ->
                    val route: EditorRoute = backStackEntry.toRoute()
                    val viewModel: EditorViewModel = koinViewModel()
                    EditorScreen(
                        viewModel = viewModel,
                        initialName = route.initialName,
                        initialCode = route.initialCode,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        userSettingsRepository = userSettingsRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<HelpRoute> {
                    HelpScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
