package com.example.corewar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.corewar.ui.components.MemoryVisualizer
import com.example.corewar.ui.viewmodel.BattleIntent
import com.example.corewar.ui.viewmodel.BattleViewModel
import com.example.corewar.model.BattleStatus

@Composable
fun BattleScreen(
    viewModel: BattleViewModel,
    warriors: List<Pair<String, String>>,
    chaosMode: Boolean,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(BattleIntent.StartBattle(warriors, chaosMode))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        BattleHeader(uiState, onNavigateBack)

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            uiState.battleState?.let { battleState ->
                MemoryVisualizer(
                    state = battleState,
                    onCellClick = { viewModel.handleIntent(BattleIntent.SelectCell(it)) }
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Green)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BattleControls(uiState, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        WarriorDashboard(uiState)

        Spacer(modifier = Modifier.height(16.dp))

        CellInspector(uiState)
    }

    if (uiState.battleState?.status == BattleStatus.WARRIOR_WINS || uiState.battleState?.status == BattleStatus.DRAW) {
        BattleEndDialog(uiState, onNavigateBack)
    }
}

@Composable
fun BattleHeader(uiState: com.example.corewar.ui.viewmodel.BattleUiState, onNavigateBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
            Text("←", color = Color.White, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("BATTLE ARENA", color = Color.Green, fontWeight = FontWeight.Bold)
            Text("CYCLE ${uiState.battleState?.cycle ?: 0}", color = Color.Gray, fontSize = 12.sp)
        }
        val statusText = when (uiState.battleState?.status) {
            BattleStatus.RUNNING -> "RUNNING"
            BattleStatus.PAUSED -> "PAUSED"
            else -> "IDLE"
        }
        Text(statusText, color = Color.Yellow, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BattleControls(uiState: com.example.corewar.ui.viewmodel.BattleUiState, viewModel: BattleViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { viewModel.handleIntent(BattleIntent.PauseResume) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
        ) {
            Text(if (uiState.battleState?.status == BattleStatus.PAUSED) "RESUME" else "PAUSE")
        }

        Slider(
            value = (500f - uiState.speed.toFloat()).coerceIn(0f, 499f),
            onValueChange = { viewModel.handleIntent(BattleIntent.SetSpeed((500 - it).toLong())) },
            valueRange = 0f..499f,
            modifier = Modifier.width(150.dp),
            colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
        )

        Button(
            onClick = { viewModel.handleIntent(BattleIntent.Step) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
            enabled = uiState.battleState?.status == BattleStatus.PAUSED
        ) {
            Text("STEP")
        }
    }
}

@Composable
fun WarriorDashboard(uiState: com.example.corewar.ui.viewmodel.BattleUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.battleState?.warriors?.forEach { warrior ->
            val warriorColor = Color(warrior.color.argb)
            Surface(
                modifier = Modifier.weight(1f),
                color = Color.DarkGray.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, warriorColor)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(warrior.name, color = warriorColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("THREADS: ${warrior.threads.size}", color = Color.White, fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { if (warrior.threads.isEmpty()) 0f else 1f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = warriorColor
                    )
                }
            }
        }
    }
}

@Composable
fun CellInspector(uiState: com.example.corewar.ui.viewmodel.BattleUiState) {
    uiState.selectedCell?.let { cell ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.DarkGray.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("CELL #${uiState.selectedCellIndex}", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Text("${cell.instruction.opcode} ${cell.instruction.modeA}${cell.instruction.valueA}, ${cell.instruction.modeB}${cell.instruction.valueB}", color = Color.White)
                Text("OWNER: ${cell.ownerId?.let { uiState.battleState?.warriors?.getOrNull(it)?.name } ?: "NONE"}", color = Color.Gray, fontSize = 12.sp)
                Text("TYPE: ${cell.type}", color = Color.Cyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BattleEndDialog(uiState: com.example.corewar.ui.viewmodel.BattleUiState, onNavigateBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("BATTLE ENDED", color = Color.Green) },
        text = {
            Column {
                val winnerId = uiState.battleState?.winnerId
                val statusText = if (winnerId != null) "WINNER: ${uiState.battleState?.warriors?.getOrNull(winnerId)?.name}" else "DRAW"
                Text(statusText, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("XP GAINED: +${uiState.xpGained}", color = Color.Magenta)
            }
        },
        confirmButton = {
            Button(onClick = onNavigateBack) {
                Text("BACK TO BASE")
            }
        },
        containerColor = Color.DarkGray
    )
}
