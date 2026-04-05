package com.example.corewar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corewar.ui.components.RedcodeEditor
import com.example.corewar.ui.viewmodel.EditorIntent
import com.example.corewar.ui.viewmodel.EditorViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    initialName: String?,
    initialCode: String?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportEvent.collect { json ->
            clipboardManager.setText(AnnotatedString(json))
        }
    }

    LaunchedEffect(initialName, initialCode) {
        if (initialName != null && initialCode != null) {
            viewModel.handleIntent(EditorIntent.LoadWarrior(initialName, initialCode))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
                    viewModel.handleIntent(EditorIntent.SaveWarrior)
                    true
                } else false
            }
    ) {
        EditorHeader(uiState, viewModel, onNavigateBack)

        Spacer(modifier = Modifier.height(16.dp))

        RedcodeEditor(
            code = uiState.code,
            onCodeChanged = { viewModel.handleIntent(EditorIntent.CodeChanged(it)) },
            errors = uiState.errors,
            fontSize = uiState.fontSize,
            autocompleteEnabled = uiState.autocompleteEnabled,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        EditorFooter(uiState, viewModel)
    }
    }
}

@Composable
fun EditorHeader(uiState: com.example.corewar.ui.viewmodel.EditorUiState, viewModel: EditorViewModel, onNavigateBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
            Text("←", color = Color.White, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("REDCODE EDITOR", color = Color.Magenta, fontWeight = FontWeight.Bold)
            BasicTextField(
                value = uiState.name,
                onValueChange = { viewModel.handleIntent(EditorIntent.NameChanged(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = SolidColor(Color.White)
            )
        }

        Button(
            onClick = { viewModel.handleIntent(EditorIntent.SaveWarrior) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta, contentColor = Color.White),
            enabled = uiState.errors.isEmpty() && uiState.name.isNotBlank() && !uiState.isSaving
        ) {
            Text(if (uiState.isSaving) "SAVING..." else "SAVE")
        }
    }
}

@Composable
fun EditorFooter(uiState: com.example.corewar.ui.viewmodel.EditorUiState, viewModel: EditorViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
             Text("UNLOCKED OPCODES: ${uiState.unlockedOpcodes.joinToString(", ")}", color = Color.Gray, fontSize = 10.sp)
             Text("LEVEL: ${uiState.level}", color = Color.White, fontSize = 12.sp)
        }

        TextButton(onClick = { viewModel.handleIntent(EditorIntent.ExportWarrior) }) {
             Text("EXPORT (COPY)", color = Color.Cyan, fontSize = 10.sp)
        }
    }
}
