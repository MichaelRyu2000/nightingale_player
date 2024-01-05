package com.example.nightingaleplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.nightingaleplayer.player.service.NpAudioService
import com.example.nightingaleplayer.ui.audio.AudioViewModel
import com.example.nightingaleplayer.ui.audio.HomeScreen
import com.example.nightingaleplayer.ui.audio.UIEvents
import com.example.nightingaleplayer.ui.theme.NightingalePlayerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.permissions.rememberPermissionState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false
    @androidx.annotation.OptIn(UnstableApi::class) @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NightingalePlayerTheme {
                val audioPermissionState = rememberPermissionState(
                    permission = android.Manifest.permission.READ_MEDIA_AUDIO // note: needs the android at the beginning
                )
                val intent = Intent(this, NpAudioService::class.java)
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(key1 = lifecycleOwner) {
                    val observer = LifecycleEventObserver {_, event ->
                        if (event == Lifecycle.Event.ON_START) {
                            Log.d("np", "ON_START called")
                            audioPermissionState.launchPermissionRequest()
                        } else if (event == Lifecycle.Event.ON_DESTROY) {
                            Log.d("np", "ON_DESTROY called (Main Activity)")
                            stopService(intent)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    startAudioService(intent)
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
                            viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                        },
                        onNext = {
                            viewModel.onUiEvents(UIEvents.SeekToNext)
                        },
                        onPrevious = {
                            viewModel.onUiEvents(UIEvents.SeekToPrevious)
                        }
                    )
                }
            }
        }
    }

    private fun startAudioService(intent: Intent) {
        if (!isServiceRunning) {

            startForegroundService(intent)
        }
        isServiceRunning = true
    }
}
