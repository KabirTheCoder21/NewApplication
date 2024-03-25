package com.example.application

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

const val CACHE_DIR_NAME = "cached_video"
const val MAX_CACHE_SIZE = 256 * 1024 * 1024L // 256MB

class MediaCache private constructor(context: Context) {
    val cacheFactory: CacheDataSource.Factory

    init {
        cacheFactory = setupExoPlayerCache(context)
    }

    companion object {
        @Volatile
        private lateinit var instance: MediaCache

        fun getInstance(context: Context): MediaCache {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = MediaCache(context)
                }
                return instance
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setupExoPlayerCache(context: Context): CacheDataSource.Factory {
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
        val databaseProvider = StandaloneDatabaseProvider(context)
        val cache = SimpleCache(
            File(context.cacheDir, CACHE_DIR_NAME),
            cacheEvictor, databaseProvider
        )
        val upstreamFactory = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory().apply {
            setCache(cache)
            setUpstreamDataSourceFactory(upstreamFactory)
            setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }
}
