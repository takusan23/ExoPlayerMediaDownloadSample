package io.github.takusan23.exoplayermediadownloadsample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var exoPlayer: SimpleExoPlayer

    // ネットにある動画URL指定しといて
    val CONTENT_URL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        exoPlayer = SimpleExoPlayer.Builder(this).build()

        button.setOnClickListener {
            // サービス起動
            val intent = Intent(this, DownloadServiceTest::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        button2.setOnClickListener {
            cache()
        }

        button3.setOnClickListener {
            playCache()
        }

        button4.setOnClickListener {
            if (::exoPlayer.isInitialized) {
                exoPlayer.playWhenReady = !exoPlayer.playWhenReady
            }
        }

    }

    private fun cache() {
        val downloadRequest = DownloadRequest(
            "cache",
            DownloadRequest.TYPE_PROGRESSIVE,
            CONTENT_URL.toUri(),  /* streamKeys= */
            Collections.emptyList(),  /* customCacheKey= */
            null,
            ByteArray(1024)
        )
        DownloadService.sendAddDownload(this, DownloadServiceTest::class.java, downloadRequest, false)
    }

    private fun playCache() {
        val cache =
            SimpleCache(File("${this.getExternalFilesDir(null)?.path}/cache"), LeastRecentlyUsedCacheEvictor(1024), ExoDatabaseProvider(this))
        val upstreamDataSourceFactory = DefaultDataSourceFactory(this, "@takusan_23")
        val dataSourceFactory = CacheDataSourceFactory(
            cache, upstreamDataSourceFactory
        )
        val mediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(CONTENT_URL.toUri())
        exoPlayer.prepare(mediaSource)
        exoPlayer.setVideoSurfaceView(surfaceView)
        exoPlayer.playWhenReady = true
    }

    private fun cacheHLS() {
        val sourceFactory = DefaultDataSourceFactory(
            this,
            "@takusan_23",
            object : TransferListener {
                override fun onTransferInitializing(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onTransferStart(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onTransferEnd(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onBytesTransferred(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean, bytesTransferred: Int) {

                }
            })
        val downloadHelper = DownloadHelper.forHls(
            this,
            CONTENT_URL.toUri(),
            sourceFactory,
            DefaultRenderersFactory(this)
        )
        downloadHelper.prepare(object : DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                DownloadService.sendAddDownload(this@MainActivity, DownloadServiceTest::class.java, downloadHelper.getDownloadRequest(ByteArray(1024)), false)
                downloadHelper.release()
            }

            override fun onPrepareError(helper: DownloadHelper, e: IOException) {

            }
        })
    }

    private fun playHLSCache() {
        val cache =
            SimpleCache(File("${this.getExternalFilesDir(null)?.path}/cache"), LeastRecentlyUsedCacheEvictor(1024), ExoDatabaseProvider(this))
        val upstreamDataSourceFactory = DefaultDataSourceFactory(this, "@takusan_23")
        val dataSourceFactory = CacheDataSourceFactory(
            cache, upstreamDataSourceFactory
        )
        val sourceFactory = DefaultDataSourceFactory(
            this,
            "@takusan_23",
            object : TransferListener {
                override fun onTransferInitializing(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onTransferStart(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onTransferEnd(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

                }

                override fun onBytesTransferred(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean, bytesTransferred: Int) {

                }
            })
        val downloadHelper = DownloadHelper.forHls(
            this,
            CONTENT_URL.toUri(),
            sourceFactory,
            DefaultRenderersFactory(this)
        )
        downloadHelper.prepare(object : DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                val mediaSource =
                    DownloadHelper.createMediaSource(helper.getDownloadRequest(ByteArray(1024)), dataSourceFactory)
                exoPlayer.prepare(mediaSource)
                exoPlayer.setVideoSurfaceView(surfaceView)
                exoPlayer.playWhenReady = true
            }

            override fun onPrepareError(helper: DownloadHelper, e: IOException) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
    }

}
