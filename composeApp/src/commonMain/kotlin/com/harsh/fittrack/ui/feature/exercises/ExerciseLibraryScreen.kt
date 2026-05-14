package com.harsh.fittrack.ui.feature.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.domain.model.Equipment
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.feature.exercises.ExercisesViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.exercises_cardio
import com.harsh.fittrack.resources.exercises_count_suffix
import com.harsh.fittrack.resources.exercises_filter_all
import com.harsh.fittrack.resources.exercises_search_hint
import com.harsh.fittrack.resources.exercises_strength
import com.harsh.fittrack.resources.exercises_title
import com.harsh.fittrack.resources.exercises_training_focus
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.OutlineVariant
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExerciseLibraryScreen(
    onExerciseClick: (exerciseId: String) -> Unit,
) {
    val vm: ExercisesViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

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
                ExercisesTopBar(
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.md, bottom = FitTrackTheme.spacing.md),
                )
            }

            // ── Search bar ────────────────────────────────────────────────
            item {
                ExercisesSearchBar(
                    query = state.query,
                    onQueryChange = { vm.setQuery(it) },
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.sm),
                )
            }

            // ── Muscle group filter chips ──────────────────────────────────
            item {
                MuscleGroupChips(
                    selected = state.activeMuscleGroup,
                    onSelect = { vm.setMuscleGroup(it) },
                    modifier = Modifier.padding(bottom = FitTrackTheme.spacing.md),
                )
            }

            // ── Exercise rows ─────────────────────────────────────────────
            items(state.results, key = { it.id }) { exercise ->
                ExerciseRow(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = 1.dp),
                )
            }

            // ── Training focus cards ──────────────────────────────────────
            item {
                TrainingFocusSection(
                    strengthCount = state.strengthCount,
                    cardioCount = state.cardioCount,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.lg),
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ExercisesTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.exercises_title),
            style = FitTrackTheme.typography.headlineSmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
        )
        // Settings icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .clickable { },
            contentAlignment = Alignment.Center,
        ) {
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
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun ExercisesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(horizontal = FitTrackTheme.spacing.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            // Search icon placeholder
            Box(
                Modifier
                    .size(18.dp)
                    .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.exercises_search_hint),
                        style = FitTrackTheme.typography.bodyMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = FitTrackTheme.typography.bodyMedium.copy(
                        color = FitTrackTheme.colors.onSurface,
                    ),
                    cursorBrush = SolidColor(FitTrackTheme.colors.primary),
                    singleLine = true,
                )
            }
        }
    }
}

// ── Muscle group chips ────────────────────────────────────────────────────────

@Composable
private fun MuscleGroupChips(
    selected: MuscleGroup?,
    onSelect: (MuscleGroup?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val all = listOf(null) + MuscleGroup.entries
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = FitTrackTheme.spacing.containerMargin),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        all.forEach { group ->
            val isSelected = group == selected
            val label = group?.displayName() ?: stringResource(Res.string.exercises_filter_all)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) FitTrackTheme.colors.primary
                        else SurfaceContainerHigh,
                    )
                    .clickable { onSelect(group) }
                    .padding(horizontal = FitTrackTheme.spacing.md, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    style = FitTrackTheme.typography.labelLarge,
                    color = if (isSelected) FitTrackTheme.colors.onPrimary
                            else FitTrackTheme.colors.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Exercise row ──────────────────────────────────────────────────────────────

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
    ) {
        // Dumbbell icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(FitTrackTheme.colors.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(20.dp, 5.dp).background(FitTrackTheme.colors.primary, RoundedCornerShape(2.dp)))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = exercise.name,
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${exercise.primaryMuscle.displayName()} · ${exercise.movementType.displayName()}",
                style = FitTrackTheme.typography.labelMedium,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        }

        // Chevron placeholder
        Box(
            Modifier
                .size(6.dp, 10.dp)
                .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
        )
    }
}

// ── Training focus ────────────────────────────────────────────────────────────

@Composable
private fun TrainingFocusSection(
    strengthCount: Int,
    cardioCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm)) {
        Text(
            text = stringResource(Res.string.exercises_training_focus),
            style = FitTrackTheme.typography.labelLarge,
            color = FitTrackTheme.colors.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = FitTrackTheme.spacing.xs),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm)) {
            FocusCard(
                label = stringResource(Res.string.exercises_strength),
                count = strengthCount,
                accentColor = FitTrackTheme.colors.primaryContainer,
                modifier = Modifier.weight(1f),
            )
            FocusCard(
                label = stringResource(Res.string.exercises_cardio),
                count = cardioCount,
                accentColor = FitTrackTheme.colors.tertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FocusCard(
    label: String,
    count: Int,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val suffix = stringResource(Res.string.exercises_count_suffix)
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHighest),
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.1f), accentColor.copy(alpha = 0.02f)))
                )
        )
        // Top accent
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(FitTrackTheme.spacing.md),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = label,
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "$count $suffix",
                style = FitTrackTheme.typography.labelMedium,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        }
    }
}

// ── Display name helpers ──────────────────────────────────────────────────────

private fun MuscleGroup.displayName(): String = when (this) {
    MuscleGroup.CHEST -> "Chest"
    MuscleGroup.BACK -> "Back"
    MuscleGroup.LEGS -> "Legs"
    MuscleGroup.SHOULDERS -> "Shoulders"
    MuscleGroup.ARMS -> "Arms"
    MuscleGroup.CORE -> "Core"
    MuscleGroup.GLUTES -> "Glutes"
    MuscleGroup.CALVES -> "Calves"
}

private fun MovementType.displayName(): String = when (this) {
    MovementType.COMPOUND -> "Compound"
    MovementType.ISOLATION -> "Isolation"
    MovementType.CARDIO -> "Cardio"
}
