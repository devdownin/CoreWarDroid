package com.example.corewar.engine

import com.example.corewar.model.*

data class ParseError(val line: Int, val message: String)

class RedcodeParser {

    fun parse(code: String): List<Instruction> {
        val lines = code.lines().map { it.substringBefore(";").trim() }.filter { it.isNotEmpty() }
        return lines.map { parseLine(it) }
    }

    fun validate(code: String): List<ParseError> {
        val errors = mutableListOf<ParseError>()
        code.lines().forEachIndexed { index, line ->
            val cleanLine = line.substringBefore(";").trim()
            if (cleanLine.isNotEmpty()) {
                try {
                    parseLine(cleanLine)
                } catch (e: Exception) {
                    errors.add(ParseError(index + 1, e.message ?: "Invalid syntax"))
                }
            }
        }
        return errors
    }

    private fun parseLine(line: String): Instruction {
        // Correct regex to split by whitespace and/or comma
        val tokens = line.split(Regex("[ ,]+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) throw Exception("Empty instruction")

        val opcode = try {
            Opcode.valueOf(tokens[0].uppercase())
        } catch (e: Exception) {
            throw Exception("Unknown opcode: ${tokens[0]}")
        }

        if (tokens.size == 1) return Instruction(opcode)

        val partA = tokens[1]
        val (modeA, valA) = parseArgument(partA)

        if (tokens.size == 2) return Instruction(opcode, modeA, valA)

        val partB = tokens[2]
        val (modeB, valB) = parseArgument(partB)

        return Instruction(opcode, modeA, valA, modeB, valB)
    }

    private fun parseArgument(arg: String): Pair<AddressMode, Int> {
        val mode = when (arg[0]) {
            '#' -> AddressMode.IMMEDIATE
            '$' -> AddressMode.DIRECT
            '@' -> AddressMode.INDIRECT_B
            '<' -> AddressMode.PRE_DEC_B
            '>' -> AddressMode.POST_INC_B
            '*' -> AddressMode.INDIRECT_A
            '{' -> AddressMode.PRE_DEC_A
            '}' -> AddressMode.POST_INC_A
            else -> null
        }

        val valStr = if (mode != null) arg.substring(1) else arg
        val addressMode = mode ?: AddressMode.DIRECT
        val value = valStr.toIntOrNull() ?: throw Exception("Invalid value: $valStr")

        return addressMode to value
    }
}
