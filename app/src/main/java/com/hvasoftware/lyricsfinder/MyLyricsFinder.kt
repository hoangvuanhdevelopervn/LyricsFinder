package com.hvasoftware.lyricsfinder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Find Lyrics of any Song using it's title
 */
class MyLyricsFinder(

    /**
     * Minimum Length of lyrics required to mark as valid
     */
    private val minLengthForValidLyrics: Int = 100,

    /**
     * User agent to set on Http Requests
     */
    private val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
) {

    /**
     * Find lyrics of a song with [title]
     */
    suspend fun find(title: String): String? = withContext(Dispatchers.IO) {
        val links = google("$title lyrics")

        for (link in links) {
            val lyrics = findLyrics(link)

            if (lyrics != null && lyrics.length > minLengthForValidLyrics) return@withContext lyrics
        }

        null
    }

    /**
     * Find lyrics of a song with [title] and [artist]
     */
    suspend fun find(title: String, artist: String): String? = withContext(Dispatchers.IO) {


        val links = google("$title $artist lyrics")

        for (link in links) {
            val lyrics = findLyrics(link)

            if (lyrics != null && lyrics.length > minLengthForValidLyrics) return@withContext lyrics
        }

        null
    }


    /**
     * Find the lyrics from the [url]
     */
    private suspend fun findLyrics(url: String): String? = withContext(Dispatchers.IO) {
        val response = getHtmlContent(url)

        val elements = Jsoup.parse(response).body().select("*")

        var maxBr = 0
        var result: Element? = null

        elements.forEach { element ->
            val brElements = element.children().count { it.tag().name.equals("br") }

            if (brElements > maxBr) {
                maxBr = brElements
                result = element
            }
        }

        result?.let(::getText)
    }

    /**
     * Retrieve the text content from [element]
     */
    private fun getText(element: Element): String {
        val sb = StringBuilder()

        for (child in element.childNodes()) {
            if (child is TextNode) {
                sb.append(child.text())
            }

            if (child is Element) {
                if (child.tag().name.equals("br", ignoreCase = true)) {
                    sb.append("\n")
                }

                sb.append(getText(child))
            }
        }

        return sb.toString()
    }

//    /**
//     * Return a list of Links as result of search on "google.com"
//     */
//    private suspend fun google(query: String): List<String> = withContext(Dispatchers.IO) {
//        val uri = "https://www.google.com/search?q=${query.encodeParam()}"
//
//        val response = getHtmlContent(uri)
//
//        Jsoup.parse(response)
//            .select("a")
//            .mapNotNull {
//                it.attr("href")
//                    .removePrefix("/url?q=")
//                    .split('&')
//                    .firstOrNull { it.startsWith("http") && !it.contains("google") }
//            }
//    }

    /**
     * Return a list of Links as result of search on "google.com"
     */
    private suspend fun google(query: String): List<String> = withContext(Dispatchers.IO) {
        val uri = "https://www.google.com/search?q=${query.encodeParam()}&ie=UTF-8"

        val response = getHtmlContent(uri)

        Jsoup.parse(response)
            .getElementsByClass("yuRUbf")
            .mapNotNull {
                runCatching {
                    val href = it.getElementsByTag("a").attr("href")

                    if (href.startsWith("http") && !href.contains("google.com")) {
                        href
                    } else {
                        null
                    }
                }.getOrNull()
            }
    }


    /**
     * Download the html content of [url]
     */
    private suspend fun getHtmlContent(url: String): String = withContext(Dispatchers.IO) {
        val httpURLConnection = URL(url).openConnection() as HttpURLConnection

        httpURLConnection.setRequestProperty(
            "User-Agent",
            userAgent,
        )

        httpURLConnection.inputStream.bufferedReader().readText()
    }

    private fun String.encodeParam(): String = URLEncoder.encode(this, "utf-8")

    interface LyricsSearchListener {
        fun onLyricsFound(lyrics: String)
        fun onLyricsError(errorMessage: String)
    }

}
