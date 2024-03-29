package com.example.application

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.application.databinding.ListVideoBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoAdapter(
    var context: Context,
    var videos: VideoResponse?,
    var videoPreparedListner: OnVideoPreparedListner
    ) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>(){
    class VideoViewHolder(
        val binding : ListVideoBinding,
        var context: Context,
        var videoPreparedListner: OnVideoPreparedListner
    ):RecyclerView.ViewHolder(binding.root){
        private lateinit var exoPlayer: ExoPlayer
        private lateinit var mediaSource: MediaSource

        fun setVideoPath(url : String)
        {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.addListener(object : Player.Listener
            {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(context, error.message.toString()+"Can't Play this Video", Toast.LENGTH_SHORT).show()
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if(playbackState==Player.STATE_BUFFERING)
                        binding.pbLoading.visibility = View.VISIBLE
                    else if (playbackState==Player.STATE_READY)
                        binding.pbLoading.visibility = View.GONE
                }
            })
            binding.playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(context)
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(url)))

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            if(absoluteAdapterPosition==0)
            {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }

            videoPreparedListner.onVideoPrepared(ExoPlayerItem(exoPlayer,absoluteAdapterPosition))

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = ListVideoBinding.inflate(LayoutInflater.from(context),parent,false)
        return VideoViewHolder(view,context,videoPreparedListner)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val url = videos?.msg?.get(position)?.video
        if (url != null) {
            holder.setVideoPath(url)
        }
    }

    override fun getItemCount(): Int {
        return videos?.msg?.size!!
    }

    interface OnVideoPreparedListner{
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
    }
}