package com.harsh.fittrack.ui.component

import androidx.compose.runtime.Composable
import com.harsh.fittrack.domain.model.MuscleGroup

/** Pill row for muscle-group filtering (All · Chest · Back · …). */
@Composable
fun MuscleGroupFilterRow(
    active: MuscleGroup?,
    onSelect: (MuscleGroup?) -> Unit,
) {
    // TODO
}

/** Single rounded pill chip. */
@Composable
fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    // TODO
}
