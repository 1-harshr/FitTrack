package com.harsh.fittrack.ui.feature.record

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.core.util.DurationFormatter
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationError
import com.harsh.fittrack.feature.record.RecordViewModel
import com.harsh.fittrack.feature.record.TemplateViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.record_add_exercise
import com.harsh.fittrack.resources.record_validation_empty_set
import com.harsh.fittrack.resources.record_validation_no_exercises
import com.harsh.fittrack.resources.record_validation_no_sets
import com.harsh.fittrack.resources.record_validation_ok
import com.harsh.fittrack.resources.record_validation_title
import com.harsh.fittrack.resources.record_add_set
import com.harsh.fittrack.resources.record_cancel
import com.harsh.fittrack.resources.record_discard
import com.harsh.fittrack.resources.record_discard_confirm_action
import com.harsh.fittrack.resources.record_discard_confirm_body
import com.harsh.fittrack.resources.record_discard_confirm_title
import com.harsh.fittrack.resources.record_finish
import com.harsh.fittrack.resources.record_finish_confirm_action
import com.harsh.fittrack.resources.record_finish_confirm_body
import com.harsh.fittrack.resources.record_finish_confirm_title
import com.harsh.fittrack.resources.record_kg_col
import com.harsh.fittrack.resources.record_reps_col
import com.harsh.fittrack.resources.record_set_col
import com.harsh.fittrack.resources.record_pre_start
import com.harsh.fittrack.resources.record_pre_title_hint
import com.harsh.fittrack.resources.record_title_hint
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecordWorkoutScreen(
    userId: String,
    onFinished: () -> Unit,
    onDiscard: () -> Unit,
    onOpenExerciseDetail: (exerciseId: String) -> Unit = {},
    addedExerciseId: String? = null,
    onExerciseConsumed: () -> Unit = {},
) {
    val vm: RecordViewModel = koinViewModel()
    val templateVm: TemplateViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val templates by templateVm.templates.collectAsStateWithLifecycle()

    LaunchedEffect(userId) { vm.startOrResumeWorkout(userId) }

    // Exercise added via ExerciseDetail SHEET flow — consume once
    LaunchedEffect(addedExerciseId) {
        if (addedExerciseId != null) {
            vm.addExercise(addedExerciseId)
            onExerciseConsumed()
        }
    }

    var elapsed by remember { mutableStateOf(0L) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }

    // Timer only ticks once the workout has started
    LaunchedEffect(state.hasStarted) {
        if (state.hasStarted) {
            while (true) {
                delay(1000)
                elapsed++
            }
        }
    }

    if (!state.hasStarted) {
        PreWorkoutScreen(
            title = state.title,
            titlePlaceholder = vm.suggestedTitle,
            onTitleChange = { vm.renameTitle(it) },
            onStart = { vm.startWorkout() },
            onDiscard = onDiscard,
            onUseTemplate = { showTemplateSheet = true },
        )
        if (showTemplateSheet) {
            TemplateListSheet(
                templates = templates,
                onSelect = { template ->
                    showTemplateSheet = false
                    vm.startWorkout()
                    template.exercises.forEach { vm.addExercise(it.exerciseId) }
                },
                onDelete = { id -> templateVm.deleteTemplate(id) },
                onDismiss = { showTemplateSheet = false },
            )
        }
        return@RecordWorkoutScreen
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
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            // ── Top bar ───────────────────────────────────────────────
            item {
                RecordTopBar(
                    title = state.title,
                    elapsed = elapsed,
                    onTitleChange = { vm.renameTitle(it) },
                    onDiscard = { showDiscardDialog = true },
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.md, bottom = FitTrackTheme.spacing.md),
                )
            }

            // ── Exercise sections ─────────────────────────────────────
            if (state.exercises.isEmpty()) {
                item {
                    RecordEmptyState(
                        modifier = Modifier
                            .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                            .padding(top = FitTrackTheme.spacing.xl),
                    )
                }
            } else {
                items(state.exercises, key = { it.entry.id }) { exerciseWithSets ->
                    RecordExerciseSection(
                        exerciseWithSets = exerciseWithSets,
                        exerciseName = exerciseWithSets.entry.exerciseName,
                        hasPr = exerciseWithSets.entry.exerciseId in state.newPrExerciseIds,
                        onAddSet = { vm.addSet(exerciseWithSets.entry.id) },
                        onUpdateSet = { vm.updateSet(it) },
                        modifier = Modifier
                            .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                            .padding(bottom = FitTrackTheme.spacing.sm),
                    )
                }
            }

            // ── Add Exercise button ───────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.md)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .clickable { showAddExercise = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.record_add_exercise),
                        style = FitTrackTheme.typography.labelLarge,
                        color = FitTrackTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // ── Finish button (sticky bottom) ─────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(FitTrackTheme.colors.surface)
                .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                .padding(top = FitTrackTheme.spacing.sm, bottom = FitTrackTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FitTrackTheme.colors.primary)
                    .clickable { if (vm.finish(elapsed)) showFinishDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.record_finish),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (state.exercises.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSaveTemplateDialog = true }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Save as Template",
                        style = FitTrackTheme.typography.labelMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }

    // ── Add exercise sheet ────────────────────────────────────────────
    AddExerciseSheet(
        visible = showAddExercise,
        onDismiss = { showAddExercise = false },
        onAdd = { exerciseId -> vm.addExercise(exerciseId) },
        onOpenDetail = { exerciseId -> showAddExercise = false; onOpenExerciseDetail(exerciseId) },
    )

    // ── Discard confirmation dialog ───────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.record_discard_confirm_title),
                    style = FitTrackTheme.typography.headlineSmall,
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.record_discard_confirm_body),
                    style = FitTrackTheme.typography.bodyMedium,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; vm.discard(); onDiscard() }) {
                    Text(
                        text = stringResource(Res.string.record_discard_confirm_action),
                        color = FitTrackTheme.colors.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        text = stringResource(Res.string.record_cancel),
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            },
            containerColor = FitTrackTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // ── Finish confirmation dialog ────────────────────────────────────
    if (showFinishDialog) {
        val totalSets = state.exercises.sumOf { it.sets.size }
        val body = stringResource(Res.string.record_finish_confirm_body)
            .replace("%1\$s", DurationFormatter.minutes(elapsed))
            .replace("%2\$s", state.exercises.size.toString())
            .replace("%3\$s", totalSets.toString())

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.record_finish_confirm_title),
                    style = FitTrackTheme.typography.headlineSmall,
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = body,
                    style = FitTrackTheme.typography.bodyMedium,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { showFinishDialog = false; onFinished() }) {
                    Text(
                        text = stringResource(Res.string.record_finish_confirm_action),
                        color = FitTrackTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(
                        text = stringResource(Res.string.record_cancel),
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            },
            containerColor = FitTrackTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // ── Validation error dialog ───────────────────────────────────────
    if (state.validationErrors.isNotEmpty()) {
        ValidationErrorDialog(
            errors = state.validationErrors,
            onDismiss = { vm.clearValidationErrors() },
        )
    }

    // ── Save as template dialog ───────────────────────────────────────
    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            onConfirm = { name ->
                val exercises = state.exercises.map { ews ->
                    com.harsh.fittrack.domain.model.TemplateExercise(
                        exerciseId = ews.entry.exerciseId,
                        exerciseName = ews.entry.exerciseName,
                        orderIndex = ews.entry.orderIndex,
                    )
                }
                templateVm.createTemplate(name, exercises)
                showSaveTemplateDialog = false
            },
            onDismiss = { showSaveTemplateDialog = false },
        )
    }
}

