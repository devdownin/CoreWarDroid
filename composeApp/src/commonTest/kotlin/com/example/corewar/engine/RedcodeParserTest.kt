package com.example.corewar.engine

import com.example.corewar.model.*
import kotlin.test.*

class RedcodeParserTest {
    private val parser = RedcodeParser()

    @Test
    fun testParseSimpleInstructions() {
        val code = """
            MOV 0, 1
            ADD #4, 3
            DAT #0
        """.trimIndent()

        val instructions = parser.parse(code)

        assertEquals(3, instructions.size)
        assertEquals(Opcode.MOV, instructions[0].opcode)
        assertEquals(AddressMode.DIRECT, instructions[0].modeA)
        assertEquals(0, instructions[0].valueA)

        assertEquals(Opcode.ADD, instructions[1].opcode)
        assertEquals(AddressMode.IMMEDIATE, instructions[1].modeA)
        assertEquals(4, instructions[1].valueA)
    }

    @Test
    fun testParseLabels() {
        val code = """
            START: MOV 0, 1
            JMP START
        """.trimIndent()

        val instructions = parser.parse(code)

        assertEquals(2, instructions.size)
        assertEquals(Opcode.MOV, instructions[0].opcode)
        assertEquals(Opcode.JMP, instructions[1].opcode)
        assertEquals(-1, instructions[1].valueA) // JMP back to START (1 -> 0)
    }

    @Test
    fun testValidate() {
        val validCode = "MOV 0, 1"
        assertTrue(parser.validate(validCode).isEmpty())
    }

    @Test
    fun testParseComplexArithmetic() {
        val code = "MOV START+2, START-1\nSTART: NOP"
        val instructions = parser.parse(code)

        assertEquals(2, instructions.size)
        // START is at index 1, current is 0. Offset to START is 1.
        // START+2 = 1 + 2 = 3
        // START-1 = 1 - 1 = 0
        assertEquals(3, instructions[0].valueA)
        assertEquals(0, instructions[0].valueB)
    }

    @Test
    fun testCommentsIgnored() {
        val code = """
            MOV 0, 1 ; This is a comment
            ; Entire line comment
            DAT #0
        """.trimIndent()
        val instructions = parser.parse(code)
        assertEquals(2, instructions.size)
        assertEquals(Opcode.MOV, instructions[0].opcode)
        assertEquals(Opcode.DAT, instructions[1].opcode)
    }
}
