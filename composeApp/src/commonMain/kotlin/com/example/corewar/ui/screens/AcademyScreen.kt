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
import com.example.corewar.data.UserSettingsRepository
import kotlinx.coroutines.launch

data class SkillNode(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val requiredLevel: Int,
    val dependencies: List<String> = emptyList()
)

val SkillTree = listOf(
    SkillNode("LOGIC", "Logic Module", "Unlocks CMP and SLT instructions", 0, 2),
    SkillNode("REPLICATION", "Replication Engine", "Unlocks SPL instruction", 0, 3, listOf("LOGIC")),
    SkillNode("ADVANCED_MATH", "Advanced Math", "Unlocks SUB, MUL, DIV, MOD", 0, 4, listOf("LOGIC")),
    SkillNode("SHIELD", "Process Shield", "Passive: 1 automatic restart if all processes die", 0, 6, listOf("REPLICATION")),
    SkillNode("TURBO", "Turbo Clock", "Passive: 10% chance to execute 2 instructions in 1 cycle", 0, 8, listOf("ADVANCED_MATH"))
)

@Composable
fun AcademyScreen(
    userSettingsRepository: UserSettingsRepository,
    onNavigateBack: () -> Unit
) {
    val totalXp by userSettingsRepository.totalXp.collectAsState(0)
    val unlockedSkills by userSettingsRepository.unlockedSkills.collectAsState(emptySet())
    val level = userSettingsRepository.getLevel(totalXp)
    val scope = rememberCoroutineScope()

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
            Text("TECH TREE", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("CURRENT XP: $totalXp", color = Color.Magenta, fontWeight = FontWeight.Bold)
        Text("LEVEL: $level", color = Color.White, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

        TutorialSection()

        Spacer(modifier = Modifier.height(24.dp))

        Text("TECH TREE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(SkillTree) { skill ->
                val isUnlocked = unlockedSkills.contains(skill.id)
                val canUnlock = level >= skill.requiredLevel &&
                                skill.dependencies.all { unlockedSkills.contains(it) } &&
                                !isUnlocked

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.Cyan.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.3f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUnlocked) Color.Cyan else if (canUnlock) Color.White else Color.Gray
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(skill.name, color = if (isUnlocked) Color.Cyan else Color.White, fontWeight = FontWeight.Bold)
                            Text(skill.description, color = Color.Gray, fontSize = 12.sp)
                            if (skill.dependencies.isNotEmpty()) {
                                Text("REQUIRES: ${skill.dependencies.joinToString()}", color = Color.Red.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }

                        if (isUnlocked) {
                            Text("UNLOCKED", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        userSettingsRepository.unlockSkill(skill.id)
                                    }
                                },
                                enabled = canUnlock,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)
                            ) {
                                Text("LVL ${skill.requiredLevel}", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
