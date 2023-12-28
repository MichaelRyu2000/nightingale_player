package com.example.nightingaleplayer.data.local.model

import android.net.Uri

data class Audio (
    val uri: Uri,
    val displayName: String,
    val id: Long,
    val artist: String,
    val title: String,
    val data: String,
    val duration: Int,
)