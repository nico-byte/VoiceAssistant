package com.example.voiceassistant

import android.graphics.drawable.Icon
import androidx.compose.ui.Modifier

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Help : NavigationItem("profile", R.drawable.round_help_center_24, "Help")
    object Mic : NavigationItem("music", R.drawable.round_mic_none_24, "Speak")
    object Add : NavigationItem("movies", R.drawable.round_note_add_24, "Add")
}