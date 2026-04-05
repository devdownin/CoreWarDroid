package com.example.corewar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corewar.data.UserSettingsRepository
import com.example.corewar.data.WarriorRepository
import com.example.corewar.engine.RedcodeParser
import com.example.corewar.model.Instruction
import com.example.corewar.model.Opcode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditorUiState(
    val code: String = "",
    val name: String = "",
    val errors: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val unlockedOpcodes: Set<Opcode> = emptySet(),
    val level: Int = 1
)

sealed class EditorIntent {
    data class CodeChanged(val code: String) : EditorIntent()
    data class NameChanged(val name: String) : EditorIntent()
    object SaveWarrior : EditorIntent()
    data class LoadWarrior(val name: String, val code: String) : EditorIntent()
    data class ImportWarrior(val json: String) : EditorIntent()
    object ExportWarrior : EditorIntent()
}

class EditorViewModel(
    private val warriorRepository: WarriorRepository,
    private val parser: RedcodeParser,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<String>()
    val exportEvent = _exportEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(userSettingsRepository.totalXp, userSettingsRepository.unlockedSkills) { xp, skills ->
                val level = userSettingsRepository.getLevel(xp)
                val unlocked = userSettingsRepository.getUnlockedOpcodes(level, skills)
                _uiState.update { it.copy(level = level, unlockedOpcodes = unlocked) }
            }.collect()
        }
    }

    fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.CodeChanged -> onCodeChanged(intent.code)
            is EditorIntent.NameChanged -> onNameChanged(intent.name)
            EditorIntent.SaveWarrior -> saveWarrior()
            is EditorIntent.LoadWarrior -> _uiState.update { it.copy(name = intent.name, code = intent.code) }
            is EditorIntent.ImportWarrior -> importWarrior(intent.json)
            EditorIntent.ExportWarrior -> exportWarrior()
        }
    }

    private fun onCodeChanged(code: String) {
        val validationErrors = parser.validate(code).map { "Line ${it.line}: ${it.message}" }
        val errors = if (validationErrors.isEmpty()) {
            try {
                val instructions = parser.parse(code)
                val usedOpcodes = instructions.map { it.opcode }.toSet()
                val lockedUsed = usedOpcodes - _uiState.value.unlockedOpcodes
                if (lockedUsed.isNotEmpty()) {
                    listOf("Locked opcodes: ${lockedUsed.joinToString(", ")}")
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                listOf(e.message ?: "Unknown parse error")
            }
        } else {
            validationErrors
        }
        _uiState.update { it.copy(code = code, errors = errors) }
    }

    private fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    private fun saveWarrior() {
        val currentState = _uiState.value
        if (currentState.errors.isEmpty() && currentState.name.isNotBlank()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                warriorRepository.saveWarrior(currentState.name, currentState.code)
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun importWarrior(json: String) {
        warriorRepository.importWarriorFromJson(json)?.let { (name, code) ->
            _uiState.update { it.copy(name = name, code = code) }
            onCodeChanged(code)
        }
    }

    private fun exportWarrior() {
        viewModelScope.launch {
            val json = warriorRepository.exportWarriorToJson(_uiState.value.name, _uiState.value.code)
            _exportEvent.emit(json)
        }
    }
}
