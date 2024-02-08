package com.example.nightingaleplayer.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nightingaleplayer.ui.audio.AudioViewModel
import com.example.nightingaleplayer.ui.audio.HomeScreen
import com.example.nightingaleplayer.ui.audio.SelectedSongScreen
import com.example.nightingaleplayer.ui.audio.UIEvents

@Composable
fun AppNavHost(
    viewModel: AudioViewModel, // note: this likely breaks mvvm architecture since a view now gets passed a viewmodel, but trying to pass parameters without doing this causes a LOT of extra clutter
    navController: NavHostController,
    startDestination: String = NavigationItem.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier,
    ) {
        composable(route = NavigationItem.Home.route) {
            HomeScreen(
                reload = { viewModel.loadAudioData() },
                progress = viewModel.progress,
                onProgress = { viewModel.onUiEvents(UIEvents.SeekTo(it)) },
                isAudioPlaying = viewModel.isPlaying,
                audioList = viewModel.audioList,
                currentAudio = viewModel.currentAudio,
                onStart = {
                    viewModel.onUiEvents(UIEvents.PlayPause)
                },
                onItemClick = {
                    viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))   // commenting this out allows AudioItem's to skip recomposition
                    navController.navigate(Screens.SELECTEDSONG.name)
                },
                onNext = {
                    viewModel.onUiEvents(UIEvents.SeekToNext)
                },
                onPrevious = {
                    viewModel.onUiEvents(UIEvents.SeekToPrevious)
                }
            )
        }
        composable(route = NavigationItem.SelectedSong.route) {
            SelectedSongScreen(
                title = viewModel.currentAudio.title, // note if on this screen and song changes, it does not update correctly
                onClick = {
                    navController.navigate(Screens.HOME.name)
                }
            )
        }
    }
}