package com.harsh.fittrack.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harsh.fittrack.domain.model.ExerciseProgressionPoint

@Composable
fun LineChart(
    data: List<ExerciseProgressionPoint>,
    lineColor: Color,
    fillColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 10.sp) }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.size < 2) return@Canvas

        val maxWeight = data.maxOf { it.maxWeightKg }.coerceAtLeast(1.0)
        val minWeight = data.minOf { it.maxWeightKg }
        val range = (maxWeight - minWeight).coerceAtLeast(1.0)

        val paddingBottom = 32.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingHorizontal = 12.dp.toPx()
        val chartWidth = size.width - paddingHorizontal * 2
        val chartHeight = size.height - paddingBottom - paddingTop

        fun xOf(index: Int) = paddingHorizontal + index * chartWidth / (data.size - 1)
        fun yOf(weight: Double) = paddingTop + chartHeight * (1.0 - (weight - minWeight) / range).toFloat()

        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { index, point ->
            val x = xOf(index)
            val y = yOf(point.maxWeightKg)
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, size.height - paddingBottom)
                fillPath.lineTo(x, y)
            } else {
                val prevX = xOf(index - 1)
                val prevY = yOf(data[index - 1].maxWeightKg)
                val cpX = (prevX + x) / 2
                linePath.cubicTo(cpX, prevY, cpX, y, x, y)
                fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
            }
        }

        val lastX = xOf(data.size - 1)
        fillPath.lineTo(lastX, size.height - paddingBottom)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.3f), Color.Transparent),
                startY = paddingTop,
                endY = size.height - paddingBottom,
            ),
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        data.forEachIndexed { index, point ->
            val x = xOf(index)
            val y = yOf(point.maxWeightKg)
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(x, y))

            if (index % (data.size / 4).coerceAtLeast(1) == 0 || index == data.size - 1) {
                val label = point.date.takeLast(5)
                val measured = textMeasurer.measure(label, labelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(
                        (x - measured.size.width / 2).coerceIn(0f, size.width - measured.size.width),
                        size.height - paddingBottom + 4.dp.toPx(),
                    ),
                    style = labelStyle.copy(color = labelColor),
                )
            }
        }
    }
}
