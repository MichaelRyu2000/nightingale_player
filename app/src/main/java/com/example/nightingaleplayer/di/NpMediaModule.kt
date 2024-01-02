package com.example.nightingaleplayer.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.nightingaleplayer.player.notification.NpNotificationManager
import com.example.nightingaleplayer.player.service.NpAudioServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// https://dagger.dev/hilt/modules.html
@Module
@InstallIn(SingletonComponent::class)
class NpMediaModule {
    // https://developer.android.com/reference/android/media/AudioAttributes
    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    @UnstableApi
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    @Provides
    @Singleton
    fun providesModule(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun providesNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): NpNotificationManager = NpNotificationManager(context, player)

    @Provides
    @Singleton
    fun providesServicesHandler(player: ExoPlayer): NpAudioServiceHandler = NpAudioServiceHandler(player)
}