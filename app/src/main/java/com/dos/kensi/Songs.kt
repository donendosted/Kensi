package com.dos.kensi

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

class Songs(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongId: Int? = null
    private val queue = mutableListOf<Int>()
    private val history = mutableListOf<Int>()

    fun play(songList: List<SongData>, songId: Int): String {
        stop()
        queue.clear()
        queue.add(songId)
        return playFromQueue(songList)
    }

    fun playFromQueue(songList: List<SongData>): String {
        if (queue.isEmpty()) return ""

        val songId = queue.first()
        if (songId==0) return ""

        history.add(songId)

        val song = songList.find { it.songID == songId } ?: return ""

        val songName = song.name // Get song name before playback starts
        val uri: Uri = song.uri ?: return ""

        mediaPlayer?.release()

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
            playFromQueue(songList)
        }

        return songName // Return song name before playback starts
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

    fun getSongDuration(): Int = mediaPlayer?.duration ?: 0

    fun currentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getQueue(songList: List<SongData>): List<SongData> {
        return queue.mapNotNull { id -> songList.find { it.songID == id } }
    }

    fun playNext(songList: List<SongData>): String {
        if (queue.isEmpty()) {
            return playFromHistory(songList)
        }
        else {
            queue.removeAt(0)
            return playFromQueue(songList)
        }
    }

    fun isNotPrepared(): Boolean {
        return !mediaPlayer?.isPlaying!! && currentPosition() == 0
    }

    fun addRandomSongToQueue(playlist: List<SongData>) {
        val randomSong = playlist.random()
        addToQueue(randomSong.songID)
    }

    fun playRadio(playlist: List<SongData>): String {
        if (playlist.isEmpty()) return ""

        queue.clear()
        if (isQueueEmpty()) {
            addRandomSongToQueue(playlist)
            return playFromQueue(playlist)
        }
        mediaPlayer?.setOnCompletionListener {
            playNext(playlist)
            addRandomSongToQueue(playlist)
        }
        return ""
    }

    fun playFromHistory(songList: List<SongData>): String {
        if (history.size > 1) {
            val previousSongID = history[history.size - 2]
            queue.add(0, previousSongID)
            return playFromQueue(songList)
        }
        return ""
    }

    fun existsHistory(): Boolean {
        return history.size>1
    }

    fun playRepeat(songList: List<SongData>, tog: Boolean): String {
        while (tog) {
            queue.add(history[history.size-1])
            return playFromQueue(songList)
        }
        return ""
    }
}