// ── Validation error dialog ───────────────────────────────────────────────────

@Composable
private fun ValidationErrorDialog(
    errors: List<WorkoutValidationError>,
    onDismiss: () -> Unit,
) {
    val noExercises = stringResource(Res.string.record_validation_no_exercises)
    val noSetsTemplate = stringResource(Res.string.record_validation_no_sets)
    val emptySetTemplate = stringResource(Res.string.record_validation_empty_set)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.record_validation_title),
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm)) {
                errors.forEach { error ->
                    val message = when (error) {
                        is WorkoutValidationError.NoExercises -> noExercises
                        is WorkoutValidationError.NoSets ->
                            noSetsTemplate.replace("%1\$s", error.exerciseName)
                        is WorkoutValidationError.EmptySet ->
                            emptySetTemplate
                                .replace("%1\$s", error.exerciseName)
                                .replace("%2\$s", error.setNumber.toString())
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .background(FitTrackTheme.colors.error, CircleShape)
                        )
                        Text(
                            text = message,
                            style = FitTrackTheme.typography.bodySmall,
                            color = FitTrackTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.record_validation_ok),
                    color = FitTrackTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = FitTrackTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
    )
}

// ── Pre-workout screen ────────────────────────────────────────────────────────

@Composable
private fun PreWorkoutScreen(
    title: String,
    titlePlaceholder: String,
    onTitleChange: (String) -> Unit,
    onStart: () -> Unit,
    onDiscard: () -> Unit,
    onUseTemplate: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FitTrackTheme.spacing.containerMargin),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dumbbell placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(FitTrackTheme.colors.primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.size(44.dp, 10.dp).background(FitTrackTheme.colors.primary.copy(alpha = 0.6f), RoundedCornerShape(3.dp)))
                    Box(Modifier.size(6.dp, 16.dp).background(FitTrackTheme.colors.primary.copy(alpha = 0.4f)))
                    Box(Modifier.size(44.dp, 10.dp).background(FitTrackTheme.colors.primary.copy(alpha = 0.6f), RoundedCornerShape(3.dp)))
                }
            }

            Spacer(Modifier.height(FitTrackTheme.spacing.lg))

            // Editable workout title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FitTrackTheme.colors.surface)
                    .padding(bottom = FitTrackTheme.spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = titlePlaceholder,
                        style = FitTrackTheme.typography.headlineSmall,
                        color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    textStyle = FitTrackTheme.typography.headlineSmall.copy(
                        color = FitTrackTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                    ),
                    cursorBrush = SolidColor(FitTrackTheme.colors.primary),
                    singleLine = true,
                )
            }

            Spacer(Modifier.height(FitTrackTheme.spacing.xl))

            // Start button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FitTrackTheme.colors.primary)
                    .clickable(onClick = onStart),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.record_pre_start),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(FitTrackTheme.spacing.sm))

            // Use Template button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FitTrackTheme.colors.primary.copy(alpha = 0.12f))
                    .clickable(onClick = onUseTemplate),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Use Template",
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(FitTrackTheme.spacing.md))

            // Discard link
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDiscard)
                    .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.record_discard),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun RecordTopBar(
    title: String,
    elapsed: Long,
    onTitleChange: (String) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Timer badge
            Box(
                modifier = Modifier
                    .background(SurfaceContainerHigh, RoundedCornerShape(8.dp))
                    .padding(horizontal = FitTrackTheme.spacing.md, vertical = 6.dp),
            ) {
                Text(
                    text = DurationFormatter.hhmmss(elapsed),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Discard text button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDiscard)
                    .padding(horizontal = FitTrackTheme.spacing.sm, vertical = 6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.record_discard),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.error,
                )
            }
        }

        Spacer(Modifier.height(FitTrackTheme.spacing.sm))

        // Editable title
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (title.isEmpty()) {
                Text(
                    text = stringResource(Res.string.record_title_hint),
                    style = FitTrackTheme.typography.headlineSmall,
                    color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                )
            }
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = FitTrackTheme.typography.headlineSmall.copy(
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
                cursorBrush = SolidColor(FitTrackTheme.colors.primary),
                singleLine = true,
            )
        }
    }
}

