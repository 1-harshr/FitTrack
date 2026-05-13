package com.harsh.fittrack.ui.component

import androidx.compose.runtime.Composable
import com.harsh.fittrack.domain.model.SetEntry

/**
 * Inline set logging row used inside the active workout screen.
 * [set number] [reps input] [weight input] [checkmark]
 */
@Composable
fun SetRow(
    set: SetEntry,
    onChange: (SetEntry) -> Unit,
    onToggleComplete: () -> Unit,
) {
    // TODO
}
