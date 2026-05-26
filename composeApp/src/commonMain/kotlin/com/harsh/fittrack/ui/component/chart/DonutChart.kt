package com.harsh.fittrack.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harsh.fittrack.domain.model.MuscleFrequencyPoint

private val donutColors = listOf(
    Color(0xFF6C8EBF),
    Color(0xFF82B366),
    Color(0xFFD79B00),
    Color(0xFF9673A6),
    Color(0xFF00BCD4),
    Color(0xFFE53935),
    Color(0xFF43A047),
    Color(0xFFF57C00),
)

@Composable
fun DonutChart(
    data: List<MuscleFrequencyPoint>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.sessionCount }.toFloat().coerceAtLeast(1f)
    val slices = data.mapIndexed { i, point ->
        Triple(point, point.sessionCount / total * 360f, donutColors[i % donutColors.size])
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 28.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(inset, inset)

                var startAngle = -90f
                slices.forEach { (_, sweep, color) ->
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep - 1f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke),
                    )
                    startAngle += sweep
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            slices.forEach { (point, _, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(10.dp)) {
                        drawCircle(color)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${point.muscle.lowercase().replaceFirstChar { it.uppercase() }} (${point.sessionCount})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
