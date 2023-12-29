package com.example.nightingaleplayer.player.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NpAudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer
): Player.Listener {
    private val _audioState: MutableStateFlow<NpAudioState> = MutableStateFlow(NpAudioState.Initial)
    val audioState: StateFlow<NpAudioState> = _audioState.asStateFlow()

    private var job: Job? = null

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun addMediaItem(mediaItem: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItem)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudio: Int = -1,
        seekPosition: Long = 0
    ) {
        when(playerEvent){
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvent.Stop -> stopProgressUpdate()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            PlayerEvent.SelectedAudioChange -> {
                when(selectedAudio) {
                    exoPlayer.currentMediaItemIndex -> {
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudio)
                        _audioState.value = NpAudioState.Playing(isPlaying = true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value = NpAudioState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _audioState.value = NpAudioState.Ready(exoPlayer.duration)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = NpAudioState.Playing(isPlaying = isPlaying)
        _audioState.value = NpAudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying) {
            // TODO: Below is BAD PRACTICE, change asap
            GlobalScope.launch(Dispatchers.IO) {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
        }
    }

    private suspend fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = NpAudioState.Playing(
                isPlaying = true
            )
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while(true) {
            delay(500)
            _audioState.value = NpAudioState.Progress(exoPlayer.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = NpAudioState.Playing(isPlaying = false)
    }
}

sealed class PlayerEvent{
    object PlayPause: PlayerEvent()
    object SelectedAudioChange: PlayerEvent()
    object Backward: PlayerEvent()
    object SeekToNext: PlayerEvent()
    object Forward: PlayerEvent()
    object SeekTo: PlayerEvent()
    object Stop: PlayerEvent()
    data class UpdateProgress(val newProgress: Float): PlayerEvent()
}

// https://kotlinlang.org/docs/sealed-classes.html
sealed class NpAudioState {
    object Initial: NpAudioState()
    data class Ready(val duration: Long): NpAudioState()
    data class Progress(val duration: Long): NpAudioState()
    data class Buffering(val duration: Long): NpAudioState()
    data class Playing(val isPlaying: Boolean): NpAudioState()
    data class CurrentPlaying(val mediaItemIndex: Int): NpAudioState()
}