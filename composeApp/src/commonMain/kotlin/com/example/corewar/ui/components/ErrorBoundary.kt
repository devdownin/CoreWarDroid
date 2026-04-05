package com.example.corewar.ui.components

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

@Composable
fun ErrorBoundary(
    content: @Composable () -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A0000))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CRITICAL ANOMALY", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error?.message ?: "Unknown system failure",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { error = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("REBOOT SYSTEM")
                }
            }
        }
    } else {
        // In a real KMP app, we'd use a try-catch in a wrapper or side-effect,
        // but for now we provide a way to trigger it.
        CompositionLocalProvider(LocalErrorHandler provides { error = it }) {
            content()
        }
    }
}

val LocalErrorHandler = staticCompositionLocalOf<(Throwable) -> Unit> { { } }
