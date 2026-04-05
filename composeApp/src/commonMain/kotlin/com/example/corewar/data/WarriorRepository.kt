package com.example.corewar.data

import com.example.corewar.db.MarsDatabase
import com.example.corewar.model.Instruction
import com.example.corewar.model.Opcode
import com.example.corewar.engine.RedcodeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

class WarriorRepository(
    private val database: MarsDatabase,
    private val parser: RedcodeParser
) {
    private val queries = database.warriorQueries

    suspend fun getAllWarriors() = withContext(Dispatchers.Default) {
        runCatching { queries.selectAllWarriors().executeAsList() }.getOrDefault(emptyList())
    }

    suspend fun saveWarrior(name: String, code: String) = withContext(Dispatchers.Default) {
        runCatching { queries.insertWarrior(name, code) }
    }

    suspend fun deleteWarrior(id: Long) = withContext(Dispatchers.Default) {
        runCatching { queries.deleteWarrior(id) }
    }

    suspend fun saveBattleResult(winnerName: String?, warriors: List<String>, status: String) = withContext(Dispatchers.Default) {
        runCatching {
            queries.insertBattle(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                winner_name = winnerName,
                warriors_involved = warriors.joinToString(","),
                status = status
            )
        }
    }

    suspend fun getAllBattles() = withContext(Dispatchers.Default) {
        runCatching { queries.selectAllBattles().executeAsList() }.getOrDefault(emptyList())
    }

    fun exportWarriorToJson(name: String, code: String): String {
        val warriorData = mapOf("name" to name, "code" to code)
        return Json.encodeToString(warriorData)
    }

    fun importWarriorFromJson(jsonStr: String): Pair<String, String>? {
        return try {
            val data: Map<String, String> = Json.decodeFromString(jsonStr)
            data["name"]?.let { name ->
                data["code"]?.let { code ->
                    name to code
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getPreloadedWarriors(): List<Pair<String, String>> {
        return listOf(
            "Imp" to "MOV 0, 1",
            "Dwarf" to """
                ADD #4, 3
                MOV 2, @2
                JMP -2
                DAT #0
            """.trimIndent(),
            "Stone" to """
                SPL 0, <-10
                MOV 2, <-1
                JMP -2, <-12
                DAT #0, #0
            """.trimIndent(),
            "Paper" to """
                SPL 1, <300
                SPL 1, <400
                MOV <0, <1
            """.trimIndent(),
            "Vampire" to """
                ADD #10, 3
                MOV 2, @2
                JMP -2
                DAT #0
            """.trimIndent()
        )
    }
}
