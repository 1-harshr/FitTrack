package com.harsh.fittrack.ui.feature.record

import androidx.compose.runtime.Composable

/** Modal bottom sheet: search + muscle filter + tappable exercise rows. */
@Composable
fun AddExerciseSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (exerciseId: String) -> Unit,
    onOpenDetail: (exerciseId: String) -> Unit,
) {
    // TODO
}
