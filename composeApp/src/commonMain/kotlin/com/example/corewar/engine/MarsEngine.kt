package com.example.corewar.engine

import com.example.corewar.model.*
import kotlinx.coroutines.delay
import kotlin.random.Random

class MarsEngine {

    fun loadWarriors(warriors: List<Pair<String, List<Instruction>>>, memSize: Int = 8000, maxCycles: Int = 80000, chaosMode: Boolean = false): BattleState {
        val memory = Array(memSize) { index ->
            val type = when {
                index in 500..600 -> CellType.PROTECTED
                index in 2000..2100 -> CellType.VOLATILE
                else -> CellType.NORMAL
            }
            MemoryCell(Instruction(Opcode.DAT), type = type)
        }
        val activeWarriors = mutableListOf<Warrior>()

        val spacing = if (warriors.isNotEmpty()) memSize / warriors.size else 0
        warriors.forEachIndexed { index, (name, instructions) ->
            val startPos = index * spacing
            instructions.forEachIndexed { i, instr ->
                memory[(startPos + i).mod(memSize)] = MemoryCell(instr, index, type = memory[(startPos + i).mod(memSize)].type)
            }
            val warrior = Warrior(
                id = index,
                name = name,
                color = warriorColors[index % warriorColors.size],
                threads = listOf(startPos)
            )
            activeWarriors.add(warrior)
        }

        return BattleState(memory, activeWarriors, 0, maxCycles, BattleStatus.RUNNING, chaosMode = chaosMode)
    }

    private val warriorColors = listOf(
        CoreWarColor(0xFF00FF00), // Green
        CoreWarColor(0xFFFF00FF), // Magenta
        CoreWarColor(0xFF00FFFF), // Cyan
        CoreWarColor(0xFFFFFF00)  // Yellow
    )

    fun step(state: BattleState): BattleState {
        if (state.status != BattleStatus.RUNNING) return state

        var nextState = state
        val memSize = state.memory.size

        // Handle Chaos Mode events
        if (state.chaosMode && Random.nextInt(1000) == 0) {
            nextState = handleChaosEvent(nextState)
        }

        val nextWarriors = nextState.warriors.map { it.copy(threads = it.threads.toMutableList()) }.toMutableList()

        for (warriorIdx in nextWarriors.indices) {
            val warrior = nextWarriors[warriorIdx]
            val threads = warrior.threads.toMutableList()
            if (threads.isEmpty()) continue

            // Speed Boost check
            val executions = if (warrior.specialPowers.contains(SpecialPower.SPEED_BOOST) && Random.nextInt(10) == 0) 2 else 1

            repeat(executions) {
                if (threads.isEmpty()) return@repeat
                val pc = threads.removeAt(0)
                val cell = nextState.memory[pc.mod(memSize)]

                // Protected zone speed penalty
                if (cell.type == CellType.PROTECTED && state.cycle % 2 != 0) {
                    threads.add(0, pc)
                    return@repeat
                }

                val instr = cell.instruction
                val result = execute(instr, pc, warriorIdx, nextState)

                nextState = result.nextState

                if (result.nextPc != null) {
                    threads.add(result.nextPc.mod(memSize))
                }
                result.spawnPc?.let {
                    threads.add(it.mod(memSize))
                }

                // Process Shield logic
                if (threads.isEmpty() && warrior.specialPowers.contains(SpecialPower.PROCESS_SHIELD) && !warrior.shieldUsed) {
                    threads.add(Random.nextInt(memSize))
                    nextWarriors[warriorIdx] = nextWarriors[warriorIdx].copy(shieldUsed = true)
                }
            }
            nextWarriors[warriorIdx] = nextWarriors[warriorIdx].copy(threads = threads)
        }

        val aliveWarriors = nextWarriors.filter { it.threads.isNotEmpty() }
        val status = when {
            aliveWarriors.isEmpty() -> BattleStatus.DRAW
            aliveWarriors.size == 1 && state.warriors.size > 1 -> BattleStatus.WARRIOR_WINS
            state.cycle >= state.maxCycles -> BattleStatus.DRAW
            else -> BattleStatus.RUNNING
        }

        return nextState.copy(
            warriors = nextWarriors,
            cycle = state.cycle + 1,
            status = status,
            winnerId = if (status == BattleStatus.WARRIOR_WINS) aliveWarriors.first().id else null
        )
    }

    private fun handleChaosEvent(state: BattleState): BattleState {
        val memSize = state.memory.size
        return when (Random.nextInt(2)) {
            0 -> { // MEMORY GLITCH
                val pos = Random.nextInt(memSize)
                state.memory[pos] = state.memory[pos].copy(instruction = Instruction(Opcode.DAT), ownerId = null)
                state
            }
            else -> { // PROCESS WARP
                val nextWarriors = state.warriors.map { warrior ->
                    val newThreads = warrior.threads.map { Random.nextInt(memSize) }
                    warrior.copy(threads = newThreads)
                }
                state.copy(warriors = nextWarriors)
            }
        }
    }

    private data class ExecutionResult(
        val nextState: BattleState,
        val nextPc: Int?,
        val spawnPc: Int? = null
    )

