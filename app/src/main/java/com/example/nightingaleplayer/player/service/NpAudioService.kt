package com.example.nightingaleplayer.player.service

import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NpAudioService: MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // onDestroy is callback before activity is shut down
    override fun onDestroy() {
        // learn more on Player within MediaSession: https://developer.android.com/reference/androidx/media3/common/Player
        // will be using ExoPlayer, as that's the default implementation and should be fine with current goals
        // https://developer.android.com/media/implement/playback-app states what to do on onDestroy call
        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
        super.onDestroy()
    }
}