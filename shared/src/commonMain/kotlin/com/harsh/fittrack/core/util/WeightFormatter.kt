package com.harsh.fittrack.core.util

import com.harsh.fittrack.domain.model.Units

/** Converts and formats weight values. Weights are stored in kg internally. */
object WeightFormatter {
    private const val LBS_PER_KG = 2.20462

    fun convert(weightKg: Double, units: Units): Double = when (units) {
        Units.KG -> weightKg
        Units.LBS -> weightKg * LBS_PER_KG
    }

    fun format(weightKg: Double, units: Units): String {
        val v = convert(weightKg, units)
        val suffix = if (units == Units.KG) "kg" else "lbs"
        return "${v.formatOneDecimal()} $suffix"
    }

    private fun Double.formatOneDecimal(): String {
        val rounded = kotlin.math.round(this * 10) / 10
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
}
