package com.dos.kensi

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import java.nio.file.Files.size

class Songs (private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongId: Int? = null
    private val queue = mutableListOf<Int>()
    private val sonq = mutableListOf<String>()

    fun play(songList: List<SongData>, id: Int) {
        stop()
        queue.clear()
        queue.add(id)
        playFromQueue(context, songList)
    }

    fun playFromQueue(context: Context, songList: List<SongData>) {
        if (queue.isEmpty()) return

        val id = queue.first()
        if (id !in songList.indices) return

        val uri: Uri = songList[id].uri
        mediaPlayer?.release() // Release old player

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            prepare()
            start()
        }

        currentSongId = id
        mediaPlayer?.setOnCompletionListener { queue.removeAt(0) } // Remove song when finished
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resume() {
        if (queue.isNotEmpty() && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }


    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        queue.clear()
    }

    fun addToQueue(id: Int) {
        queue.add(id)
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun isQueueEmpty(): Boolean {
        return queue.isEmpty()
    }

    fun clearQueue() {
        queue.clear()
    }
    // ✅ Get song duration in milliseconds
    fun getSongDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    // ✅ Get current playback position in milliseconds
    fun currentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getQueue(): MutableList<Int> {
        return queue
    }
}