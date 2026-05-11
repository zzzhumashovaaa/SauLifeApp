package com.example.saulifeapp.ui.home

import androidx.annotation.DrawableRes

data class HomeQuickAction(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val type: HomeQuickActionType
)

enum class HomeQuickActionType {
    SCAN,
    PHARMACY,
    AI,
    REMINDER,
    PRESCRIPTION
}