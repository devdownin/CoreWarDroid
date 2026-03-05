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
import com.example.corewar.data.UserSettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userSettingsRepository: UserSettingsRepository,
    onNavigateBack: () -> Unit
) {
    val theme by userSettingsRepository.theme.collectAsState("STANDARD")
    val chaosMode by userSettingsRepository.chaosMode.collectAsState(false)
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
            Text("SETTINGS", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("BATTLE PARAMETERS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CHAOS MODE", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Random memory glitches and process warps", color = Color.Gray, fontSize = 12.sp)
            }
            Switch(
                checked = chaosMode,
                onCheckedChange = { scope.launch { userSettingsRepository.setChaosMode(it) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Green, checkedTrackColor = Color.Green.copy(alpha = 0.5f))
            )
        }

        HorizontalDivider(color = Color.DarkGray)

        Spacer(modifier = Modifier.height(32.dp))

        Text("VISUAL THEME", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        val themes = listOf("STANDARD", "RETRO", "MATRIX", "NEON")
        themes.forEach { t ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t, color = Color.White, fontWeight = FontWeight.Bold)
                RadioButton(
                    selected = theme == t,
                    onClick = { scope.launch { userSettingsRepository.setTheme(t) } },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Green)
                )
            }
        }
    }
}
