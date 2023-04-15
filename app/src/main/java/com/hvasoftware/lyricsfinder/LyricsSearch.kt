package com.hvasoftware.lyricsfinder


import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup
import java.net.URLEncoder

class LyricsSearch(private val context: Context) {

    fun search(artist: String, track: String, listener: LyricsSearchListener) {
        val query = "$artist $track lyrics"
        val url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val doc = Jsoup.parse(response)
               // val lyrics = doc.selectFirst("div[data-attrid='kc:/music/track:lyrics']")?.text()
                val lyrics = parseLyrics(response)

                Log.d(TAG, "lyrics: $lyrics")

                if (lyrics != null) {
                    if (lyrics.isNotEmpty()) {
                        lyrics.let { listener.onLyricsFound(it) }
                    } else {
                        listener.onLyricsError("Lyrics not found")
                    }
                } else {
                    listener.onLyricsError("Lyrics not found")
                }
            },
            { error ->
                Log.e(TAG, "error: ${error.message}")
                listener.onLyricsError(error.message ?: "Unknown error")
            })

        Volley.newRequestQueue(context).add(request)
    }


    fun search(track: String, listener: LyricsSearchListener) {
        val query = "$track lyrics"
        val url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d(TAG, "response: $response")

                val doc = Jsoup.parse(response)
                val lyrics = doc.selectFirst("div[data-attrid='kc:/music/track:lyrics']")?.text()

                if (lyrics != null) {
                    if (lyrics.isNotEmpty()) {
                        lyrics.let { listener.onLyricsFound(it) }
                    } else {
                        listener.onLyricsError("Lyrics not found")
                    }
                }
            },
            { error ->
                Log.e(TAG, "error: ${error.message}")
                listener.onLyricsError(error.message ?: "Unknown error")
            })

        Volley.newRequestQueue(context).add(request)
    }


    private fun parseLyrics(response: String): String? {
        val document = Jsoup.parse(response)
        val lyricsDiv = document.selectFirst("span[jsname='YS01Ge']") ?: return null
        return lyricsDiv.text()
    }



    interface LyricsSearchListener {
        fun onLyricsFound(lyrics: String)
        fun onLyricsError(errorMessage: String)
    }

    companion object {
        private const val TAG = "LyricsSearch"
    }
}
