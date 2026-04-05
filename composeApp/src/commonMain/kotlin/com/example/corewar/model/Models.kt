package com.example.corewar.model

import kotlinx.serialization.Serializable

@Serializable
data class CoreWarColor(val argb: Long)

enum class Opcode {
    MOV, ADD, SUB, MUL, DIV, MOD, JMP, JMZ, JMN, DJN, SPL, CMP, SLT, DAT, NOP
}

enum class AddressMode {
    IMMEDIATE, // #
    DIRECT,    // $
    INDIRECT_B, // @
    PRE_DEC_B,  // <
    POST_INC_B, // >
    INDIRECT_A, // *
    PRE_DEC_A,  // {
    POST_INC_A  // }
}

@Serializable
data class Instruction(
    val opcode: Opcode,
    val modeA: AddressMode = AddressMode.DIRECT,
    val valueA: Int = 0,
    val modeB: AddressMode = AddressMode.DIRECT,
    val valueB: Int = 0
)

enum class CellType {
    NORMAL, PROTECTED, VOLATILE
}

@Serializable
data class MemoryCell(
    val instruction: Instruction,
    val ownerId: Int? = null,
    val lastModifiedCycle: Int = -1,
    val type: CellType = CellType.NORMAL,
    val writeCount: Int = 0
)

enum class SpecialPower {
    PROCESS_SHIELD, SPEED_BOOST, REDUCE_PENALTY
}

@Serializable
data class Warrior(
    val id: Int,
    val name: String,
    val color: CoreWarColor,
    val threads: List<Int> = emptyList(),
    val isAlive: Boolean = true,
    val xp: Int = 0,
    val level: Int = 1,
    val specialPowers: Set<SpecialPower> = emptySet(),
    val shieldUsed: Boolean = false
)

enum class BattleStatus {
    IDLE, RUNNING, PAUSED, WARRIOR_WINS, DRAW
}

@Serializable
data class BattleEvent(
    val cycle: Int,
    val type: EventType,
    val message: String,
    val color: CoreWarColor? = null
)

enum class EventType {
    INFO, PROCESS_DEATH, MEMORY_OVERWRITE, WINNER
}

data class BattleState(
    val memory: Array<MemoryCell>,
    val warriors: List<Warrior>,
    val cycle: Int = 0,
    val maxCycles: Int = 80000,
    val status: BattleStatus = BattleStatus.IDLE,
    val winnerId: Int? = null,
    val chaosMode: Boolean = false,
    val events: List<BattleEvent> = emptyList(),
    val deadWarriors: List<Warrior> = emptyList()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BattleState) return false
        if (!memory.contentEquals(other.memory)) return false
        if (warriors != other.warriors) return false
        if (cycle != other.cycle) return false
        if (maxCycles != other.maxCycles) return false
        if (status != other.status) return false
        if (winnerId != other.winnerId) return false
        if (chaosMode != other.chaosMode) return false
        return true
    }

    override fun hashCode(): Int {
        var result = memory.contentHashCode()
        result = 31 * result + warriors.hashCode()
        result = 31 * result + cycle
        result = 31 * result + maxCycles
        result = 31 * result + status.hashCode()
        result = 31 * result + (winnerId ?: 0)
        result = 31 * result + chaosMode.hashCode()
        return result
    }
}
