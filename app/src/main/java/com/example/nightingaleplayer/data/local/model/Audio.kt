package com.example.nightingaleplayer.data.local.model

import android.net.Uri
import java.io.Serializable


data class Audio(
    val uri: String,
    val displayName: String,
    val id: Long,
    val artist: String,
    val title: String,
    val data: String,
    val duration: Int,
): Serializable