package com.hvasoftware.lyricsfinder

import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

// CLIENT ID: oYivyIBpbWyJuKQOu4-homreDdEjjaaRZtaTzk8s1H29nDf-nB2TThEKxKeKXc0o
// CLIENT SECRET: Sy_yyV5ZEKsj-t1haYnacaezWTkKtZfUTcnyfNZVcXXCbpSb0NWQWolOS2HJAMSSjlvpt5UWEVvBy35f9uNrRw
// CLIENT ACCESS TOKEN: 1rqDFMknps9Ta8DaOHQ54uU5C7HsQ8X6RLz9Cem_ojWvAHOD4bsPADcV3_XR0Aba

class SongLyricsFetcher() {

    private val geniusBaseUrl = "https://api.genius.com"
    private val accessToken = "1rqDFMknps9Ta8DaOHQ54uU5C7HsQ8X6RLz9Cem_ojWvAHOD4bsPADcV3_XR0Aba"



    public fun getSongLyrics(songTitle: String, artistName: String): String {
        val songSearchUrl = "$geniusBaseUrl/search?q=${songTitle} $artistName"
        val songSearchRequest = Request.Builder()
            .url(songSearchUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()

        val httpClient = OkHttpClient()

        return try {
            val songSearchResponse = httpClient.newCall(songSearchRequest).execute()
            val songSearchResponseBody = songSearchResponse.body?.string()
            val songSearchResults =
                songSearchResponseBody?.let { JSONObject(it).getJSONObject("response").getJSONArray("hits") }

            if (songSearchResults!!.length() > 0) {
                val songResult = songSearchResults.getJSONObject(0).getJSONObject("result")
                val songUrl = songResult.getString("url")
                val songId = songResult.getInt("id")
                val songLyricsUrl = "$geniusBaseUrl/songs/$songId"
                val songLyricsRequest = Request.Builder()
                    .url(songLyricsUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                val songLyricsResponse = httpClient.newCall(songLyricsRequest).execute()
                val songLyricsResponseBody = songLyricsResponse.body?.string()

                ""

            } else {
                ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}
