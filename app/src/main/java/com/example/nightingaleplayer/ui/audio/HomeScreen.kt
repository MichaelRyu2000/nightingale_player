package com.example.nightingaleplayer.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.nightingaleplayer.data.local.model.Audio
import com.example.nightingaleplayer.ui.theme.NightingalePlayerTheme
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen (
    progress: Float,
    onProgress: (Float) -> Unit,
    currentAudio: Audio,
    isAudioPlaying: Boolean,
    audioList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomAppPlayer(
                progress = progress,
                onProgress = onProgress,
                audio = currentAudio,
                isAudioPlaying = isAudioPlaying,
                onStart = onStart,
                onNext = onNext
            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audioList) {index, audio ->
                AudioItem(
                    audio = audio,
                    onItemClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
fun AudioItem(
    audio: Audio,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onItemClick()
            }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
            }
            Text(
                text = timestampToDuration(audio.duration.toLong())
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

private fun timestampToDuration(position: Long): String {
    val totalSeconds = floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun BottomAppPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio: Audio,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar(
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    ArtistInfo(
                        audio = audio,
                        modifier = Modifier.weight(1f)
                    )
                    MediaPlayer(
                        isAudioPlaying,
                        onStart,
                        onNext
                    )

                    Slider(
                        value = progress,
                        onValueChange = { onProgress(it) },
                        valueRange = 0f..100f
                    )
                }
            }
        }
    )
}

@Composable
fun MediaPlayer(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        PlayerIconItem(icon = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow)  {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = null,
            modifier = Modifier.clickable {
                onNext()
            },

        )
    }
}

@Composable
fun ArtistInfo(
    modifier: Modifier = Modifier,
    audio: Audio,
) {
    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = Icons.Default.MusicNote,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {}
        Spacer(modifier = Modifier.size(4.dp))
        Column {
            Text(
                text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = audio.artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PlayerIconItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color:Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        contentColor = color,
        color = backgroundColor,
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    NightingalePlayerTheme {
        HomeScreen(
            progress = 50f,
            onProgress = {},
            isAudioPlaying = true,
            audioList = listOf(
                Audio("".toUri(), "display 1", 0L, "artist 1", "title 1", "", 0),
                Audio("".toUri(), "display 2", 0L, "artist 2", "title 2", "", 0)
            ),
            currentAudio = Audio("".toUri(), "display 1", 0L, "artist 1", "title 1", "", 0),
            onStart = {},
            onNext = {},
            onItemClick = {}
        )
    }
}


