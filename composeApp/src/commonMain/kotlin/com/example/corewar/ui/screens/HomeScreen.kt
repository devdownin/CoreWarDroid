package com.example.corewar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke
import com.example.corewar.data.UserSettingsRepository
import com.example.corewar.data.WarriorRepository
import com.example.corewar.model.CoreWarColor
import com.example.corewar.ui.components.ProceduralAvatar
import com.example.corewar.ui.LocalCoreWarColors
import com.example.corewar.ui.SoundManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first

@Composable
fun HomeScreen(
    warriorRepository: WarriorRepository,
    userSettingsRepository: UserSettingsRepository,
    onStartBattle: (List<Pair<String, String>>, Boolean) -> Unit,
    onOpenEditor: (String?, String?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAcademy: () -> Unit
) {
    var warriors by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var selectedWarriors by remember { mutableStateOf(setOf<Int>()) }
    val totalXp by userSettingsRepository.totalXp.collectAsStateWithLifecycle(0)
    val level = userSettingsRepository.getLevel(totalXp)
    val chaosMode by userSettingsRepository.chaosMode.collectAsStateWithLifecycle(false)
    val colors = LocalCoreWarColors.current

    LaunchedEffect(Unit) {
        val custom = warriorRepository.getAllWarriors().map { it.name to it.code }
        val preloaded = warriorRepository.getPreloadedWarriors()
        warriors = preloaded + custom
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Header(level, totalXp, onOpenSettings, onOpenHelp, onOpenAcademy = onOpenAcademy)

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { onOpenHelp() }) {
             Text("VIEW DOCUMENTATION", color = colors.primary, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("SELECT WARRIORS", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(warriors.indices.toList()) { index ->
                val (name, code) = warriors[index]
                val isSelected = selectedWarriors.contains(index)

                WarriorCard(
                    name = name,
                    code = code,
                    isSelected = isSelected,
                    onClick = {
                        selectedWarriors = if (isSelected) selectedWarriors - index else selectedWarriors + index
                    },
                    onEdit = { onOpenEditor(name, code) }
                )
            }
        }

        Button(
            onClick = {
                val selected = selectedWarriors.map { warriors[it] }
                SoundManager.playSound("ENGAGE")
                onStartBattle(selected, chaosMode)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
            enabled = selectedWarriors.size >= 2
        ) {
            Text("ENGAGE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Header(level: Int, xp: Int, onOpenSettings: () -> Unit, onOpenHelp: () -> Unit, onOpenAcademy: () -> Unit) {
    val colors = LocalCoreWarColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.clickable { onOpenAcademy() }) {
            Text("ANOMALY MASTERY", color = colors.accent, fontWeight = FontWeight.Bold)
            Text("LVL $level", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            LinearProgressIndicator(
                progress = { (xp % 100) / 100f },
                modifier = Modifier.width(100.dp).height(4.dp),
                color = colors.accent,
                trackColor = Color.DarkGray
            )
        }

        Row {
            IconButton(onClick = onOpenAcademy) {
                Text("🎓", fontSize = 24.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Text("⚙️", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun WarriorCard(
    name: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val colors = LocalCoreWarColors.current
    Surface(
        color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isSelected) colors.primary else Color.Gray),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProceduralAvatar(code, if (isSelected) CoreWarColor(colors.primary.value.toLong()) else CoreWarColor(0xFFFFFFFF))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("CODE SIZE: ${code.lines().size}", color = Color.Gray, fontSize = 12.sp)
            }
            TextButton(onClick = onEdit) {
                Text("EDIT", color = colors.accent)
            }
        }
    }
}
