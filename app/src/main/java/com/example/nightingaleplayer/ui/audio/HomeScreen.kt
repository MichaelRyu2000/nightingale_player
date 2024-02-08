package com.example.nightingaleplayer.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.nightingaleplayer.data.local.model.Audio
import com.example.nightingaleplayer.ui.theme.NightingalePlayerTheme
import kotlin.math.floor


// consider https://patilshreyas.github.io/compose-report-to-html/
// and https://medium.com/@patilshreyas/solving-the-mystery-of-recompositions-in-composes-lazylist-514d187079b9

@androidx.annotation.OptIn(UnstableApi::class) @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen (
    reload: () -> Unit,
    progress: Float,
    onProgress: (Float) -> Unit,
    currentAudio: Audio,
    isAudioPlaying: Boolean,
    audioList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    if (audioList.isEmpty()) {
        RefreshItem (
            onReload = reload
        )
    } else {
        Scaffold(
            bottomBar = {
                BottomAppPlayer(
                    progress = progress,
                    onProgress = onProgress,
                    audio = currentAudio,
                    isAudioPlaying = isAudioPlaying,
                    onStart = onStart,
                    onNext = onNext,
                    onPrevious = onPrevious
                )
            }
        ) {
            LazyColumn(
                contentPadding = it
            ) {
                itemsIndexed(audioList) {index, audio ->
                    AudioItem(
                        audio = audio,
                        currentAudio = currentAudio,
                        index = index,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
fun RefreshItem(
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier
                .size(128.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onReload()
                }
        )
        Text(
            text = "Refresh Song List",
            fontSize = 32.sp
        )
    }
}

// recomposition occurs here A LOT, when audio is playing
@androidx.annotation.OptIn(UnstableApi::class) @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioItem(
    audio: Audio,
    currentAudio: Audio,
    index: Int,
    onItemClick: (Int) -> Unit,
) {
    Card(
        onClick = {
            onItemClick(index)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                    modifier = if (audio.uri == currentAudio.uri) Modifier.basicMarquee(animationMode = MarqueeAnimationMode.Immediately) // note: if a word does not fit within the box, the whole word and everything after is cut off now
                    else Modifier
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
            Spacer(modifier = Modifier.size(4.dp))
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

// Overload function looks for float values from 0 to 100 inclusive (percentages)
// Meant to be used to get current timestamp of song
private fun timestampToDuration(position: Float, duration: Long): String {
    val totalSeconds = floor(duration / 1E3).toInt()
    val currentSeconds = floor(totalSeconds * position / 100.0).toInt()
    val minutes = currentSeconds / 60
    val remainingSeconds = currentSeconds - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun SliderLabel(
    label: String,
    minWidth: Dp,
    modifier: Modifier = Modifier,
    offset: Dp
) {
    Text(
        text = label,
        textAlign = TextAlign.Center,
        color = Color.White,
        overflow = TextOverflow.Visible,
        maxLines = 1,
        modifier = Modifier
            .offset(x = offset)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp)
            .defaultMinSize(minWidth = minWidth)

    )
}

// https://www.devbitsandbytes.com/jetpack-compose-configuring-slider-with-label/

/* TODO
*  Issue where on the first song when starting the app, since audio duration is not found, moving
*  slider won't update the current timestamp label
*
*
* TODO
*  CHECK IF ITS CALLING THIS LITERALLY EVERY TICK!!!!!!!!!!!!!!!!!!!!!!!
* */
private fun getSliderOffset(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    boxWidth: Dp,
    labelWidth: Dp,
): Dp {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

    return (boxWidth - labelWidth) * positionFraction
}

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

@androidx.annotation.OptIn(UnstableApi::class) @Composable
fun CustomSliderWithLabel(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    labelMinWidth: Dp = 24.dp,
    duration: Long,
) {
    Column(modifier = Modifier.fillMaxWidth(0.9F)) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val offset = getSliderOffset(
                value = value,
                valueRange = valueRange,
                boxWidth = maxWidth,
                labelWidth = labelMinWidth + 8.dp
            )
            SliderLabel(
                label = timestampToDuration(value, duration),
                minWidth = labelMinWidth,
                offset = offset
            )
        }
        Spacer(modifier = Modifier.padding(2.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        )
    }
}

@Composable
fun BottomAppPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio: Audio,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
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
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    MediaPlayer(
                        isAudioPlaying,
                        onStart,
                        onNext,
                        onPrevious
                    )
                    CustomSliderWithLabel(
                        value = progress,
                        onValueChange = { onProgress(it) },
                        valueRange = 0f..100f,
                        duration = audio.duration.toLong(),
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
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(42.dp)
            .padding(2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = null,
            modifier = Modifier.clickable {
                onPrevious()
            }
        )
        Spacer(modifier = Modifier.size(8.dp))
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
                Audio("", "display 1", 0L, "artist 1", "title 1", "", 0),
                Audio("", "display 2", 0L, "artist 2", "title 2", "", 0)
            ),
            currentAudio = Audio("", "display 1", 0L, "artist 1", "title 1", "", 0),
            onStart = {},
            onNext = {},
            onItemClick = {},
            onPrevious = {},
            reload = {}
        )
    }
}


