package com.harsh.fittrack.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.feature.auth.AuthState
import com.harsh.fittrack.feature.auth.AuthViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.app_name
import com.harsh.fittrack.resources.app_tagline_short
import com.harsh.fittrack.resources.login_continue_with_apple
import com.harsh.fittrack.resources.login_continue_with_google
import com.harsh.fittrack.resources.login_legal_footer
import com.harsh.fittrack.resources.stat_streak
import com.harsh.fittrack.resources.stat_this_week
import com.harsh.fittrack.resources.stat_workouts
import com.harsh.fittrack.ui.component.FitTrackLogo
import com.harsh.fittrack.ui.component.SocialButton
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.OutlineVariant
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OAuth-only login. [showAppleSignIn] is true on iOS (App Store policy).
 */
@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    showAppleSignIn: Boolean = false,
) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val isLoading = state is AuthState.Loading
    val error = (state as? AuthState.Error)?.message

    LaunchedEffect(state) {
        if (state is AuthState.SignedIn) onSignedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface)
            .systemBarsPadding()
            .padding(horizontal = FitTrackTheme.spacing.containerMargin),
    ) {
        // ── Top spacer ────────────────────────────────────────────────────
        Spacer(Modifier.weight(0.8f))

        // ── Logo + tagline ────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            FitTrackLogo()

            Text(
                text = stringResource(Res.string.app_name),
                style = FitTrackTheme.typography.headlineLarge,
                color = FitTrackTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )

            Text(
                text = stringResource(Res.string.app_tagline_short),
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        }

        // ── Hero stats strip ──────────────────────────────────────────────
        Spacer(Modifier.weight(1f))
        HeroStatsStrip()
        Spacer(Modifier.weight(1f))

        // ── Error message ─────────────────────────────────────────────────
        if (error != null) {
            Text(
                text = error,
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.error,
                modifier = Modifier.padding(bottom = FitTrackTheme.spacing.sm),
            )
        }

        // ── Auth buttons ──────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm)) {
            SocialButton(
                text = stringResource(Res.string.login_continue_with_google),
                providerLabel = "G",
                onClick = { vm.signInWithGoogle() },
                enabled = !isLoading,
                isLoading = isLoading,
            )

            if (showAppleSignIn) {
                SocialButton(
                    text = stringResource(Res.string.login_continue_with_apple),
                    providerLabel = "",
                    onClick = { vm.signInWithApple() },
                    enabled = !isLoading,
                    isLoading = isLoading,
                    filled = true,
                )
            }
        }

        // ── Footer ────────────────────────────────────────────────────────
        Text(
            text = stringResource(Res.string.login_legal_footer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FitTrackTheme.spacing.md),
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

/** Abstract stat cards to fill the hero area — echoes the app's tracking purpose. */
@Composable
private fun HeroStatsStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        StatCard(label = Res.string.stat_workouts, value = "—", modifier = Modifier.weight(1f))
        StatCard(label = Res.string.stat_this_week, value = "—", modifier = Modifier.weight(1f))
        StatCard(label = Res.string.stat_streak, value = "—", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: StringResource, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(FitTrackTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = FitTrackTheme.typography.headlineMedium,
            color = FitTrackTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(label),
            style = FitTrackTheme.typography.labelLarge,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
    }
}
