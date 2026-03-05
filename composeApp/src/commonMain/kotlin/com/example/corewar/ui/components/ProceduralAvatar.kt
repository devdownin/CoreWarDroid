package com.example.corewar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.corewar.model.CoreWarColor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun ProceduralAvatar(
    code: String,
    color: CoreWarColor,
    modifier: Modifier = Modifier.size(64.dp)
) {
    val seed = code.hashCode().toLong()
    val random = kotlin.random.Random(seed)

    val sides = random.nextInt(3, 8)
    val points = (0 until sides).map { i ->
        val angle = (2 * PI * i) / sides
        val radiusMultiplier = random.nextDouble(0.4, 1.0)
        angle to radiusMultiplier
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        val path = Path()
        points.forEachIndexed { index, (angle, rMult) ->
            val r = radius * rMult
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        val composeColor = Color(color.argb)
        drawPath(path = path, color = composeColor, alpha = 0.3f)
        drawPath(path = path, color = composeColor, style = Stroke(width = 2.dp.toPx()))

        val detailCount = random.nextInt(2, 5)
        repeat(detailCount) {
             val r = radius * random.nextDouble(0.2, 0.8).toFloat()
             drawCircle(color = composeColor, radius = r, center = center, style = Stroke(width = 1.dp.toPx()), alpha = 0.5f)
        }
    }
}
