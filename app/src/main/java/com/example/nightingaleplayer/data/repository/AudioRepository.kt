package com.example.nightingaleplayer.data.repository

import com.example.nightingaleplayer.data.local.LocalContentResolverHelper
import com.example.nightingaleplayer.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val contentResolver: LocalContentResolverHelper
) {
    suspend fun getAudioData(): List<Audio> = withContext(Dispatchers.IO) {
        contentResolver.getAudioData()
    }


}