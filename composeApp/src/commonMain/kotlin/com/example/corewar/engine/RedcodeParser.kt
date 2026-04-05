package com.example.corewar.engine

import com.example.corewar.model.*

data class ParseError(val line: Int, val message: String)

class RedcodeParser {

    fun parse(code: String): List<Instruction> {
        val labels = mutableMapOf<String, Int>()
        val rawLines = code.lines().map { it.substringBefore(";").trim() }
        var instrCount = 0

        // Pass 1: Resolve Labels
        rawLines.forEach { line ->
            if (line.isEmpty()) return@forEach
            val tokens = line.split(Regex("[ ,]+")).filter { it.isNotEmpty() }
            if (tokens.isEmpty()) return@forEach

            val firstToken = tokens[0].uppercase()
            if (Opcode.entries.none { it.name == firstToken }) {
                // It's a label
                val labelName = tokens[0].removeSuffix(":")
                labels[labelName] = instrCount
                if (tokens.size > 1) instrCount++
            } else {
                instrCount++
            }
        }

        // Pass 2: Parse Instructions
        val instructions = mutableListOf<Instruction>()
        var currentIdx = 0
        rawLines.forEach { line ->
            if (line.isEmpty()) return@forEach
            val tokens = line.split(Regex("[ ,]+")).filter { it.isNotEmpty() }
            if (tokens.isEmpty()) return@forEach

            val opcodeToken: String
            val args: List<String>

            val firstToken = tokens[0].uppercase()
            if (Opcode.entries.none { it.name == firstToken }) {
                if (tokens.size == 1) return@forEach
                opcodeToken = tokens[1]
                args = tokens.drop(2)
            } else {
                opcodeToken = tokens[0]
                args = tokens.drop(1)
            }

            val opcode = Opcode.entries.find { it.name == opcodeToken.uppercase() } ?: Opcode.DAT

            val instr = when (args.size) {
                0 -> Instruction(opcode)
                1 -> {
                    val (mode, value) = parseArgument(args[0], labels, currentIdx)
                    Instruction(opcode, mode, value)
                }
                else -> {
                    val (modeA, valA) = parseArgument(args[0], labels, currentIdx)
                    val (modeB, valB) = parseArgument(args[1], labels, currentIdx)
                    Instruction(opcode, modeA, valA, modeB, valB)
                }
            }
            instructions.add(instr)
            currentIdx++
        }

        return instructions
    }

    fun validate(code: String): List<ParseError> {
        return try {
            parse(code)
            emptyList()
        } catch (e: Exception) {
            listOf(ParseError(0, e.message ?: "Unknown error"))
        }
    }

    private fun parseArgument(arg: String, labels: Map<String, Int>, currentIdx: Int): Pair<AddressMode, Int> {
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

        val valPart = if (mode != null) arg.substring(1) else arg
        val addressMode = mode ?: AddressMode.DIRECT

        // Simple arithmetic and label resolution
        val value = resolveValue(valPart, labels, currentIdx)

        return addressMode to value
    }

    private fun resolveValue(part: String, labels: Map<String, Int>, currentIdx: Int): Int {
        val trimmed = part.trim()

        // Handle basic arithmetic START+1 (with potential labels)
        if (trimmed.contains("+")) {
            val subParts = trimmed.split("+", limit = 2)
            return resolveValue(subParts[0], labels, currentIdx) + resolveValue(subParts[1], labels, currentIdx)
        }
        if (trimmed.contains("-") && !trimmed.startsWith("-")) {
            val subParts = trimmed.split("-", limit = 2)
            return resolveValue(subParts[0], labels, currentIdx) - resolveValue(subParts[1], labels, currentIdx)
        }

        // Handle labels
        if (labels.containsKey(trimmed)) {
            return labels[trimmed]!! - currentIdx
        }

        return trimmed.toIntOrNull() ?: 0
    }
}
