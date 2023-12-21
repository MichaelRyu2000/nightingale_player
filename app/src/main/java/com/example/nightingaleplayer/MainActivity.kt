package com.example.nightingaleplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nightingaleplayer.ui.theme.NightingalePlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NightingalePlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PlayerScreen()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { AudioTopAppBar() },
        content = { AudioPlayer(modifier = Modifier.fillMaxSize()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@Composable
fun AudioPlayer(modifier: Modifier = Modifier) {
    val musContext = LocalContext.current

    val player = MediaPlayer.create(musContext, R.raw.audio)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(16.dp)
    ) {
        Row {
            IconButton(
                onClick = { player.start() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_circle_outline_24),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
            IconButton(
                onClick = { player.pause() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_pause_circle_outline_24),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
        }
    }
}
