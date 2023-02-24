package dev.abdujabbor.musicplayerdemo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dev.abdujabbor.musicplayerdemo.R
import dev.abdujabbor.musicplayerdemo.databinding.FragmentPlayingBinding
import dev.abdujabbor.musicplayerdemo.fragments.HomeFragment.Companion.MusicListMA
import dev.abdujabbor.musicplayerdemo.models.Music
import dev.abdujabbor.musicplayerdemo.models.formatDuration
import dev.abdujabbor.musicplayerdemo.utils.MyData
import dev.abdujabbor.musicplayerdemo.utils.MyData.mediaPlayer
import dev.abdujabbor.musicplayerdemo.utils.MyData.mp
import dev.abdujabbor.musicplayerdemo.utils.MyData.music


class PlayingFragment : Fragment() {
    lateinit var animation: Animation
    private val binding by lazy { FragmentPlayingBinding.inflate(layoutInflater) }
    lateinit var handler: Handler

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false

        @SuppressLint("StaticFieldLeak")
        var repeat: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()
        initializeLayout()
        binding.playPauseBtn.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.prevBtn.setOnClickListener { prevNextSong(false) }
        binding.nextBtn.setOnClickListener { prevNextSong(true) }
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBAr: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
//        binding.repeatBtn.setOnClickListener {
//            if (!repeat) {
//                repeat = true
//                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.primary))
//            } else {
//                repeat = false
//                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.cool_gray))
//            }
//        }
        return binding.root
    }

    private fun initializeLayout() {
        songPosition = requireArguments().getInt("position", 0)
        when (requireArguments().getString("class")) {
            "MusicAdapter" -> {
                musicListPA = ArrayList<Music>()
                musicListPA.addAll(MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                musicListPA = ArrayList<Music>()
                musicListPA.addAll(MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun setLayout() {
        Glide.with(this).load(MyData.music[requireArguments().getInt("position")+mp].imageUri)
            .apply(RequestOptions.placeholderOf(R.drawable.img).centerCrop())
            .into(binding.image)
        binding.songName.text = MyData.music[requireArguments().getInt("position")+mp].title
        binding.songName.isSelected = true
        binding.duration.text = formatDuration(MyData.music[requireArguments().getInt("position")+mp].duration)
//        if (repeat) binding.repeatBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
    }

    private fun playMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        mediaPlayer.start()
        binding.image.startAnimation(animation)
        isPlaying = true
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        mediaPlayer.pause()
        binding.image.clearAnimation()
        isPlaying = false
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            if (music.size-1==requireArguments().getInt("position")+mp){
                findNavController().popBackStack()
            }else {
                mp += 1
                mediaPlayer.reset()
                mediaPlayer.setDataSource(MyData.music[requireArguments().getInt("position") + mp].path)
                mediaPlayer.prepare()
                mediaPlayer.start()
                setLayout()
            }
        }else if (!increment){
            if (requireArguments().getInt("position")+mp==0){
                findNavController().popBackStack()
            }else {
                mp -= 1
                mediaPlayer.reset()
                mediaPlayer.setDataSource(MyData.music[requireArguments().getInt("position") + mp].path)
                mediaPlayer.prepare()
                mediaPlayer.start()
                setLayout()
            }
        }
    }

    // load all data
    @SuppressLint("SetTextI18n")
    fun loadData() {
        handler = Handler()
        changingMusic()
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
        /** tv progress */
        if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.seekBarStartPA.text =
            "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
        else binding.seekBarStartPA.text =
            "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"

        try {
            MyData.mediaPlayer.setDataSource(
                requireContext(),
                MyData.music[requireArguments().getInt("position")+ mp].path!!.toUri()

            )
            MyData.mediaPlayer.prepare()
            MyData.mediaPlayer.start()

        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        }

        binding.image.animation = animation
        seekVolume()
    }


    //change volume

    fun seekVolume() {
        binding.seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val audioManager =
                    requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val volume: Int =
                    progress * maxVolume / 100 // Scale the progress to the volume range
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    fun changingMusic() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                binding.seekBarPA.progress = mediaPlayer.currentPosition
                if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.seekBarStartPA.text =
                    "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
                else binding.seekBarStartPA.text =
                    "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"

                handler.postDelayed(this, 1000)
            }
        }, 1000)

        /** changing music using seekBar */
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer.seekTo(p1)
                    if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.seekBarStartPA.text =
                        "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
                    else binding.seekBarStartPA.text =
                        "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"

                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

    }
}
