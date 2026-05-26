package com.harsh.fittrack.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.ui.feature.record.PrBadge
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh

@Composable
fun SetRow(
    set: SetEntry,
    onChange: (SetEntry) -> Unit,
    onToggleComplete: () -> Unit,
    showPrBadge: Boolean = false,
) {
    val completedBg = FitTrackTheme.colors.primary.copy(alpha = 0.08f)
    val bg = if (set.isCompleted) completedBg else SurfaceContainerHigh

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Set number
        Text(
            text = set.setNumber.toString(),
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center,
        )

        // Weight input
        NumberField(
            value = if (set.weight == 0.0) "" else set.weight.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            },
            placeholder = "kg",
            modifier = Modifier.weight(1f),
            onValueChange = { newVal ->
                val w = newVal.toDoubleOrNull() ?: 0.0
                onChange(set.copy(weight = w))
            },
        )

        Text(
            text = "×",
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )

        // Reps input
        NumberField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            placeholder = "reps",
            modifier = Modifier.weight(1f),
            onValueChange = { newVal ->
                val r = newVal.toIntOrNull() ?: 0
                onChange(set.copy(reps = r))
            },
        )

        // PR badge
        if (showPrBadge) {
            PrBadge()
        } else {
            Spacer(Modifier.width(28.dp))
        }

        // Done checkmark
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (set.isCompleted) FitTrackTheme.colors.primary
                    else FitTrackTheme.colors.surface
                )
                .border(
                    width = 1.5.dp,
                    color = if (set.isCompleted) FitTrackTheme.colors.primary
                    else FitTrackTheme.colors.outline,
                    shape = CircleShape,
                )
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center,
        ) {
            if (set.isCompleted) {
                Text(
                    text = "✓",
                    style = FitTrackTheme.typography.labelSmall,
                    color = FitTrackTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) { mutableStateOf(value) }

    BasicTextField(
        value = text,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' }
            text = filtered
            onValueChange(filtered)
        },
        modifier = modifier
            .height(36.dp)
            .background(FitTrackTheme.colors.surface, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = FitTrackTheme.typography.bodySmall.copy(
            color = FitTrackTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(FitTrackTheme.colors.primary),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = FitTrackTheme.typography.bodySmall,
                        color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                }
                inner()
            }
        },
    )
}
