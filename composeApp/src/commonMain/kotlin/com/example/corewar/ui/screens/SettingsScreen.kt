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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corewar.data.UserSettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userSettingsRepository: UserSettingsRepository,
    onNavigateBack: () -> Unit
) {
    val theme by userSettingsRepository.theme.collectAsStateWithLifecycle("STANDARD")
    val chaosMode by userSettingsRepository.chaosMode.collectAsStateWithLifecycle(false)
    val memorySize by userSettingsRepository.memorySize.collectAsStateWithLifecycle(8000)
    val maxCycles by userSettingsRepository.maxCycles.collectAsStateWithLifecycle(80000)
    val editorFontSize by userSettingsRepository.editorFontSize.collectAsStateWithLifecycle(14)
    val autocompleteEnabled by userSettingsRepository.autocompleteEnabled.collectAsStateWithLifecycle(true)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.height(16.dp))

        Text("CORE SIZE: $memorySize", color = Color.White, fontWeight = FontWeight.Bold)
        Slider(
            value = memorySize.toFloat(),
            onValueChange = { scope.launch { userSettingsRepository.setMemorySize(it.toInt()) } },
            valueRange = 1024f..16384f,
            steps = 15
        )

        Text("MAX CYCLES: $maxCycles", color = Color.White, fontWeight = FontWeight.Bold)
        Slider(
            value = maxCycles.toFloat(),
            onValueChange = { scope.launch { userSettingsRepository.setMaxCycles(it.toInt()) } },
            valueRange = 10000f..200000f,
            steps = 19
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("EDITOR PREFERENCES", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AUTOCOMPLETE", color = Color.White, fontWeight = FontWeight.Bold)
            Switch(checked = autocompleteEnabled, onCheckedChange = { scope.launch { userSettingsRepository.setAutocompleteEnabled(it) } })
        }

        Text("FONT SIZE: $editorFontSize", color = Color.White, fontWeight = FontWeight.Bold)
        Slider(
            value = editorFontSize.toFloat(),
            onValueChange = { scope.launch { userSettingsRepository.setEditorFontSize(it.toInt()) } },
            valueRange = 8f..24f
        )

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
