package com.harsh.fittrack.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harsh.fittrack.ui.theme.FitTrackTheme

@Composable
fun FitTrackLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    cornerRadius: Dp = 24.dp,
    iconSize: TextUnit = 36.sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = FitTrackTheme.colors.primaryContainer,
                shape = RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "⚡", fontSize = iconSize)
    }
}
