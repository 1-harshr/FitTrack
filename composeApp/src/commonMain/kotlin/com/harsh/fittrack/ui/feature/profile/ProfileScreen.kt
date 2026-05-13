package com.harsh.fittrack.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.feature.profile.ProfileViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.profile_banner_cta
import com.harsh.fittrack.resources.profile_cancel
import com.harsh.fittrack.resources.profile_settings_notifications
import com.harsh.fittrack.resources.profile_settings_security
import com.harsh.fittrack.resources.profile_settings_units
import com.harsh.fittrack.resources.profile_sign_out
import com.harsh.fittrack.resources.profile_sign_out_confirm_action
import com.harsh.fittrack.resources.profile_sign_out_confirm_body
import com.harsh.fittrack.resources.profile_sign_out_confirm_title
import com.harsh.fittrack.resources.profile_stat_lifted
import com.harsh.fittrack.resources.profile_stat_sessions
import com.harsh.fittrack.resources.profile_stat_streak
import com.harsh.fittrack.resources.profile_this_month
import com.harsh.fittrack.resources.profile_title
import com.harsh.fittrack.resources.profile_welcome_back
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.OutlineVariant
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
) {
    val vm: ProfileViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                vm.signOut()
                onSignedOut()
            },
            onDismiss = { showSignOutDialog = false },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── Top bar ───────────────────────────────────────────────────
            item {
                ProfileTopBar(
                    initials = state.user?.initials() ?: "",
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.md, bottom = FitTrackTheme.spacing.lg),
                )
            }

            // ── Identity hero ─────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.lg),
                ) {
                    Text(
                        text = stringResource(Res.string.profile_welcome_back),
                        style = FitTrackTheme.typography.bodyMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                    Text(
                        text = state.user?.name ?: "",
                        style = FitTrackTheme.typography.headlineLarge,
                        color = FitTrackTheme.colors.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            // ── Stats row ─────────────────────────────────────────────────
            item {
                ProfileStatsRow(
                    totalWorkouts = state.totalWorkouts,
                    streakDays = state.streakDays,
                    totalVolumeKg = state.totalVolumeKg,
                    thisMonthVolumeKg = state.totalVolumeThisMonthKg,
                    units = state.units,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.lg),
                )
            }

            // ── Settings list ─────────────────────────────────────────────
            item {
                SettingsSection(
                    units = state.units,
                    onToggleUnits = { vm.toggleUnits() },
                    onSignOut = { showSignOutDialog = true },
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.lg),
                )
            }

            // ── Motivational banner ───────────────────────────────────────
            item {
                MotivationalBanner(
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin),
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTopBar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(FitTrackTheme.colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = FitTrackTheme.typography.labelLarge,
                color = FitTrackTheme.colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(FitTrackTheme.spacing.sm))

        Text(
            text = stringResource(Res.string.profile_title),
            style = FitTrackTheme.typography.headlineSmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        // Settings icon placeholder — replace with Icon when asset ready
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .clickable { /* TODO: navigate to settings */ },
            contentAlignment = Alignment.Center,
        ) {
            SettingsIconPlaceholder()
        }
    }
}

