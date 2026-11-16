package com.example.mp3player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity() {
    private val songs = mutableListOf<Song>()
    private lateinit var adapter: SongAdapter
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null
    private lateinit var status: TextView
    private lateinit var currentTitle: TextView
    private lateinit var currentArtist: TextView
    private lateinit var playButton: Button

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) loadSongs()
            else {
                status.text = "Autorisation refusée. Autorise l'accès audio pour afficher tes chansons."
                Toast.makeText(this, "Accès audio non autorisé", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.statusText)
        currentTitle = findViewById(R.id.nowPlaying)
        currentArtist = findViewById(R.id.artistText)
        playButton = findViewById(R.id.playButton)

        val list = findViewById<RecyclerView>(R.id.songList)
        adapter = SongAdapter(emptyList()) { index -> playSong(index) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<Button>(R.id.permissionButton).setOnClickListener { requestAudioPermission() }
        playButton.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    playButton.text = "▶ Lecture"
                } else {
                    it.play()
                    playButton.text = "Ⅱ Pause"
                }
            }
        }
        findViewById<Button>(R.id.prevButton).setOnClickListener { player?.seekToPreviousMediaItem() }
        findViewById<Button>(R.id.nextButton).setOnClickListener { player?.seekToNextMediaItem() }

        connectPlaybackService()
        if (hasAudioPermission()) loadSongs() else requestAudioPermission()
    }

    private fun connectPlaybackService() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            try {
                player = controllerFuture?.get()
            } catch (_: Exception) {
                Toast.makeText(this, "Impossible de démarrer le lecteur", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, audioPermission()) == PackageManager.PERMISSION_GRANTED

    private fun requestAudioPermission() {
        if (hasAudioPermission()) loadSongs() else permissionLauncher.launch(audioPermission())
    }

    private fun loadSongs() {
        songs.clear()
        val collection: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val cursor: Cursor? = contentResolver.query(collection, projection, selection, null, sortOrder)
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val uri = Uri.withAppendedPath(collection, id.toString())
                songs.add(Song(
                    id,
                    it.getString(titleCol) ?: "Titre inconnu",
                    it.getString(artistCol)?.takeUnless { a -> a == "<unknown>" } ?: "Artiste inconnu",
                    it.getLong(durationCol),
                    uri
                ))
            }
        }
        adapter.update(songs.toList())
        status.text = if (songs.isEmpty()) "Aucun morceau trouvé. Ajoute des fichiers audio sur ton téléphone."
            else "${songs.size} morceau(x) trouvé(s)"
    }

    private fun playSong(index: Int) {
        val p = player
        if (p == null) {
            Toast.makeText(this, "Le lecteur démarre, réessaie dans un instant", Toast.LENGTH_SHORT).show()
            return
        }
        if (index !in songs.indices) return
        val playlist = songs.map { song ->
            MediaItem.Builder().setUri(song.uri).setMediaId(song.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title).setArtist(song.artist).build()
                ).build()
        }
        p.setMediaItems(playlist, index, 0)
        p.prepare()
        p.play()
        currentTitle.text = songs[index].title
        currentArtist.text = songs[index].artist
        playButton.text = "Ⅱ Pause"
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        player = null
        super.onDestroy()
    }
}
