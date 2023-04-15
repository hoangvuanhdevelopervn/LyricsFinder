package com.hvasoftware.lyricsfinder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xeinebiu.lyrics_finder.LyricsFinder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var edtSongName: EditText
    private lateinit var edtArtist: EditText
    private lateinit var btnSearch: Button
    private lateinit var tvLyricsBing: TextView
    private lateinit var tvLyricsGoogle: TextView
    private lateinit var pbLoading: ProgressBar
    private val TAG = "MainActivity"
    private var lyrics = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edtSongName = findViewById(R.id.edtSongName)
        edtArtist = findViewById(R.id.edtArtist)
        btnSearch = findViewById(R.id.btnSearch)
        tvLyricsBing = findViewById(R.id.tvLyricsBing)
        tvLyricsGoogle = findViewById(R.id.tvLyricsGoogle)
        pbLoading = findViewById(R.id.pbLoading)

        btnSearch.setOnClickListener {
            val songName = edtSongName.text.toString().trim()
            val artist = edtArtist.text.toString().trim()
            if (songName.isNullOrBlank()) {
                Toast.makeText(this, "Song name can't be empty", Toast.LENGTH_SHORT).show()
            } else {
                lyricsFinders(artist, songName)
                pbLoading.visibility = View.VISIBLE
                btnSearch.visibility = View.GONE
                hideKeyboard()
            }

        }
    }


    private fun lyricsFinders(artist: String, songName: String) {
        GlobalScope.launch {
            val lyricsFinder = LyricsFinder().find(songName)
            runOnUiThread {
                if (lyricsFinder != null) {
                    tvLyricsBing.text = "Result from Bing:\n$lyricsFinder"
                } else {
                    tvLyricsGoogle.text = "No lyrics found"
                }
            }
        }


        GlobalScope.launch {
            val lyricsFinder = if (artist.isNotEmpty()) {
                MyLyricsFinder().find(songName, artist).toString()
            } else {
                MyLyricsFinder().find(songName).toString()
            }
            runOnUiThread {
                hideLoading()
                if (lyricsFinder.length > 10) {
                    tvLyricsGoogle.text = "Result from Google:\n$lyricsFinder"
                } else {
                    tvLyricsGoogle.text = "No lyrics found"
                }
            }
        }
    }

    private fun hideLoading() {
        pbLoading.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtArtist.windowToken, 0)
        imm.hideSoftInputFromWindow(edtSongName.windowToken, 0)
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

}