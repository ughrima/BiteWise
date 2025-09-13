package com.example.bitewise.core

fun bmi(kg: Double, cm: Double): Double {
    val m = cm / 100.0
    if (m <= 0.0) return 0.0
    return (kg / (m * m)).let { kotlin.math.round(it * 10) / 10.0 } // 1 decimal
}

fun bmiCategory(bmi: Double): String = when {
    bmi <= 0.0 -> "—"
    bmi < 18.5 -> "Underweight"
    bmi < 25.0 -> "Healthy"
    bmi < 30.0 -> "Overweight"
    else -> "Obese"
}
