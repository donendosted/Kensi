package com.dos.kensi

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

/*
class Songs(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongId: Int? = null
    private val queue = mutableListOf<Int>()

    fun play(songList: List<SongData>, songId: Int) {
        stop()
        queue.clear()
        queue.add(songId)
        playFromQueue(context, songList)
    }

    fun playFromQueue(context: Context, songList: List<SongData>) {
        if (queue.isEmpty()) return

        val songId = queue.first()
        val song = songList.find { it.songID == songId } ?: return // Find song by actual ID

        val uri: Uri = song.uri
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

        currentSongId = songId
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

    fun addToQueue(songId: Int) {
        queue.add(songId)
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
}
*/

class Songs(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongId: Int? = null
    private val queue = mutableListOf<Int>()

    fun play(songList: List<SongData>, songId: Int) {
        stop()
        queue.clear()
        queue.add(songId)
        playFromQueue(songList)
    }

    fun playFromQueue(songList: List<SongData>) {
        if (queue.isEmpty()) return

        val songId = queue.first()
        val song = songList.find { it.songID == songId } ?: return // Find song by actual ID

        val uri: Uri = song.uri ?: return
        mediaPlayer?.release() // Release old player

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentSongId = songId
        mediaPlayer?.setOnCompletionListener {
            queue.removeAt(0)
            playFromQueue(songList) // Play next song if queue isn't empty
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
    }

    fun resume() {
        if (queue.isNotEmpty() && mediaPlayer?.isPlaying == false) mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        queue.clear()
    }

    fun addToQueue(songId: Int) {
        queue.add(songId)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun isQueueEmpty(): Boolean = queue.isEmpty()

    fun clearQueue() {
        queue.clear()
    }

    fun getSongDuration(): Int = mediaPlayer?.duration ?: 0

    fun currentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getQueue(songList: List<SongData>): List<SongData> {
        return queue.mapNotNull { id -> songList.find { it.songID == id } }
    }

}
