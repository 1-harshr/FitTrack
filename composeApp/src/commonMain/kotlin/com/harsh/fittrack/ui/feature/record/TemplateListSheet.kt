package com.harsh.fittrack.ui.feature.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.domain.model.WorkoutTemplate
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListSheet(
    templates: List<WorkoutTemplate>,
    onSelect: (WorkoutTemplate) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FitTrackTheme.colors.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Workout Templates",
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = FitTrackTheme.spacing.containerMargin,
                    vertical = FitTrackTheme.spacing.md,
                ),
            )
            HorizontalDivider(color = FitTrackTheme.colors.outline.copy(alpha = 0.3f))

            if (templates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FitTrackTheme.spacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No templates yet. Finish a workout and save it as a template.",
                        style = FitTrackTheme.typography.bodySmall,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(templates, key = { it.id }) { template ->
                        TemplateRow(
                            template = template,
                            onSelect = { onSelect(template) },
                            onDelete = { onDelete(template.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(
    template: WorkoutTemplate,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FitTrackTheme.spacing.containerMargin, vertical = FitTrackTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.name,
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${template.exercises.size} exercise${if (template.exercises.size != 1) "s" else ""}",
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
            )
        }

        // Delete button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onDelete)
                .background(FitTrackTheme.colors.error.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Delete",
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.error,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Start button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onSelect)
                .background(FitTrackTheme.colors.primary)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Use",
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    HorizontalDivider(
        color = FitTrackTheme.colors.outline.copy(alpha = 0.15f),
        modifier = Modifier.padding(horizontal = FitTrackTheme.spacing.containerMargin),
    )
}
