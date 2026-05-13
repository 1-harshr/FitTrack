package com.harsh.fittrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val FitTrackShapes = Shapes(
    // Small tags, chips
    extraSmall = RoundedCornerShape(4.dp),
    // Buttons, inputs, interactive elements
    small = RoundedCornerShape(8.dp),
    // Cards, primary containers
    medium = RoundedCornerShape(12.dp),
    // Large panels, sheets
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
