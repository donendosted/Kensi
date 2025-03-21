package com.dos.kensi

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Playlists {
    fun loadPlaylists(context: Context): List<PlaylistData> {
        val file = File(context.filesDir, "playlist.json")
        if (!file.exists()) {
            file.writeText("[]")
            return emptyList()
        }

        val content = file.readText()
        if (content.isBlank()) return emptyList()

        val jsonArray = JSONArray(content)
        val playlists = mutableListOf<PlaylistData>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val playlistId = obj.getInt("id")
            val name = obj.getString("name")
            val songsArray = obj.getJSONArray("songs")
            val songs = List(songsArray.length()) { songsArray.getInt(it) }
            playlists.add(PlaylistData(playlistId, name, songs))
        }
        return playlists
    }

    fun savePlaylist(context: Context, newPlaylist: PlaylistData) {
        val file = File(context.filesDir, "playlist.json")
        val playlists = loadPlaylists(context).toMutableList()
        playlists.add(newPlaylist)

        val jsonArray = JSONArray()
        playlists.forEach {
            val obj = JSONObject()
            obj.put("id", it.playlistId)
            obj.put("name", it.name)
            obj.put("songs", JSONArray(it.songs))
            jsonArray.put(obj)
        }

        try {
            FileWriter(file).use { it.write(jsonArray.toString()) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}