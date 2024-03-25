package com.example.application

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.application.databinding.ActivityMainBinding
import com.example.application.databinding.ActivityMainBinding.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter : VideoAdapter
    private  var videos : VideoResponse? = null

    private val exoPlayerItems = ArrayList<ExoPlayerItem>()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      binding = inflate(layoutInflater)
        setContentView(binding.root)



        lifecycleScope.launch {
             videos = fetchVideos()
            if (videos != null) {
                // Handle the list of videos (e.g., display in RecyclerView)
                adapter = VideoAdapter(this@MainActivity,videos,object : VideoAdapter.OnVideoPreparedListner{
                    override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                        exoPlayerItems.add(exoPlayerItem)
                    }

                })
                binding.viewPager2.adapter = adapter
                cacheMedia(videos)
            } else {
                // Handle error or empty response
                Log.d("check", "onCreate: error")
            }
        }
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                val prevIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if(prevIndex!=-1)
                {
                    val player = exoPlayerItems[prevIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if(newIndex!=-1)
                {
                    val player = exoPlayerItems[newIndex].exoPlayer
                        player.playWhenReady = true
                    player.play()

                }
            }
        })

    }
    private suspend fun cacheMedia(videos: VideoResponse?) {
        withContext(Dispatchers.IO) {
            val dataSourceFactory = DefaultDataSourceFactory(this@MainActivity, "user-agent")
            val cacheDir = File(this@MainActivity.cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024) // 50 MB cache
            val cache = SimpleCache(cacheDir, evictor)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)


            if (videos != null) {
                // Assuming each Video object has a position property
                for (video in videos.msg) {
                    val newIndex = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.verticalScrollbarPosition}
                    if (newIndex != -1) {
                        val exoPlayer = exoPlayerItems[newIndex].exoPlayer
                        // Create a MediaSource for the video
                        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(Uri.parse(video.video)))

                        // Set the media source for the ExoPlayer
                        exoPlayer.setMediaSource(mediaSource)

                        // Prepare the ExoPlayer (load the media source)
                        exoPlayer.prepare()

                        // Preload 5-10 seconds of each video in the background
                        exoPlayer.seekTo(9000)

                        // Set playWhenReady to false to pause the playback initially
                        exoPlayer.playWhenReady = false
                    }
                }

            }
        }
    }

    private suspend fun fetchVideos(): VideoResponse? {
        return try {
            val response = RetrofitInstance.apiInterface.getVideos()
            if (response.isSuccessful) {
                response.body()
            } else {
                // Handle error response or return null
                null
            }
        } catch (e: Exception) {
            // Handle network or parsing errors and return null
            null
        }
    }


    override fun onPause() {
        super.onPause()
        val index = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
        if(index!=-1)
        {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()
        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if(index!=-1)
        {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(exoPlayerItems.isNotEmpty())
        {
            for(item in exoPlayerItems)
            {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }
}