package com.harsh.fittrack.ui.feature.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.feature.exercises.ExercisesViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.add_exercise_filter_all
import com.harsh.fittrack.resources.add_exercise_search_hint
import com.harsh.fittrack.resources.add_exercise_title
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Modal bottom sheet: search + muscle filter + tappable exercise rows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (exerciseId: String) -> Unit,
    onOpenDetail: (exerciseId: String) -> Unit,
) {
    if (!visible) return

    val vm: ExercisesViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FitTrackTheme.colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .background(
                        FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                        CircleShape,
                    )
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Title ─────────────────────────────────────────────────
            Text(
                text = stringResource(Res.string.add_exercise_title),
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = FitTrackTheme.spacing.containerMargin,
                    vertical = FitTrackTheme.spacing.sm,
                ),
            )

            // ── Search bar ────────────────────────────────────────────
            SheetSearchBar(
                query = state.query,
                onQueryChange = { vm.setQuery(it) },
                modifier = Modifier
                    .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                    .padding(bottom = FitTrackTheme.spacing.sm),
            )

            // ── Muscle chips ──────────────────────────────────────────
            SheetMuscleChips(
                selected = state.activeMuscleGroup,
                onSelect = { vm.setMuscleGroup(it) },
                modifier = Modifier.padding(bottom = FitTrackTheme.spacing.sm),
            )

            // ── Exercise rows ─────────────────────────────────────────
            LazyColumn(contentPadding = PaddingValues(bottom = 40.dp)) {
                items(state.results, key = { it.id }) { exercise ->
                    SheetExerciseRow(
                        exercise = exercise,
                        onAdd = { onAdd(exercise.id); onDismiss() },
                        onDetail = { onOpenDetail(exercise.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                            .padding(bottom = 1.dp),
                    )
                }
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SheetSearchBar(
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
            Box(
                Modifier
                    .size(18.dp)
                    .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.add_exercise_search_hint),
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
private fun SheetMuscleChips(
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
            val label = group?.displayName() ?: stringResource(Res.string.add_exercise_filter_all)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) FitTrackTheme.colors.primary else SurfaceContainerHigh,
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
private fun SheetExerciseRow(
    exercise: Exercise,
    onAdd: () -> Unit,
    onDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onDetail)
            .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
    ) {
        // Dumbbell icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    FitTrackTheme.colors.primaryContainer.copy(alpha = 0.15f),
                    RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(20.dp, 5.dp)
                    .background(FitTrackTheme.colors.primary, RoundedCornerShape(2.dp))
            )
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

        // Plus button — tapping adds directly without opening detail
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(FitTrackTheme.colors.primary.copy(alpha = 0.15f))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(14.dp, 2.dp)
                    .background(FitTrackTheme.colors.primary, RoundedCornerShape(1.dp))
            )
            Box(
                Modifier
                    .size(2.dp, 14.dp)
                    .background(FitTrackTheme.colors.primary, RoundedCornerShape(1.dp))
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
