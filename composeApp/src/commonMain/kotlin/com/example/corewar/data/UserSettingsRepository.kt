package com.example.corewar.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.corewar.model.SpecialPower
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(private val dataStore: DataStore<Preferences>) {

    private val totalXpKey = intPreferencesKey("total_xp")
    private val themeKey = stringPreferencesKey("theme")
    private val chaosModeKey = booleanPreferencesKey("chaos_mode")

    val totalXp: Flow<Int> = dataStore.data.map { preferences ->
        preferences[totalXpKey] ?: 0
    }

    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[themeKey] ?: "STANDARD"
    }

    val chaosMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[chaosModeKey] ?: false
    }

    suspend fun addXp(xp: Int) {
        dataStore.edit { preferences ->
            val currentXp = preferences[totalXpKey] ?: 0
            preferences[totalXpKey] = currentXp + xp
        }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[themeKey] = theme
        }
    }

    suspend fun setChaosMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[chaosModeKey] = enabled
        }
    }

    fun getLevel(xp: Int): Int = 1 + (xp / 100)

    fun getUnlockedOpcodes(level: Int): Set<com.example.corewar.model.Opcode> {
        val unlocked = mutableSetOf(
            com.example.corewar.model.Opcode.MOV,
            com.example.corewar.model.Opcode.ADD,
            com.example.corewar.model.Opcode.JMP,
            com.example.corewar.model.Opcode.JMZ,
            com.example.corewar.model.Opcode.JMN,
            com.example.corewar.model.Opcode.DJN,
            com.example.corewar.model.Opcode.DAT
        )
        if (level >= 3) {
            unlocked.add(com.example.corewar.model.Opcode.SPL)
            unlocked.add(com.example.corewar.model.Opcode.CMP)
            unlocked.add(com.example.corewar.model.Opcode.SLT)
        }
        if (level >= 5) {
            unlocked.add(com.example.corewar.model.Opcode.SUB)
            unlocked.add(com.example.corewar.model.Opcode.MUL)
            unlocked.add(com.example.corewar.model.Opcode.DIV)
            unlocked.add(com.example.corewar.model.Opcode.MOD)
            unlocked.add(com.example.corewar.model.Opcode.NOP)
        }
        return unlocked
    }

    fun getUnlockedPowers(level: Int): Set<SpecialPower> {
        val powers = mutableSetOf<SpecialPower>()
        if (level >= 6) powers.add(SpecialPower.PROCESS_SHIELD)
        if (level >= 8) powers.add(SpecialPower.SPEED_BOOST)
        if (level >= 10) powers.add(SpecialPower.REDUCE_PENALTY)
        return powers
    }
}
