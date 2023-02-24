package dev.abdujabbor.musicplayerdemo.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.abdujabbor.musicplayerdemo.R
import dev.abdujabbor.musicplayerdemo.adapters.MusicAdapter
import dev.abdujabbor.musicplayerdemo.adapters.RvClick
import dev.abdujabbor.musicplayerdemo.databinding.FragmentHomeBinding
import dev.abdujabbor.musicplayerdemo.models.Music
import dev.abdujabbor.musicplayerdemo.utils.MyData
import dev.abdujabbor.musicplayerdemo.utils.MyData.mediaPlayer
import dev.abdujabbor.musicplayerdemo.utils.MyData.mp
import java.io.File


class HomeFragment : Fragment(), RvClick {
    val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }

    companion object {
        lateinit var MusicListMA: ArrayList<Music>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                initLayouts()
            }
        }
        return binding.root
    }

    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                initLayouts()
            } else ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13
            )
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initLayouts() {

        MusicListMA = getAllMusic()
        binding.musicRV.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(requireContext())
            val adapterRv = MusicAdapter(requireContext(), MusicListMA, this@HomeFragment)
            adapter = adapterRv
        }
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllMusic(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val IdC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val imageUri = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(IdC, titleC, albumC, artistC, durationC, pathC, imageUri)
                    val file = File(pathC)
                    if (file.exists()) {
                        tempList.add(music)
                        MyData.music.add(music)
                    }


                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun click(position: Int) {
        var a = MyData.music[position].path
        if (mediaPlayer.isPlaying){
            mediaPlayer.reset()
        }
        mp = 0
        findNavController().navigate(
            R.id.playingFragment, bundleOf(
                "position" to position, "class" to "MainActivity",
                "music" to a ,
            )
        )
    }
}