package com.dos.kensi

import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dos.kensi.ui.theme.KensiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            KensiTheme {
                SongFrag(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SongFrag(context: Context) {

    //VARIABLES
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchText by remember { mutableStateOf("") }
    var songList by remember { mutableStateOf(emptyList<SongData>()) }
    var isRefreshing: Boolean by remember { mutableStateOf(false) }
    var songs = remember { Songs(context) }
    var openPlayscreenDialog by remember { mutableStateOf(false) }
    var playstate by remember { mutableIntStateOf(R.drawable.play) }
    var currentsong by remember { mutableStateOf("Song to be played")}
    var songDuration by remember { mutableFloatStateOf(0f) }
    var songCursor by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var openQueueScreenDialog by remember { mutableStateOf(false) }
    var queue by remember { mutableStateOf(emptyList<Int>())}

    LaunchedEffect(Unit) {
        while (true) {
            val duration = songs.getSongDuration() / 1000f
            val position = songs.currentPosition() / 1000f

            if (duration > 0) {
                songDuration = duration
                songCursor = position
                progress = songCursor / songDuration
            }

            delay(500)
        }
    }

    //PLAY SCREEN
    if (openPlayscreenDialog) {
        Dialog(onDismissRequest = {openPlayscreenDialog = false}) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ){
                Column {

                    //SONG TITLE AND PLAYSTATE BUTTON
                    Row (
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                    ){
                        Text(
                            text = currentsong,
                            //text = progress.toString(),
                            modifier = Modifier
                                .padding(20.dp)
                                //.align(Alignment.Start)
                                .width(212.dp),
                            fontSize = 26.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        FilledIconButton(
                            onClick = {
                                if (songs.isPlaying()){
                                    songs.pause()
                                } else if (songs.isQueueEmpty()){

                                }
                                else {
                                    songs.resume()
                                }
                            },
                            //shape = CircleShape,
                            modifier = Modifier.padding(10.dp).size(47.dp),
                            ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = if (!songs.isPlaying() && songs.isQueueEmpty())
                                        R.drawable.play
                                    else if (songs.isPlaying() && !songs.isQueueEmpty())
                                        R.drawable.pause
                                    else
                                        R.drawable.play
                                ),
                                contentDescription = "Play Song",
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    //THE PREV, NEXT, SLIDER, AND SHUFFLE/REPEAT
                    Row {
                        IconButton(onClick = {/*TODO: prev*/}) {
                            Icon(painterResource(R.drawable.prev), contentDescription = "Previous",modifier = Modifier.size(15.dp))
                        }


                        //SLIDER
                        Slider(
                            value = progress,
                            onValueChange = { newValue ->
                                songCursor = newValue * songDuration
                                songs.seekTo((songCursor * 1000).toInt())
                            },
                            modifier = Modifier
                                .height(15.dp)
                                .width(175.dp)
                                .padding(vertical = 25.dp),
                        )

                        IconButton(onClick = {/*TODO: next*/}) {
                            Icon(painterResource(R.drawable.next), contentDescription = "Next", modifier = Modifier.size(15.dp))
                        }

                        IconButton(onClick = {/*TODO: repeat*/}) {
                            Icon(painter = painterResource(R.drawable.repeat), contentDescription = "Repeat", modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }
        }
    }

    //QUEUE SCREEN
    if (openQueueScreenDialog){
        Dialog(onDismissRequest = {openPlayscreenDialog=false}) {
            queue = songs.getQueue()
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
            ){ Scaffold (
                topBar = {TopAppBar(
                    title = {
                        Text(
                            text = "Queue",
                            fontSize = 25.sp
                        ) },
                    actions = {
                        IconButton(onClick = {/*TODO Radio*/ }) {
                            Icon(
                                painterResource(R.drawable.recyclebin),
                                contentDescription = "clear queue",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { /*TODO sochenge */ }) {
                            Icon(
                                painter = painterResource(R.drawable.radio),
                                contentDescription = "Radio",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = {openQueueScreenDialog=false}) {
                            Icon(
                                painter = painterResource(R.drawable.cross),
                                contentDescription = "Close menu",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )}
            )
            { values ->
                    LazyColumn (
                        Modifier.padding(values)
                    ){
                        items(queue.size) { index ->
                            Text(
                                text = songList[index].name
                            )
                        }
                    }
                }
            }
        }
    }

    //MAIN SCREEN
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Songs",
                        fontSize = 30.sp
                    )
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),

                actions = {
                    IconButton(onClick = {openQueueScreenDialog = true}) {
                        Icon(
                            painterResource(R.drawable.queue),
                            contentDescription = "Radio",
                            modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { songList = listSongs(context) }) {
                        Icon(
                            painter = painterResource(R.drawable.refr),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },

        //PLAY SCREEN
        floatingActionButton = {
            FloatingActionButton(onClick = {openPlayscreenDialog = true }, shape = CircleShape) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Song Screen",
                    modifier = Modifier.size(17.dp)
                )
            }
        },

        //SEARCH PANEL
        bottomBar = {
            BottomAppBar (
                // TODO kuch to karenge
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                )
            }
        }
    ) { values ->

        //PULL TO REFRESH
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { songList = listSongs(context) }
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(values)
                    .fillMaxWidth()
            ) {
                if (songList.isEmpty()) {
                    item {
                        Text(
                            text = "No Songs Found",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(songList.size) { index ->
                        Card (modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 10.dp)
                            .height(45.dp)
                            .clickable{
                                songs.play(songList, songList[index].songID)
                                currentsong = songList[index].name
                            }) {
                            Text(
                                text = songList[index].name,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    //.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                            //Icon(Icons.Outlined.PlayArrow, contentDescription = "Play", /*modifier = Modifier.align(Alignment.End)*/)
                        }
                    }
                }
            }
        }
    }
}

fun listSongs(context: Context): MutableList<SongData> {
    var songListTemp = mutableListOf<SongData>()
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media._ID
    )

    val cursor = context.contentResolver.query(collection, projection, null, null, null)

    cursor?.use {
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

        while (it.moveToNext()) {
            val name = it.getString(nameColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            )
            songListTemp.add(SongData(songListTemp.size, name, contentUri))
        }
    }

    return songListTemp
}