// ── Exercise section ──────────────────────────────────────────────────────────

@Composable
private fun RecordExerciseSection(
    exerciseWithSets: ExerciseWithSets,
    exerciseName: String,
    hasPr: Boolean = false,
    onAddSet: () -> Unit,
    onUpdateSet: (SetEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp)),
    ) {
        // Exercise header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(FitTrackTheme.colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(16.dp, 4.dp)
                        .background(FitTrackTheme.colors.primary, RoundedCornerShape(2.dp))
                )
            }
            Text(
                text = exerciseName,
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (hasPr) {
                PrBadge()
            }
        }

        HorizontalDivider(color = FitTrackTheme.colors.outline.copy(alpha = 0.3f))

        // Column headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.record_set_col),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(0.8f),
            )
            Text(
                text = stringResource(Res.string.record_kg_col),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.record_reps_col),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        // Set rows
        exerciseWithSets.sets.forEach { set ->
            EditableSetRow(
                set = set,
                onUpdate = onUpdateSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FitTrackTheme.spacing.md, vertical = 4.dp),
            )
        }

        // Add set
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddSet)
                .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.record_add_set),
                style = FitTrackTheme.typography.labelMedium,
                color = FitTrackTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EditableSetRow(
    set: SetEntry,
    onUpdate: (SetEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Set number badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SurfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = set.setNumber.toString(),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(Modifier.weight(0.8f))

        // Weight field
        SetInputField(
            value = if (set.weight == 0.0) "" else formatWeight(set.weight),
            placeholder = "0",
            onValueChange = { raw ->
                val kg = raw.toDoubleOrNull() ?: 0.0
                onUpdate(set.copy(weight = kg))
            },
            modifier = Modifier.weight(1f),
        )

        // Reps field
        SetInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            placeholder = "0",
            onValueChange = { raw ->
                val reps = raw.toIntOrNull() ?: 0
                onUpdate(set.copy(reps = reps))
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SetInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .padding(end = FitTrackTheme.spacing.sm)
            .background(SurfaceContainerHighest, RoundedCornerShape(6.dp))
            .padding(horizontal = FitTrackTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = FitTrackTheme.typography.bodySmall.copy(
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            ),
            cursorBrush = SolidColor(FitTrackTheme.colors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun RecordEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        // Dumbbell placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(SurfaceContainerHigh, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    Modifier
                        .size(36.dp, 8.dp)
                        .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
                Box(
                    Modifier
                        .size(4.dp, 12.dp)
                        .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Box(
                    Modifier
                        .size(36.dp, 8.dp)
                        .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
            }
        }
        Text(
            text = "No exercises yet",
            style = FitTrackTheme.typography.bodyLarge,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Tap \"Add Exercise\" to get started",
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
    }
}

private fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()}" else "$kg"
