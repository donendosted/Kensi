package com.dos.kensi

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

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
    var currentsong by remember { mutableStateOf("Song to be played") }
    var songDuration by remember { mutableFloatStateOf(0f) }
    var songCursor by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var openQueueScreenDialog by remember { mutableStateOf(false) }
    var optionDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SongData?>(null) }
    var queueSongs = remember { mutableStateListOf<SongData>() }
    var repeatTint by remember { mutableStateOf(Color.Green) }
    var tog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var optionNewPlaylist by remember { mutableStateOf(false) }
    var newName: String by remember { mutableStateOf("") }
    var renameDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var playlistSongs = remember { mutableListOf<Int>() }
    var playlistId by remember { mutableIntStateOf(0) }
    var optionPlaylist by remember { mutableStateOf(false) }
    val playlists = remember { Playlists() }
    var allPlaylists by remember { mutableStateOf(emptyList<PlaylistData>()) }
    var optionaddtoplaylist by remember { mutableStateOf(false) }
    var isNotPlaylist by remember { mutableStateOf(true) }
    var isRadioOn by remember { mutableStateOf(false) }
    var radioTint by remember { mutableStateOf(Color.LightGray) }
    var regards by remember { mutableStateOf(false) }


    songList = listSongs(context)
    allPlaylists = playlists.loadPlaylists(context)

    Log.d(TAG, "SongsFrag recalled")

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

    LaunchedEffect(Unit) {
        queueSongs.clear()
        queueSongs.addAll(songs.getQueue(songList))
    }

    LaunchedEffect(Unit) {
        snapshotFlow { queueSongs.firstOrNull()?.name ?: "..." }
            .collect { currentsong = it }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { queueSongs.toList() }
            .collect { updatedQueue ->
                queueSongs.clear()
                queueSongs.addAll(updatedQueue)
            }
    }

    LaunchedEffect(tog) {
        if (tog) {
            repeatTint = Color.LightGray
        } else {
            repeatTint = Color.DarkGray
        }
    }

    LaunchedEffect(Unit) {
        if(isRadioOn){
            radioTint = Color.Cyan
            songs.addRandomSongToQueue(songList)
            delay(6000L)
        }
        else {
            radioTint = Color.Gray
        }
    }

    //PLAY SCREEN DIALOG
    if (openPlayscreenDialog) {
        Dialog(onDismissRequest = { openPlayscreenDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Column {

                    //SONG TITLE AND PLAYSTATE BUTTON
                    Row(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                    ) {
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
                                if (songs.isPlaying()) {
                                    songs.pause()
                                } else if (songs.isQueueEmpty()) {
                                } else if (songs.isNotPrepared() && !songs.isQueueEmpty()) {
                                    currentsong = songs.playFromQueue(queueSongs)
                                } else {
                                    songs.resume()
                                }
                            },
                            //shape = CircleShape,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(47.dp),
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
                        IconButton(onClick = {
                            if (progress > 0.05) {
                                songs.seekTo(0)
                            } else if (songs.existsHistory()) {
                                currentsong = songs.playFromHistory(songList)
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.prev),
                                contentDescription = "Previous",
                                modifier = Modifier.size(15.dp)
                            )
                        }


                        //SLIDER
                        Slider(
                            value = progress,
                            onValueChange = { newValue ->
                                progress = newValue
                            },
                            onValueChangeFinished = {
                                songCursor = progress * songDuration
                                songs.seekTo((songCursor * 1000).toInt())
                            },
                            modifier = Modifier
                                .height(15.dp)
                                .width(175.dp)
                                .padding(vertical = 25.dp),
                        )


                        IconButton(onClick = {
                            if (queueSongs.isEmpty()) {
                                if (songs.existsHistory()) {
                                    currentsong = songs.playFromHistory(songList)
                                }
                            }
                            else if(queueSongs.size<2) {
                                //TODO something... it feels blank else
                            }
                            else {
                                queueSongs.removeAt(0)
                                currentsong = songs.playNext(songList)
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.next),
                                contentDescription = "Next",
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(onClick = {
                            if (songs.isPlaying()) {
                                tog = !tog
                                currentsong = songs.playRepeat(songList, tog)
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.repeatone),
                                tint = repeatTint,
                                contentDescription = "Repeat",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    //QUEUE SCREEN DIALOG
    if (openQueueScreenDialog) {
        Dialog(onDismissRequest = { openQueueScreenDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Queue",
                                    fontSize = 25.sp
                                )
                            },
                            actions = {

                                //CLEAR QUEUE
                                IconButton(onClick = {
                                    songs.stop()
                                    queueSongs.clear()
                                }) {
                                    Icon(
                                        painterResource(R.drawable.recyclebin),
                                        contentDescription = "clear queue",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                //PLAY QUEUE
                                IconButton(onClick = {
                                    if (queueSongs.isNotEmpty()) {
                                        currentsong = songs.playFromQueue(songList)
                                        queueSongs.clear()
                                        queueSongs.addAll(songs.getQueue(songList))

                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.play),
                                        contentDescription = "Play from queue",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                )
                { values ->
                    LazyColumn(
                        Modifier.padding(values)
                    ) {
                        queueSongs.clear()
                        queueSongs.addAll(songs.getQueue(songList))

                        items(queueSongs.size) { index ->
                            ElevatedCard(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = queueSongs[index].name,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //OPTION DIALOG
    if (optionDialog) {
        Dialog(onDismissRequest = { optionDialog = false }) {
            OutlinedCard(
                modifier = Modifier
                    .height(230.dp)
            ) {
                Column {
                    Spacer(Modifier.size(5.dp))
                    Card(
                        Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    songs.play(songList, selected!!.songID)
                                    optionDialog = false
                                }
                            )
                    ) {
                        Text(
                            selected!!.name,
                            fontSize = 16.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
                        )
                    }
                    ElevatedCard(
                        Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    songs.addToQueue(selected!!.songID)
                                    optionDialog = false
                                }
                            )
                    ) {
                        Text(
                            "Add to queue",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
                        )
                    }
                    ElevatedCard(
                        Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    renameDialog = true
                                    optionDialog = false
                                }
                            )
                    ) {
                        Text(
                            "Rename",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
                        )
                    }
                    ElevatedCard(
                        Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                            .clickable(
                                onClick = {/*TODO delete*/ }
                            )
                    ) {
                        Text(
                            "Delete",
                            fontSize = 16.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
        }
    }

    //NEW PLAYLIST DIALOG
    if (optionNewPlaylist) {
        Dialog(onDismissRequest = { optionNewPlaylist = false }) {
            Card {
                Column {
                    Text("Playlist Name:", fontSize = 18.sp, modifier = Modifier.padding(10.dp))
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("New Playlist Name ${allPlaylists.size + 1}") }
                    )
                    Button(onClick = {
                        val newPlaylist = PlaylistData(
                            playlistId = allPlaylists.size + 1,
                            name = newPlaylistName,
                            songs = emptyList()
                        )
                        playlists.savePlaylist(context, newPlaylist)
                        optionNewPlaylist = false
                        showBottomSheet = true
                        Toast.makeText(
                            context,
                            "$newPlaylistName created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text("Done", fontSize = 16.sp, modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }

    //PLAYLIST DIALOG
    if (optionPlaylist) {
        Dialog(onDismissRequest = { optionPlaylist = false }) {
            Card {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Playlist Name") }
                        )
                    }
                ) { values ->
                    LazyColumn(modifier = Modifier.padding(values)) {
                        items(10) { index ->
                            Text("Playlist Name")
                        }
                    }
                }
            }
        }
    }

    //RENAME DIALOG
    if (renameDialog) {
        Dialog(onDismissRequest = { renameDialog = false }) {
            Card {
                Column {
                    Text(
                        "Sorry rename might not yet work",
                        Modifier.padding(10.dp),
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Text("NEW NAME:", Modifier.padding(10.dp), fontSize = 18.sp)
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text(selected!!.name) }
                    )
                    ElevatedButton(onClick = {
                        val contentResolver = context.contentResolver
                        val newFileName = "$newName.${selected!!.extension}"

                        val fileUri =
                            selected?.uri ?: return@ElevatedButton // Ensure Uri is not null

                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                        }

                        try {
                            val updated = contentResolver.update(fileUri, values, null, null)
                            if (updated > 0) {
                                Toast.makeText(
                                    context,
                                    "Renamed successfully to $newFileName",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Renaming Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }

                        renameDialog = false
                    }, Modifier.padding(10.dp)) {
                        Text("Rename", Modifier.padding(5.dp), fontSize = 15.sp)
                    }


                }
            }
        }
    }

    //ADD TO PLAYLIST
    if (optionaddtoplaylist) {
        Dialog(onDismissRequest = { optionaddtoplaylist = false }) {
            Card {

            }
        }
    }

    //REGARDS
    if (regards){
        Dialog(onDismissRequest = { regards = false }) {
            Card {
                Column {
                    Text(
                        "This app was an useless attempt to... idk why but just wanted to make my own app to listen to the music i pirate in my own device. Probably I did it... probably not though because the app is incomplete... at many levels.",
                        Modifier.padding(15.dp)
                    )
                    Text("Well, Thanks.", Modifier.padding(10.dp))
                    Button(onClick = {regards = false}, Modifier.padding(15.dp)) { Text("Close", Modifier.padding(5.dp))}
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
                        fontSize = 30.sp,
                        modifier = Modifier.combinedClickable(
                            onLongClick = {
                                regards = true
                            },
                            onClick = {
                                Toast.makeText(context, "Made with Love by Dos ;-)", Toast.LENGTH_SHORT).show()
                            }
                        )
                    )
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),

                actions = {
                    //RADIO PLAY
                    IconButton(onClick = {
                        songs.addRandomSongToQueue(songList)
                        isRadioOn = !isRadioOn
                        currentsong = songs.playRadio(songList)
                    }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.radio),
                            contentDescription = "Radio",
                            modifier = Modifier.size(20.dp),
                            tint = radioTint
                        )
                    }

                    IconButton(onClick = { openQueueScreenDialog = true }) {
                        Icon(
                            painterResource(R.drawable.queue),
                            contentDescription = "Queue",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        showBottomSheet = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.playlist),
                            contentDescription = "Playlist",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },

        //PLAY SCREEN
        floatingActionButton = {
            FloatingActionButton(onClick = { openPlayscreenDialog = true }, shape = CircleShape) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Song Screen",
                    modifier = Modifier.size(17.dp)
                )
            }
        },

        //SEARCH PANEL
        bottomBar = {
            BottomAppBar(
                //TODO search
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
            onRefresh = {
                songList = listSongs(context)
                Toast.makeText(context, "Songs Refreshed", Toast.LENGTH_SHORT).show()
            }
        ) {
            Column {
                Spacer(Modifier.size(10.dp))
                if (isNotPlaylist) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(values)
                            .fillMaxWidth()
                    ) {
                        if (songList.isEmpty()) {
                            item {
                                Text(
                                    text = "No Songs Found",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else {

                            //MAIN SCREEN
                            items(songList.size) { index ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp, horizontal = 10.dp)
                                        .height(45.dp)
                                        .combinedClickable(
                                            onClick = {
                                                currentsong =
                                                    songs.play(songList, songList[index].songID)
                                                Toast.makeText(
                                                    context,
                                                    "Playing $currentsong",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onLongClick = {
                                                optionDialog = true
                                                selected = songList[index]
                                                Toast.makeText(
                                                    context,
                                                    "${selected!!.name} selected",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            })
                                ) {

                                    //THE OPENING SONG LISTED
                                    Text(
                                        text = songList[index].name,
                                        fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TODO  if playlist specified
                }
            }

            //PLAYLIST LISTED BOTTOM SHEET
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    Column(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                        ElevatedCard(modifier = Modifier.padding(10.dp), onClick = {
                            optionNewPlaylist = true
                            showBottomSheet = false
                        }) {
                            Text("Create new playlist")
                        }
                        LazyColumn {
                            if (allPlaylists.isEmpty()) {
                                item {
                                    Text("No other playlists", fontSize = 16.sp)
                                }
                            } else {
                                items(allPlaylists.size) { index ->
                                    Card(Modifier.padding(10.dp)) {
                                        Text(
                                            allPlaylists[index].name,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
