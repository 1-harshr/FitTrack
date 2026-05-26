package com.harsh.fittrack.feature.record

import com.harsh.fittrack.domain.model.PersonalRecord

fun detectNewPr(currentPr: PersonalRecord?, weightKg: Double, reps: Int): Boolean {
    if (weightKg <= 0 || reps <= 0) return false
    val candidateOrm = estimatedOneRm(weightKg, reps)
    val existingOrm = if (currentPr != null) estimatedOneRm(currentPr.maxWeightKg, currentPr.maxReps) else 0.0
    return candidateOrm > existingOrm
}

private fun estimatedOneRm(weightKg: Double, reps: Int): Double =
    weightKg * (1.0 + reps / 30.0)