@Composable
private fun SettingsIconPlaceholder() {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(3) {
            Box(
                Modifier
                    .size(14.dp, 2.dp)
                    .background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(1.dp))
            )
        }
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileStatsRow(
    totalWorkouts: Int,
    streakDays: Int,
    totalVolumeKg: Double,
    thisMonthVolumeKg: Double,
    units: Units,
    modifier: Modifier = Modifier,
) {
    val volumeDisplay = totalVolumeKg.toTons(units)
    val thisMonthDisplay = thisMonthVolumeKg.toTons(units)
    val tonsSuffix = if (units == Units.KG) "t" else "t" // tonnes display regardless

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        ProfileStatCard(
            value = totalWorkouts.toString(),
            label = stringResource(Res.string.profile_stat_sessions),
            modifier = Modifier.weight(1f),
        )
        ProfileStatCard(
            value = "$streakDays",
            label = stringResource(Res.string.profile_stat_streak),
            unit = "days",
            modifier = Modifier.weight(1f),
        )
        ProfileStatCard(
            value = "$volumeDisplay$tonsSuffix",
            label = stringResource(Res.string.profile_stat_lifted),
            unit = if (thisMonthVolumeKg > 0) "+${thisMonthDisplay}t ${stringResource(Res.string.profile_this_month)}" else null,
            unitColor = FitTrackTheme.colors.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    unitColor: Color = FitTrackTheme.colors.onSurfaceVariant,
) {
    Column(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(FitTrackTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = FitTrackTheme.typography.headlineMedium,
            color = FitTrackTheme.colors.primary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (unit != null) {
            Text(
                text = unit,
                style = FitTrackTheme.typography.labelSmall,
                color = unitColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Settings section ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    units: Units,
    onToggleUnits: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp)),
    ) {
        SettingsRow(
            label = stringResource(Res.string.profile_settings_units),
            trailingText = units.name,
            onClick = onToggleUnits,
            leadingIcon = { RulerIconPlaceholder() },
        )
        RowDivider()
        SettingsRow(
            label = stringResource(Res.string.profile_settings_notifications),
            onClick = { /* TODO */ },
            showChevron = true,
            leadingIcon = { BellIconPlaceholder() },
        )
        RowDivider()
        SettingsRow(
            label = stringResource(Res.string.profile_settings_security),
            onClick = { /* TODO */ },
            showChevron = true,
            leadingIcon = { ShieldIconPlaceholder() },
        )
        RowDivider()
        SettingsRow(
            label = stringResource(Res.string.profile_sign_out),
            labelColor = FitTrackTheme.colors.error,
            onClick = onSignOut,
            leadingIcon = { LogoutIconPlaceholder() },
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = FitTrackTheme.colors.onSurface,
    trailingText: String? = null,
    showChevron: Boolean = false,
    leadingIcon: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
    ) {
        // Leading icon
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            leadingIcon()
        }

        Text(
            text = label,
            style = FitTrackTheme.typography.bodyMedium,
            color = labelColor,
            modifier = Modifier.weight(1f),
        )

        // Trailing content
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = FitTrackTheme.typography.labelLarge,
                color = FitTrackTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        if (showChevron) {
            ChevronPlaceholder()
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = FitTrackTheme.spacing.md),
        color = OutlineVariant,
        thickness = 0.5.dp,
    )
}

// ── Motivational banner ───────────────────────────────────────────────────────

@Composable
private fun MotivationalBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        // Gradient overlay — replace with AsyncImage when photo asset is ready
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FitTrackTheme.colors.primaryContainer.copy(alpha = 0.15f),
                            FitTrackTheme.colors.surface.copy(alpha = 0.7f),
                        )
                    )
                ),
        )

        // Accent bar
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(4.dp)
                .height(160.dp)
                .background(FitTrackTheme.colors.primaryContainer),
        )

        Text(
            text = stringResource(Res.string.profile_banner_cta),
            style = FitTrackTheme.typography.headlineSmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(FitTrackTheme.spacing.lg),
        )
    }
}

// ── Sign-out dialog ───────────────────────────────────────────────────────────

@Composable
private fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerHigh,
        title = {
            Text(
                text = stringResource(Res.string.profile_sign_out_confirm_title),
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.profile_sign_out_confirm_body),
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(Res.string.profile_sign_out_confirm_action),
                    color = FitTrackTheme.colors.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.profile_cancel),
                    color = FitTrackTheme.colors.onSurfaceVariant,
                )
            }
        },
    )
}

// ── Icon placeholders ─────────────────────────────────────────────────────────
// Replace each with Icon(painter = painterResource(Res.drawable.ic_*)) when assets are ready.

@Composable
private fun RulerIconPlaceholder() {
    Box(Modifier.size(18.dp, 4.dp).background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(2.dp)))
}

@Composable
private fun BellIconPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Box(Modifier.size(12.dp, 8.dp).background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 1.dp, bottomEnd = 1.dp)))
        Box(Modifier.size(5.dp, 2.dp).background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)))
    }
}

@Composable
private fun ShieldIconPlaceholder() {
    Box(Modifier.size(14.dp, 16.dp).background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 4.dp, bottomEnd = 4.dp)))
}

@Composable
private fun LogoutIconPlaceholder() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(Modifier.size(10.dp, 2.dp).background(FitTrackTheme.colors.error, RoundedCornerShape(1.dp)))
        Box(Modifier.size(5.dp, 8.dp).background(FitTrackTheme.colors.error, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)))
    }
}

@Composable
private fun ChevronPlaceholder() {
    Box(
        Modifier
            .size(6.dp, 10.dp)
            .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.initials(): String =
    trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }

private fun com.harsh.fittrack.domain.model.User.initials(): String = name.initials()

private fun Double.toTons(units: Units): String {
    val kg = if (units == Units.LBS) this * 2.20462 else this
    val tons = kg / 1000.0
    val rounded = (tons * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
}
