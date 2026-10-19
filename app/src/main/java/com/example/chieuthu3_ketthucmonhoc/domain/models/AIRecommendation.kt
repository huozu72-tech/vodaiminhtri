package com.example.chieuthu3_ketthucmonhoc.domain.models

data class AIRecommendation(
    val menuItem: MenuItem,
    val score: Double,
    val reason: String
)
