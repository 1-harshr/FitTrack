package com.harsh.fittrack.ui.feature.record

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.ui.theme.FitTrackTheme

@Composable
fun SaveTemplateDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save as Template",
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text("e.g. Push Day A", color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = FitTrackTheme.colors.onSurface,
                    unfocusedTextColor = FitTrackTheme.colors.onSurface,
                    focusedContainerColor = FitTrackTheme.colors.surface,
                    unfocusedContainerColor = FitTrackTheme.colors.surface,
                    focusedIndicatorColor = FitTrackTheme.colors.primary,
                    unfocusedIndicatorColor = FitTrackTheme.colors.outline,
                    cursorColor = FitTrackTheme.colors.primary,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text(
                    text = "Save",
                    color = FitTrackTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FitTrackTheme.colors.onSurfaceVariant)
            }
        },
        containerColor = FitTrackTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
    )
}
