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

        Row(modifier = Modifier.weight(0.5f)) {
            Column(modifier = Modifier.weight(1f)) {
                WarriorDashboard(uiState)
                Spacer(modifier = Modifier.height(8.dp))
                CellInspector(uiState)
            }
            Spacer(modifier = Modifier.width(8.dp))
            EventLog(uiState)
        }
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

        IconButton(onClick = { viewModel.handleIntent(BattleIntent.Restart) }) {
            Text("🔄", color = Color.White)
        }
    }
}

@Composable
fun EventLog(uiState: com.example.corewar.ui.viewmodel.BattleUiState) {
    val events = uiState.battleState?.events?.reversed() ?: emptyList()
    Surface(
        modifier = Modifier.width(200.dp).fillMaxHeight(),
        color = Color.DarkGray.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            androidx.compose.foundation.lazy.items(events) { event ->
                Text(
                    text = "[${event.cycle}] ${event.message}",
                    color = event.color?.let { Color(it.argb) } ?: Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun WarriorDashboard(uiState: com.example.corewar.ui.viewmodel.BattleUiState) {
    val allWarriors = (uiState.battleState?.warriors ?: emptyList())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allWarriors.forEach { warrior ->
            val isDead = warrior.threads.isEmpty()
            val warriorColor = Color(warrior.color.argb)
            Surface(
                modifier = Modifier.weight(1f),
                color = if (isDead) Color.Black else Color.DarkGray.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (isDead) Color.Gray else warriorColor)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        warrior.name,
                        color = if (isDead) Color.Gray else warriorColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        textDecoration = if (isDead) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Text(
                        if (isDead) "ELIMINATED" else "THREADS: ${warrior.threads.size}",
                        color = if (isDead) Color.Red else Color.White,
                        fontSize = 10.sp
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("BATTLE REPORT", color = Color.Green, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("v1.0", color = Color.DarkGray, fontSize = 10.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                val winnerId = uiState.battleState?.winnerId
                val winner = winnerId?.let { uiState.battleState?.warriors?.getOrNull(it) }

                Text(
                    text = if (winner != null) "WINNER: ${winner.name}" else "DRAW",
                    color = winner?.color?.let { Color(it.argb) } ?: Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("BATTLE STATISTICS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val memSize = uiState.battleState?.memory?.size ?: 1
                uiState.battleState?.warriors?.forEach { warrior ->
                    val stats = uiState.warriorStats[warrior.id]
                    val coverage = (stats?.cellsOwned ?: 0).toFloat() / memSize

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(warrior.color.argb)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(warrior.name, color = Color(warrior.color.argb), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        LinearProgressIndicator(
                            progress = { coverage },
                            modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp),
                            color = Color(warrior.color.argb),
                            trackColor = Color.DarkGray
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dominance:", color = Color.Gray, fontSize = 11.sp)
                            Text("${(coverage * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Survival:", color = Color.Gray, fontSize = 11.sp)
                            Text("${stats?.survivalCycles ?: 0} cycles", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("XP GAINED: +${uiState.xpGained}", color = Color.Magenta, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
            ) {
                Text("RETURN TO BASE")
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}
