package com.example.corewar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.corewar.model.BattleState
import com.example.corewar.model.CellType
import com.example.corewar.model.CoreWarColor
import kotlin.math.sqrt

@Composable
fun MemoryVisualizer(
    state: BattleState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 10f)
        offset += offsetChange
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFF050505))) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val cellsPerRow = sqrt(state.memory.size.toDouble()).toInt()
        val cellWidth = width / cellsPerRow
        val cellHeight = height / (state.memory.size / cellsPerRow)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val localX = (tapOffset.x - offset.x) / scale
                        val localY = (tapOffset.y - offset.y) / scale
                        val col = (localX / cellWidth).toInt()
                        val row = (localY / cellHeight).toInt()
                        val index = row * cellsPerRow + col
                        if (index in state.memory.indices) {
                            onCellClick(index)
                        }
                    }
                }
        ) {
            state.memory.forEachIndexed { index, cell ->
                val row = index / cellsPerRow
                val col = index % cellsPerRow

                val color = when {
                    cell.ownerId != null -> {
                        val owner = state.warriors.find { it.id == cell.ownerId } ?: state.deadWarriors.find { it.id == cell.ownerId }
                        if (owner != null) {
                            val baseColor = Color(owner.color.argb)
                            if (owner.threads.isEmpty()) baseColor.copy(alpha = 0.4f) else baseColor
                        } else {
                            Color.DarkGray
                        }
                    }
                    cell.type == CellType.PROTECTED -> Color.Blue.copy(alpha = 0.3f)
                    cell.type == CellType.VOLATILE -> Color.Red.copy(alpha = 0.3f)
                    else -> Color.DarkGray
                }

                val finalColor = if (state.cycle - cell.lastModifiedCycle < 5 && cell.lastModifiedCycle != -1) {
                    Color.White
                } else {
                    color
                }

                drawRect(
                    color = finalColor,
                    topLeft = Offset(col * cellWidth, row * cellHeight),
                    size = androidx.compose.ui.geometry.Size(cellWidth * 0.9f, cellHeight * 0.9f)
                )

                state.warriors.forEach { warrior ->
                    if (warrior.threads.contains(index)) {
                        drawCircle(
                            color = Color.White,
                            center = Offset(col * cellWidth + cellWidth / 2, row * cellHeight + cellHeight / 2),
                            radius = cellWidth / 3
                        )
                    }
                }
            }
        }
    }
}
