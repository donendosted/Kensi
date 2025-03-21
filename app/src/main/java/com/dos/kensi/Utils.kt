package com.dos.kensi

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File


fun listSongs(context: Context): MutableList<SongData> {
    val songListTemp = mutableListOf<SongData>()
    val folderMap = mutableMapOf<String, MutableList<SongData>>()

    val audioscan = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA
    )

    val cursor = context.contentResolver.query(audioscan, projection, null, null, null)

    cursor?.use {
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (it.moveToNext()) {
            val fullname = it.getString(nameColumn)
            val name = fullname.substringBeforeLast(".")
            val extension = File(fullname).extension
            val filePath = it.getString(dataColumn)
            val songid = it.getInt(idColumn)
            val contentUri = ContentUris.withAppendedId(audioscan, it.getLong(idColumn))

            val file = File(filePath)
            val size = if (file.exists()) file.length() else 0L

            if (size > 40000L) {
                val folderPath = filePath.substringBeforeLast("/")

                val song = SongData(songid, name, size, contentUri, extension)
                folderMap.getOrPut(folderPath) { mutableListOf() }.add(song)
            }
        }
    }

    folderMap.values.forEach { songsInFolder ->
        songListTemp.addAll(songsInFolder.sortedBy { it.name })
    }


    return songListTemp
}


fun getSearchList(songList: List<SongData>): List<String> {
    return songList.map { "${it.name}: ${it.songID}" }
}



@Composable
fun PlaylistChip(
    text: String,
    onDismiss: () -> Unit,
) {
    var optionEnable by remember { mutableStateOf(true) }
    if (!optionEnable) return

    InputChip(
        onClick = {
            onDismiss()
            optionEnable = !optionEnable
        },
        label = { Text(text) },
        selected = optionEnable,
        trailingIcon = {
            Icon(
                painter = painterResource( R.drawable.cross),
                contentDescription = "Localized description",
                Modifier.size(InputChipDefaults.AvatarSize)
            )
        },
    )
}