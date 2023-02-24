package dev.abdujabbor.musicplayerdemo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.abdujabbor.musicplayerdemo.databinding.RvItemBinding
import dev.abdujabbor.musicplayerdemo.models.Music
import dev.abdujabbor.musicplayerdemo.models.formatDuration


class MusicAdapter(
    private val context: Context,
    private val musicList: ArrayList<Music>,
    var rvClick: RvClick
) :
    RecyclerView.Adapter<MusicAdapter.MyHolder>(), RvClick {
    class MyHolder(binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songName
        val album = binding.albumName
        val image = binding.songImage
        val duration = binding.songLength
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyHolder(
        RvItemBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        val currentItem = musicList[position]
        holder.title.text = currentItem.title
        holder.title.isSelected = true
        holder.album.text = currentItem.album
        holder.album.isSelected = true
        holder.duration.text = formatDuration(currentItem.duration)
//        Glide.with(context)
//            .load(currentItem.imageUri)
//            .apply(RequestOptions.placeholderOf(R.drawable.img).centerCrop())
//            .into(holder.image)
        holder.root.setOnClickListener {
            rvClick.click(position)
        }
    }

    override fun getItemCount() = musicList.size
    override fun click(position: Int) {

    }


}

interface RvClick {
    fun click(position: Int)
}
