package com.harsh.fittrack.ui.feature.progress

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.feature.exercises.ExercisesViewModel
import com.harsh.fittrack.feature.progress.ProgressViewModel
import com.harsh.fittrack.ui.component.chart.BarChart
import com.harsh.fittrack.ui.component.chart.DonutChart
import com.harsh.fittrack.ui.component.chart.LineChart
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProgressScreen() {
    val vm: ProgressViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val exercisesVm: ExercisesViewModel = koinViewModel()
    val exercisesState by exercisesVm.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Progress",
                style = FitTrackTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            ChartCard(title = "Weekly Volume (last 8 weeks)") {
                if (state.weeklyVolume.isEmpty()) {
                    EmptyChart("Log workouts to see weekly volume")
                } else {
                    BarChart(
                        data = state.weeklyVolume,
                        barColor = FitTrackTheme.colors.primary,
                        labelColor = FitTrackTheme.colors.onSurfaceVariant,
                        modifier = Modifier.height(180.dp),
                    )
                }
            }
        }

        item {
            ChartCard(title = "Strength Progression") {
                var expanded by remember { mutableStateOf(false) }
                val selectedExercise = exercisesState.results.find { it.id == state.selectedExerciseId }

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedExercise?.name ?: "Select Exercise",
                            style = FitTrackTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "▼",
                            style = FitTrackTheme.typography.bodySmall,
                            color = FitTrackTheme.colors.onSurfaceVariant,
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        exercisesState.results.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.name, style = FitTrackTheme.typography.bodyMedium) },
                                onClick = {
                                    vm.selectExercise(exercise.id)
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val prog = state.exerciseProgression
                when {
                    state.selectedExerciseId == null -> {
                        EmptyChart("Select an exercise to see progression")
                    }
                    state.progressionLoading -> {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(32.dp))
                        }
                    }
                    prog == null || prog.points.size < 2 -> {
                        EmptyChart("Not enough data — log this exercise in at least 2 workouts")
                    }
                    else -> {
                        Text(
                            text = prog.exerciseName,
                            style = FitTrackTheme.typography.labelMedium,
                            color = FitTrackTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        LineChart(
                            data = prog.points,
                            lineColor = FitTrackTheme.colors.primary,
                            fillColor = FitTrackTheme.colors.primary,
                            labelColor = FitTrackTheme.colors.onSurfaceVariant,
                            modifier = Modifier.height(180.dp),
                        )
                    }
                }
            }
        }

        item {
            ChartCard(title = "Muscle Frequency (last 30 days)") {
                if (state.muscleFrequency.isEmpty()) {
                    EmptyChart("Log workouts to see muscle frequency")
                } else {
                    DonutChart(
                        data = state.muscleFrequency,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = FitTrackTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
    }
}

@Composable
private fun EmptyChart(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
    }
}
