package com.example.nightingaleplayer.ui.nav

enum class Screens {
    HOME,
    SELECTEDSONG,
}

sealed class NavigationItem(val route: String) {
    object Home : NavigationItem(Screens.HOME.name)
    object SelectedSong : NavigationItem(Screens.SELECTEDSONG.name)
}