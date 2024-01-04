package com.example.nightingaleplayer.player.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.nightingaleplayer.player.notification.NpNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NpAudioService: MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: NpNotificationManager

    @OptIn(UnstableApi::class) override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("night", "NpAudioService onStart called")
        notificationManager.startNotificationService(this, mediaSession)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    // onDestroy is callback before activity is shut down
    @OptIn(UnstableApi::class) override fun onDestroy() {
        // learn more on Player within MediaSession: https://developer.android.com/reference/androidx/media3/common/Player
        // will be using ExoPlayer, as that's the default implementation and should be fine with current goals
        // https://developer.android.com/media/implement/playback-app states what to do on onDestroy call
        Log.d("night", "NpAudioService onDestroy called")
        super.onDestroy()
        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }
}