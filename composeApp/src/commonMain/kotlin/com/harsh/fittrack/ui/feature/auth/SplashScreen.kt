package com.harsh.fittrack.ui.feature.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.feature.auth.AuthState
import com.harsh.fittrack.feature.auth.AuthViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.app_name
import com.harsh.fittrack.resources.splash_tagline
import com.harsh.fittrack.resources.splash_version
import com.harsh.fittrack.ui.component.FitTrackLogo
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onSignedIn: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            is AuthState.SignedIn -> {
                delay(2_000)
                onSignedIn()
            }
            is AuthState.SignedOut -> {
                delay(2_000)
                onSignedOut()
            }
            else -> Unit
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "splashAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "splashScale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        // Centre logo block
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FitTrackLogo()

            Spacer(Modifier.height(FitTrackTheme.spacing.md))

            Text(
                text = stringResource(Res.string.app_name),
                style = FitTrackTheme.typography.displayLarge,
                color = FitTrackTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(FitTrackTheme.spacing.xs))

            Text(
                text = stringResource(Res.string.splash_tagline),
                style = FitTrackTheme.typography.labelLarge,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        }

        // Version watermark
        Text(
            text = stringResource(Res.string.splash_version),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .graphicsLayer(alpha = alpha * 0.5f),
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
    }
}
