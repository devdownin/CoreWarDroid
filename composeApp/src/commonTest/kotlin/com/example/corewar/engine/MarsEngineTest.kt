package com.example.corewar.engine

import com.example.corewar.model.*
import kotlin.test.*

class MarsEngineTest {
    private val engine = MarsEngine()

    @Test
    fun testLoadWarriors() {
        val warriors = listOf(
            "Imp" to listOf(Instruction(Opcode.MOV, AddressMode.DIRECT, 0, AddressMode.DIRECT, 1))
        )
        val state = engine.loadWarriors(warriors, memSize = 100)

        assertEquals(100, state.memory.size)
        assertEquals(1, state.warriors.size)
        assertEquals("Imp", state.warriors[0].name)
        assertEquals(Opcode.MOV, state.memory[0].instruction.opcode)
    }

    @Test
    fun testStepMovImp() {
        val warriors = listOf(
            "Imp" to listOf(Instruction(Opcode.MOV, AddressMode.DIRECT, 0, AddressMode.DIRECT, 1))
        )
        var state = engine.loadWarriors(warriors, memSize = 100)

        // Initial state: MOV 0, 1 at 0
        state = engine.step(state)

        // After 1 step: MOV 0, 1 should be at 0 AND 1
        assertEquals(Opcode.MOV, state.memory[1].instruction.opcode)
        assertEquals(1, state.warriors[0].threads[0]) // PC should have moved to 1
    }

    @Test
    fun testWarriorElimination() {
        val warriors = listOf(
            "Dying" to listOf(Instruction(Opcode.DAT)),
            "Survivor" to listOf(Instruction(Opcode.NOP))
        )
        var state = engine.loadWarriors(warriors, memSize = 100)

        // Step until "Dying" executes DAT (at pos 0)
        state = engine.step(state)

        // "Dying" should be eliminated (no threads)
        assertTrue(state.warriors[0].threads.isEmpty())
        assertEquals(BattleStatus.WARRIOR_WINS, state.status)
        assertEquals(1, state.winnerId)
    }

    @Test
    fun testDrawOnMaxCycles() {
        val warriors = listOf(
            "Slow1" to listOf(Instruction(Opcode.NOP)),
            "Slow2" to listOf(Instruction(Opcode.NOP))
        )
        var state = engine.loadWarriors(warriors, memSize = 100, maxCycles = 10)

        repeat(10) {
            state = engine.step(state)
        }

        assertEquals(BattleStatus.DRAW, state.status)
    }

    @Test
    fun testSplInstruction() {
        val warriors = listOf(
            "Spliter" to listOf(
                Instruction(Opcode.SPL, AddressMode.DIRECT, 2),
                Instruction(Opcode.DAT),
                Instruction(Opcode.NOP)
            )
        )
        var state = engine.loadWarriors(warriors, memSize = 100)

        // Initial: 1 thread at 0
        assertEquals(1, state.warriors[0].threads.size)

        // Step 1: execute SPL 2
        state = engine.step(state)

        // Should have 2 threads now: one at 1 (next), one at 2 (spawn)
        assertEquals(2, state.warriors[0].threads.size)
        assertEquals(1, state.warriors[0].threads[0])
        assertEquals(2, state.warriors[0].threads[1])
    }
}
