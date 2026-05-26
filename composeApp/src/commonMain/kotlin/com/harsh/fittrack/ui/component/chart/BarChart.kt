package com.harsh.fittrack.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harsh.fittrack.domain.model.WeeklyVolumePoint
import kotlin.math.roundToInt

@Composable
fun BarChart(
    data: List<WeeklyVolumePoint>,
    barColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 10.sp) }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas

        val maxVolume = data.maxOf { it.volumeKg }.coerceAtLeast(1.0)
        val paddingBottom = 32.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingHorizontal = 8.dp.toPx()
        val chartWidth = size.width - paddingHorizontal * 2
        val chartHeight = size.height - paddingBottom - paddingTop

        val barWidth = (chartWidth / data.size) * 0.6f
        val barSpacing = chartWidth / data.size

        data.forEachIndexed { index, point ->
            val barHeight = (point.volumeKg / maxVolume * chartHeight).toFloat()
            val x = paddingHorizontal + index * barSpacing + (barSpacing - barWidth) / 2
            val y = paddingTop + chartHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )

            val label = point.weekLabel
            val measured = textMeasurer.measure(label, labelStyle)
            val labelX = x + barWidth / 2 - measured.size.width / 2
            val labelY = size.height - paddingBottom + 4.dp.toPx()
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(labelX, labelY),
                style = labelStyle.copy(color = labelColor),
            )

            if (barHeight > 20.dp.toPx()) {
                val valueLabel = "${(point.volumeKg / 1000).roundToInt()}k"
                val valueMeasured = textMeasurer.measure(valueLabel, labelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = valueLabel,
                    topLeft = Offset(
                        x + barWidth / 2 - valueMeasured.size.width / 2,
                        y - valueMeasured.size.height - 2.dp.toPx(),
                    ),
                    style = labelStyle.copy(color = labelColor),
                )
            }
        }
    }
}
