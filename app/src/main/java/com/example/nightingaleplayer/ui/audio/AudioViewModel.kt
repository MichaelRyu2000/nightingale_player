package com.example.nightingaleplayer.ui.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.core.util.TimeUtils.formatDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.nightingaleplayer.data.local.model.Audio
import com.example.nightingaleplayer.data.repository.AudioRepository
import com.example.nightingaleplayer.player.service.NpAudioServiceHandler
import com.example.nightingaleplayer.player.service.NpAudioState
import com.example.nightingaleplayer.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val initialAudio = Audio(
    "", "", 0, "", "", "", 0
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: NpAudioServiceHandler,
    private val audioRepository: AudioRepository,
    savedStateHandle: SavedStateHandle // key-value map to write and receive objects to and from saved state. values persist after process is killed by system
): ViewModel() {
    // https://developer.android.com/topic/libraries/architecture/saving-states#ui-dismissal-system
    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate
    var duration by savedStateHandle.saveable { mutableStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var currentAudio by savedStateHandle.saveable { mutableStateOf(initialAudio) }
    // https://stackoverflow.com/questions/75019326/what-is-the-difference-between-mutablestateof-and-mutablestatelistof
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<Audio>()) }

    // encapsulation
    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        loadAudioData()
    }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when(mediaState) {
                    NpAudioState.Initial -> _uiState.value = UIState.Initial
                    is NpAudioState.Buffering -> calculateProgress(mediaState.progress)
                    is NpAudioState.Playing -> isPlaying = mediaState.isPlaying
                    is NpAudioState.Progress -> calculateProgress(mediaState.progress)
                    is NpAudioState.CurrentPlaying -> {
                        currentAudio = audioList[mediaState.mediaItemIndex]
                    }
                    is NpAudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    fun loadAudioData() {
        viewModelScope.launch {
            val audio = audioRepository.getAudioData()
            audioList = audio
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        audioList.map {audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setAlbumTitle(audio.title)
                        .setSubtitle(audio.displayName)
                        .build()
                )
                .build()
        }.also {
            audioServiceHandler.addMediaItem(it)
        }
    }

    private fun calculateProgress(currentProgress: Long) {
        progress = if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
        else 0f
        progressString = formatDuration(currentProgress)
    }

    fun onUiEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when(uiEvents) {
            UIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            UIEvents.SeekToPrevious -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToPrevious)
            is UIEvents.PlayPause -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.PlayPause
                )
            }
            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }
            is UIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudio = uiEvents.index
                )
            }
            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    ),
                )
                progress = uiEvents.newProgress
            }
        }
    }

    fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (duration / 1000L) % 60L
        return String.format("%02d:%02d", minute, seconds)
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }
}



sealed class UIEvents{
    object PlayPause: UIEvents()
    data class SelectedAudioChange(val index: Int): UIEvents()
    data class SeekTo(val position: Float): UIEvents()
    object SeekToNext: UIEvents()
    object SeekToPrevious: UIEvents()
    object Backward: UIEvents()
    object Forward: UIEvents()
    data class UpdateProgress(val newProgress: Float): UIEvents()
}

sealed class UIState{
    object Initial:UIState()
    object Ready:UIState()
}