package com.example.corewar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.corewar.model.Opcode
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText

@Composable
fun RedcodeEditor(
    code: String,
    onCodeChanged: (String) -> Unit,
    errors: List<String>,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(code) {
        mutableStateOf(TextFieldValue(code, androidx.compose.ui.text.TextRange(code.length)))
    }
    val snippets = listOf(
        "Imp" to "MOV 0, 1",
        "Dwarf" to "ADD #4, 3\nMOV 2, @2\nJMP -2\nDAT #0, #0",
        "Paper" to "SPL 1\nMOV -1, 1\nSPL 1",
        "Bomb" to "DAT #0, #0"
    )

    val opcodes = Opcode.values().map { it.name }
    val currentLine = textFieldValue.text.substring(0, textFieldValue.selection.start).lines().lastOrNull() ?: ""
    val lastWord = currentLine.split(Regex("\\s+")).lastOrNull()?.uppercase() ?: ""
    val suggestions = if (lastWord.length >= 2) {
        opcodes.filter { it.startsWith(lastWord) && it != lastWord }
    } else emptyList()

    Column(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("SNIPPETS:", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
            snippets.forEach { (name, snippet) ->
                androidx.compose.material3.SuggestionChip(
                    onClick = { onCodeChanged(if (code.isBlank()) snippet else "$code\n$snippet") },
                    label = { Text(name, fontSize = 10.sp) },
                    border = null,
                    colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.Magenta.copy(alpha = 0.2f),
                        labelColor = Color.Magenta
                    )
                )
            }
        }

        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("AUTOCOMPLETE:", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                suggestions.forEach { suggestion ->
                    androidx.compose.material3.SuggestionChip(
                        onClick = {
                            val before = textFieldValue.text.substring(0, textFieldValue.selection.start - lastWord.length)
                            val after = textFieldValue.text.substring(textFieldValue.selection.start)
                            val newText = before + suggestion + after
                            val newCursor = before.length + suggestion.length
                            textFieldValue = TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor))
                            onCodeChanged(newText)
                        },
                        label = { Text(suggestion, fontSize = 10.sp) },
                        border = null,
                        colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.Cyan.copy(alpha = 0.2f),
                            labelColor = Color.Cyan
                        )
                    )
                }
            }
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                if (it.text != code) {
                    onCodeChanged(it.text)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            textStyle = TextStyle(
                color = Color.Green,
                fontFamily = FontFamily.Monospace,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            ),
            cursorBrush = SolidColor(Color.Green),
            visualTransformation = { text ->
                TransformedText(
                    highlightCode(text.text),
                    OffsetMapping.Identity
                )
            }
        )

        if (errors.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                errors.forEach { error ->
                    Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

fun highlightCode(code: String): AnnotatedString = buildAnnotatedString {
    val opcodes = Opcode.values().map { it.name }
    val lines = code.split("\n")

    lines.forEachIndexed { index, line ->
        val parts = line.split(Regex("\\s+"))
        parts.forEach { part ->
            val cleanPart = part.uppercase()
            when {
                opcodes.contains(cleanPart) -> {
                    withStyle(SpanStyle(color = Color.Yellow)) {
                        append(part)
                    }
                }
                part.startsWith(";") -> {
                    withStyle(SpanStyle(color = Color.Gray)) {
                        append(part)
                    }
                }
                part.any { it.isDigit() } -> {
                    withStyle(SpanStyle(color = Color.Cyan)) {
                        append(part)
                    }
                }
                else -> {
                    append(part)
                }
            }
            append(" ")
        }
        if (index < lines.size - 1) append("\n")
    }
}
