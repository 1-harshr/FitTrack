package com.harsh.fittrack.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.ic_splash_bolt
import com.harsh.fittrack.resources.record_cancel
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FitTrackLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    cornerRadius: Dp = 24.dp,
    iconSize: Dp = 48.dp,
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
        Image(
            painter = painterResource(Res.drawable.ic_splash_bolt),
            contentDescription = stringResource(Res.string.record_cancel),
            modifier = Modifier.size(iconSize)
        )
    }
}
