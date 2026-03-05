package com.example.corewar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val topics = listOf(
        "CONCEPT" to "CoreWar is a simulation where programs (Warriors) compete in a circular memory area (the Core). The last warrior alive wins.",
        "OPCODES" to """
            MOV A, B : Copies A to B.
            ADD A, B : Adds A to B.
            SUB A, B : Subtracts A from B.
            MUL A, B : Multiplies B by A.
            DIV A, B : Divides B by A (kills process if A=0).
            MOD A, B : Modulo B by A (kills process if A=0).
            JMP A    : Jump to A.
            JMZ A, B : Jump to A if B is 0.
            JMN A, B : Jump to A if B is not 0.
            DJN A, B : Decrement B, jump to A if B is not 0.
            SPL A    : Create a new process at A.
            CMP A, B : Skip next if A equals B.
            SLT A, B : Skip next if A is less than B.
            DAT A, B : Data only. Kills process if executed.
            NOP      : No operation.
        """.trimIndent(),
        "ADDRESS MODES" to """
            # : Immediate (value itself)
            $ : Direct (relative to current PC)
            @ : Indirect B (relative pointer at target B)
            < : Pre-decrement B (decrement target B before use)
            > : Post-increment B (increment target B after use)
            * : Indirect A (relative pointer at target A)
            { : Pre-decrement A
            } : Post-increment A
        """.trimIndent(),
        "ANOMALIES" to """
            PROTECTED (Blue) : Immune to writes. 50% slower execution.
            VOLATILE (Red) : Instability. Resets to DAT after 5 writes.
        """.trimIndent(),
        "STRATEGIES" to """
            Imp: 'MOV 0, 1' - Simple replicator.
            Dwarf: Bombs the core with 'DAT' to kill enemies.
            Paper: Fast replicator using 'SPL' and 'MOV'.
        """.trimIndent()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Text("←", color = Color.White, fontSize = 24.sp)
            }
            Text("ACADEMY", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(topics) { (title, content) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(content, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
