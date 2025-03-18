package com.dos.kensi

import android.net.Uri

data class SongData (
    val songID: Int,
    val name: String,
    val size: Long,
    val uri: Uri
)