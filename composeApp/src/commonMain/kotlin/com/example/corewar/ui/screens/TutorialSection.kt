package com.example.corewar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialSection() {
    var step by remember { mutableStateOf(0) }
    val tutorialSteps = listOf(
        "WELCOME" to "CoreWar is a digital arena where your programs (Warriors) fight for control of the circular memory (the Core).",
        "INSTRUCTIONS" to "Every warrior is a list of Redcode instructions. 'MOV 0, 1' is the famous 'Imp'. It copies itself to the next address forever.",
        "COMBAT" to "A warrior dies when its last execution thread hits a 'DAT' instruction. Use 'SPL' to create more threads!",
        "CHAOS" to "Chaos mode adds random glitches. Use 'PROTECTED' memory areas to survive longer."
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF001A1A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("MISSION BRIEFING", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(tutorialSteps[step].first, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(tutorialSteps[step].second, color = Color.LightGray, fontSize = 12.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("PREV", color = Color.Gray) }
                }
                if (step < tutorialSteps.size - 1) {
                    TextButton(onClick = { step++ }) { Text("NEXT", color = Color.Green) }
                }
            }
        }
    }
}
