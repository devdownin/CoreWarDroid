package com.example.corewar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corewar.data.UserSettingsRepository
import com.example.corewar.data.WarriorRepository
import com.example.corewar.engine.MarsEngine
import com.example.corewar.engine.RedcodeParser
import com.example.corewar.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BattleUiState(
    val battleState: BattleState? = null,
    val speed: Long = 10, // 1ms to 500ms delay
    val xpGained: Int = 0,
    val isLoading: Boolean = false,
    val selectedCell: MemoryCell? = null,
    val selectedCellIndex: Int? = null,
    val warriorStats: Map<Int, WarriorStats> = emptyMap()
)

data class WarriorStats(
    val initialThreads: Int,
    val cellsOwned: Int,
    val processesCreated: Int,
    val survivalCycles: Int
)

sealed class BattleIntent {
    data class StartBattle(val warriors: List<Pair<String, String>>, val chaosMode: Boolean) : BattleIntent()
    object Step : BattleIntent()
    object PauseResume : BattleIntent()
    data class SetSpeed(val speed: Long) : BattleIntent()
    data class SelectCell(val index: Int) : BattleIntent()
    object Restart : BattleIntent()
}

class BattleViewModel(
    private val engine: MarsEngine,
    private val parser: RedcodeParser,
    private val userSettingsRepository: UserSettingsRepository,
    private val warriorRepository: WarriorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private var battleJob: Job? = null

    fun handleIntent(intent: BattleIntent) {
        when (intent) {
            is BattleIntent.StartBattle -> startBattle(intent.warriors, intent.chaosMode)
            BattleIntent.Step -> step()
            BattleIntent.PauseResume -> pauseResume()
            is BattleIntent.SetSpeed -> setSpeed(intent.speed)
            is BattleIntent.SelectCell -> selectCell(intent.index)
            BattleIntent.Restart -> restart()
        }
    }

    private fun startBattle(warriorSources: List<Pair<String, String>>, chaosMode: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val xp = userSettingsRepository.totalXp.first()
            val level = userSettingsRepository.getLevel(xp)
            val skills = userSettingsRepository.unlockedSkills.first()
            val unlockedPowers = userSettingsRepository.getUnlockedPowers(level, skills)

            val warriors = warriorSources.map { (name, code) ->
                name to parser.parse(code)
            }

            val initialState = engine.loadWarriors(warriors, chaosMode = chaosMode)

            // Add powers to warriors based on level/skills
            val stateWithPowers = initialState.copy(
                warriors = initialState.warriors.map { it.copy(specialPowers = unlockedPowers) }
            )

            _uiState.update { it.copy(battleState = stateWithPowers, isLoading = false) }
            runBattleLoop()
        }
    }

    private fun runBattleLoop() {
        battleJob?.cancel()
        battleJob = viewModelScope.launch {
            val state = _uiState.value.battleState ?: return@launch
            engine.runWithDelay(state, _uiState.value.speed) { nextState ->
                _uiState.update { it.copy(battleState = nextState) }
                if (nextState.status != BattleStatus.RUNNING && nextState.status != BattleStatus.PAUSED && nextState.status != BattleStatus.IDLE) {
                    viewModelScope.launch {
                        handleBattleEnd(nextState)
                    }
                }
            }
        }
    }

    private suspend fun handleBattleEnd(state: BattleState) {
        val xp = when (state.status) {
            BattleStatus.WARRIOR_WINS -> if (state.winnerId == 0) 50 else 5
            BattleStatus.DRAW -> 20
            else -> 0
        }
        userSettingsRepository.addXp(xp)

        val winnerName = state.winnerId?.let { state.warriors.getOrNull(it)?.name }
        val warriorNames = state.warriors.map { it.name }
        warriorRepository.saveBattleResult(winnerName, warriorNames, state.status.name)

        val finalStats = state.warriors.associate { warrior ->
            warrior.id to WarriorStats(
                initialThreads = 1,
                cellsOwned = state.memory.count { it.ownerId == warrior.id },
                processesCreated = 0, // Simplified for now
                survivalCycles = state.cycle
            )
        }

        _uiState.update { it.copy(xpGained = xp, warriorStats = finalStats) }
    }

    private fun step() {
        val state = _uiState.value.battleState ?: return
        val nextState = engine.step(state)
        _uiState.update { it.copy(battleState = nextState) }
    }

    private fun pauseResume() {
        val state = _uiState.value.battleState ?: return
        val nextStatus = if (state.status == BattleStatus.RUNNING) BattleStatus.PAUSED else BattleStatus.RUNNING
        _uiState.update { it.copy(battleState = state.copy(status = nextStatus)) }
        if (nextStatus == BattleStatus.RUNNING) {
            runBattleLoop()
        } else {
            battleJob?.cancel()
        }
    }

    private fun setSpeed(speed: Long) {
        _uiState.update { it.copy(speed = speed) }
        if (_uiState.value.battleState?.status == BattleStatus.RUNNING) {
            runBattleLoop()
        }
    }

    private fun selectCell(index: Int) {
        val cell = _uiState.value.battleState?.memory?.getOrNull(index)
        _uiState.update { it.copy(selectedCell = cell, selectedCellIndex = index) }
    }

    private fun restart() {
        battleJob?.cancel()
        _uiState.update { BattleUiState() }
    }
}
