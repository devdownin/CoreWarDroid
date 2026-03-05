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
import androidx.compose.ui.text.input.TransformedText

@Composable
fun RedcodeEditor(
    code: String,
    onCodeChanged: (String) -> Unit,
    errors: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Color.Black)) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChanged,
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
