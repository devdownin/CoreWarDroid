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

    fun runBatch(state: BattleState, cycles: Int): BattleState {
        var currentState = state
        repeat(cycles) {
            if (currentState.status != BattleStatus.RUNNING) return currentState
            currentState = step(currentState)
        }
        return currentState
    }

    fun step(state: BattleState): BattleState {
        if (state.status != BattleStatus.RUNNING) return state

        val memSize = state.memory.size
        // Only copy memory once per cycle, not per instruction
        val currentMemory = state.memory.copyOf()
        var currentWarriors = state.warriors.toMutableList()

        // Handle Chaos Mode events
        if (state.chaosMode && Random.nextInt(1000) == 0) {
            val chaosResult = handleChaosEvent(state.copy(memory = currentMemory, warriors = currentWarriors))
            chaosResult.memory.copyInto(currentMemory)
            currentWarriors = chaosResult.warriors.toMutableList()
        }

        for (warriorIdx in currentWarriors.indices) {
            val warrior = currentWarriors[warriorIdx]
            val threads = warrior.threads.toMutableList()
            if (threads.isEmpty()) continue

            val executions = if (warrior.specialPowers.contains(SpecialPower.SPEED_BOOST) && Random.nextInt(10) == 0) 2 else 1

            repeat(executions) {
                if (threads.isEmpty()) return@repeat
                val pc = threads.removeAt(0)
                val cell = currentMemory[pc.mod(memSize)]

                val penaltyChance = if (warrior.specialPowers.contains(SpecialPower.REDUCE_PENALTY)) 15 else 50
                if (cell.type == CellType.PROTECTED && Random.nextInt(100) < penaltyChance) {
                    threads.add(0, pc)
                    return@repeat
                }

                val result = executeInPlace(cell.instruction, pc, warriorIdx, currentMemory, state.cycle)

                if (result.nextPc != null) {
                    threads.add(result.nextPc.mod(memSize))
                }
                result.spawnPc?.let {
                    threads.add(it.mod(memSize))
                }

                if (threads.isEmpty() && warrior.specialPowers.contains(SpecialPower.PROCESS_SHIELD) && !warrior.shieldUsed) {
                    threads.add(Random.nextInt(memSize))
                    currentWarriors[warriorIdx] = currentWarriors[warriorIdx].copy(shieldUsed = true)
                }
            }
            currentWarriors[warriorIdx] = currentWarriors[warriorIdx].copy(threads = threads)
        }

        val aliveWarriors = currentWarriors.filter { it.threads.isNotEmpty() }
        val status = when {
            aliveWarriors.isEmpty() -> BattleStatus.DRAW
            aliveWarriors.size == 1 && state.warriors.size > 1 -> BattleStatus.WARRIOR_WINS
            state.cycle >= state.maxCycles -> BattleStatus.DRAW
            else -> BattleStatus.RUNNING
        }

        return state.copy(
            memory = currentMemory,
            warriors = currentWarriors,
            cycle = state.cycle + 1,
            status = status,
            winnerId = if (status == BattleStatus.WARRIOR_WINS) aliveWarriors.first().id else null
        )
    }

    private fun handleChaosEvent(state: BattleState): BattleState {
        val memSize = state.memory.size
        val newMemory = state.memory.copyOf()
        return when (Random.nextInt(2)) {
            0 -> { // MEMORY GLITCH
                val pos = Random.nextInt(memSize)
                newMemory[pos] = newMemory[pos].copy(instruction = Instruction(Opcode.DAT), ownerId = null)
                state.copy(memory = newMemory)
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

    private data class InPlaceResult(
        val nextPc: Int?,
        val spawnPc: Int? = null
    )

    private fun executeInPlace(instr: Instruction, pc: Int, ownerId: Int, workingMemory: Array<MemoryCell>, cycle: Int): InPlaceResult {
        val memSize = workingMemory.size

        fun getAddr(mode: AddressMode, value: Int, currentPc: Int): Int {
            return when (mode) {
                AddressMode.IMMEDIATE -> currentPc
                AddressMode.DIRECT -> (currentPc + value).mod(memSize)
                AddressMode.INDIRECT_B -> {
                    val target = (currentPc + value).mod(memSize)
                    (target + workingMemory[target].instruction.valueB).mod(memSize)
                }
                AddressMode.PRE_DEC_B -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = workingMemory[target].instruction
                    val newInstr = oldInstr.copy(valueB = oldInstr.valueB - 1)
                    workingMemory[target] = workingMemory[target].copy(instruction = newInstr)
                    (target + newInstr.valueB).mod(memSize)
                }
                AddressMode.POST_INC_B -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = workingMemory[target].instruction
                    val addr = (target + oldInstr.valueB).mod(memSize)
                    val newInstr = oldInstr.copy(valueB = oldInstr.valueB + 1)
                    workingMemory[target] = workingMemory[target].copy(instruction = newInstr)
                    addr
                }
                AddressMode.INDIRECT_A -> {
                    val target = (currentPc + value).mod(memSize)
                    (target + workingMemory[target].instruction.valueA).mod(memSize)
                }
                AddressMode.PRE_DEC_A -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = workingMemory[target].instruction
                    val newInstr = oldInstr.copy(valueA = oldInstr.valueA - 1)
                    workingMemory[target] = workingMemory[target].copy(instruction = newInstr)
                    (target + newInstr.valueA).mod(memSize)
                }
                AddressMode.POST_INC_A -> {
                    val target = (currentPc + value).mod(memSize)
                    val oldInstr = workingMemory[target].instruction
                    val addr = (target + oldInstr.valueA).mod(memSize)
                    val newInstr = oldInstr.copy(valueA = oldInstr.valueA + 1)
                    workingMemory[target] = workingMemory[target].copy(instruction = newInstr)
                    addr
                }
            }
        }

        fun getValue(mode: AddressMode, value: Int, currentPc: Int, isA: Boolean): Int {
            if (mode == AddressMode.IMMEDIATE) return value
            val addr = getAddr(mode, value, currentPc)
            val targetInstr = workingMemory[addr].instruction
            return if (isA) targetInstr.valueA else targetInstr.valueB
        }

        fun writeMemory(addr: Int, cell: MemoryCell) {
            val target = addr.mod(memSize)
            val targetCell = workingMemory[target]
            if (targetCell.type == CellType.PROTECTED) return

            var newCell = cell.copy(lastModifiedCycle = cycle, ownerId = ownerId, writeCount = targetCell.writeCount + 1)
            if (newCell.type == CellType.VOLATILE && newCell.writeCount >= 5) {
                newCell = newCell.copy(instruction = Instruction(Opcode.DAT), ownerId = null)
            }
            workingMemory[target] = newCell
        }

        return when (instr.opcode) {
            Opcode.DAT -> InPlaceResult(null)
            Opcode.MOV -> {
                val srcAddr = getAddr(instr.modeA, instr.valueA, pc)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (instr.modeA == AddressMode.IMMEDIATE) {
                    val targetCell = workingMemory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = instr.valueA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                } else {
                    writeMemory(destAddr, workingMemory[srcAddr])
                }
                InPlaceResult(pc + 1)
            }
            Opcode.ADD -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = workingMemory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB + valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                InPlaceResult(pc + 1)
            }
            Opcode.SUB -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = workingMemory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB - valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                InPlaceResult(pc + 1)
            }
            Opcode.MUL -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = workingMemory[destAddr]
                val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB * valA)
                writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                InPlaceResult(pc + 1)
            }
            Opcode.DIV -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (valA == 0) {
                     InPlaceResult(null)
                } else {
                    val targetCell = workingMemory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB / valA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                    InPlaceResult(pc + 1)
                }
            }
            Opcode.MOD -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val destAddr = getAddr(instr.modeB, instr.valueB, pc)
                if (valA == 0) {
                     InPlaceResult(null)
                } else {
                    val targetCell = workingMemory[destAddr]
                    val newInstr = targetCell.instruction.copy(valueB = targetCell.instruction.valueB % valA)
                    writeMemory(destAddr, targetCell.copy(instruction = newInstr))
                    InPlaceResult(pc + 1)
                }
            }
            Opcode.JMP -> InPlaceResult(getAddr(instr.modeA, instr.valueA, pc))
            Opcode.JMZ -> {
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valB == 0) InPlaceResult(getAddr(instr.modeA, instr.valueA, pc))
                else InPlaceResult(pc + 1)
            }
            Opcode.JMN -> {
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valB != 0) InPlaceResult(getAddr(instr.modeA, instr.valueA, pc))
                else InPlaceResult(pc + 1)
            }
            Opcode.DJN -> {
                val destBAddr = getAddr(instr.modeB, instr.valueB, pc)
                val targetCell = workingMemory[destBAddr]
                val newValueB = targetCell.instruction.valueB - 1
                val newInstr = targetCell.instruction.copy(valueB = newValueB)
                writeMemory(destBAddr, targetCell.copy(instruction = newInstr))
                if (newValueB != 0) InPlaceResult(getAddr(instr.modeA, instr.valueA, pc))
                else InPlaceResult(pc + 1)
            }
            Opcode.SPL -> InPlaceResult(pc + 1, getAddr(instr.modeA, instr.valueA, pc))
            Opcode.CMP -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valA == valB) InPlaceResult(pc + 2)
                else InPlaceResult(pc + 1)
            }
            Opcode.SLT -> {
                val valA = getValue(instr.modeA, instr.valueA, pc, true)
                val valB = getValue(instr.modeB, instr.valueB, pc, false)
                if (valA < valB) InPlaceResult(pc + 2)
                else InPlaceResult(pc + 1)
            }
            Opcode.NOP -> InPlaceResult(pc + 1)
        }
    }

    suspend fun runWithDelay(state: BattleState, delayMs: Long, onStep: (BattleState) -> Unit) {
        var currentState = state
        while (currentState.status == BattleStatus.RUNNING) {
            currentState = if (delayMs < 10 && delayMs > 0) {
                 runBatch(currentState, 10)
            } else {
                 step(currentState)
            }
            onStep(currentState)
            if (delayMs > 0) delay(delayMs)
        }
    }
}