    private fun execute(instr: Instruction, pc: Int, ownerId: Int, state: BattleState): ExecutionResult {
        val memSize = state.memory.size

        fun getAddr(mode: AddressMode, value: Int, currentPc: Int): Int {
            return when (mode) {
                AddressMode.IMMEDIATE -> currentPc
                AddressMode.DIRECT -> (currentPc + value).mod(memSize)
                AddressMode.INDIRECT_B -> {
                    val target = (currentPc + value).mod(memSize)
                    (target + state.memory[target].instruction.valueB).mod(memSize)
                }
                AddressMode.PRE_DEC_B -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = state.memory[target].instruction
                    val newInstr = oldInstr.copy(valueB = oldInstr.valueB - 1)
                    state.memory[target] = state.memory[target].copy(instruction = newInstr)
                    (target + newInstr.valueB).mod(memSize)
                }
                AddressMode.POST_INC_B -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = state.memory[target].instruction
                    val addr = (target + oldInstr.valueB).mod(memSize)
                    val newInstr = oldInstr.copy(valueB = oldInstr.valueB + 1)
                    state.memory[target] = state.memory[target].copy(instruction = newInstr)
                    addr
                }
                AddressMode.INDIRECT_A -> {
                    val target = (currentPc + value).mod(memSize)
                    (target + state.memory[target].instruction.valueA).mod(memSize)
                }
                AddressMode.PRE_DEC_A -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = state.memory[target].instruction
                    val newInstr = oldInstr.copy(valueA = oldInstr.valueA - 1)
                    state.memory[target] = state.memory[target].copy(instruction = newInstr)
                    (target + newInstr.valueA).mod(memSize)
                }
                AddressMode.POST_INC_A -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = state.memory[target].instruction
                    val addr = (target + oldInstr.valueA).mod(memSize)
                    val newInstr = oldInstr.copy(valueA = oldInstr.valueA + 1)
                    state.memory[target] = state.memory[target].copy(instruction = newInstr)
                    addr
                }
            }
        }

        fun getValue(mode: AddressMode, value: Int, currentPc: Int, isA: Boolean): Int {
            if (mode == AddressMode.IMMEDIATE) return value
            val addr = getAddr(mode, value, currentPc)
            val targetInstr = state.memory[addr].instruction
            return if (isA) targetInstr.valueA else targetInstr.valueB
        }

        fun writeMemory(addr: Int, cell: MemoryCell) {
            val target = addr.mod(memSize)
            val targetCell = state.memory[target]
            if (targetCell.type == CellType.PROTECTED) return // Immunized against overwrite

            var newCell = cell.copy(lastModifiedCycle = state.cycle, ownerId = ownerId, writeCount = targetCell.writeCount + 1)

            if (newCell.type == CellType.VOLATILE && newCell.writeCount >= 5) {
                newCell = newCell.copy(instruction = Instruction(Opcode.DAT), ownerId = null)
            }

            state.memory[target] = newCell
        }

        return when (instr.opcode) {
            Opcode.DAT -> ExecutionResult(state, null)
            Opcode.MOV -> {
                val srcAddr = getAddr(instr.modeA, instr.valueA, pc)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (instr.modeA == AddressMode.IMMEDIATE) {
                    val targetCell = state.memory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = instr.valueA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                } else {
                    writeMemory(destAddr, state.memory[srcAddr])
                }
                ExecutionResult(state, pc + 1)
            }
            Opcode.ADD -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = state.memory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB + valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                ExecutionResult(state, pc + 1)
            }
            Opcode.SUB -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = state.memory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB - valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                ExecutionResult(state, pc + 1)
            }
            Opcode.MUL -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = state.memory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB * valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                ExecutionResult(state, pc + 1)
            }
            Opcode.DIV -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (valA == 0) {
                     ExecutionResult(state, null) // Kill process
                } else {
                    val targetCell = state.memory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB / valA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.MOD -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (valA == 0) {
                     ExecutionResult(state, null) // Kill process
                } else {
                    val targetCell = state.memory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB % valA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.JMP -> {
                val destAddr = getAddr(instr.modeA, instr.valueA, pc)
                ExecutionResult(state, destAddr)
            }
            Opcode.JMZ -> {
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valB == 0) {
                    ExecutionResult(state, getAddr(instr.modeA, instr.valueA, pc))
                } else {
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.JMN -> {
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valB != 0) {
                    ExecutionResult(state, getAddr(instr.modeA, instr.valueA, pc))
                } else {
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.DJN -> {
                val destBAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = state.memory[destBAddr]
                val newValueB = targetCell.instruction.valueB - 1
                val newInstr = targetCell.instruction.copy(valueB = newValueB)
                writeMemory(destBAddr, targetCell.copy(instruction = newInstr))
                if (newValueB != 0) {
                    ExecutionResult(state, getAddr(instr.modeA, instr.valueA, pc))
                } else {
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.SPL -> {
                val destAddr = getAddr(instr.modeA, instr.valueA, pc)
                ExecutionResult(state, pc + 1, destAddr)
            }
            Opcode.CMP -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valA == valB) {
                    ExecutionResult(state, pc + 2)
                } else {
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.SLT -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valA < valB) {
                    ExecutionResult(state, pc + 2)
                } else {
                    ExecutionResult(state, pc + 1)
                }
            }
            Opcode.NOP -> ExecutionResult(state, pc + 1)
        }
    }

    suspend fun runWithDelay(state: BattleState, delayMs: Long, onStep: (BattleState) -> Unit) {
        var currentState = state
        while (currentState.status == BattleStatus.RUNNING) {
            currentState = step(currentState)
            onStep(currentState)
            if (delayMs > 0) delay(delayMs)
        }
    }
}
