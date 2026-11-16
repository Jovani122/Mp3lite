package com.example.mp3player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private var songs: List<Song>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.songTitle)
        val artist: TextView = view.findViewById(R.id.songArtist)
        val duration: TextView = view.findViewById(R.id.songDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.duration.text = formatDuration(song.duration)
        holder.itemView.setOnClickListener { onClick(holder.bindingAdapterPosition) }
    }

    override fun getItemCount() = songs.size

    fun update(items: List<Song>) {
        songs = items
        notifyDataSetChanged()
    }

    private fun formatDuration(ms: Long): String {
        val total = ms / 1000
        return "%d:%02d".format(total / 60, total % 60)
    }
}
