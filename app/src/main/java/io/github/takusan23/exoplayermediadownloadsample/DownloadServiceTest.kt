package io.github.takusan23.exoplayermediadownloadsample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File


class DownloadServiceTest : DownloadService(FOREGROUND_NOTIFICATION_ID_NONE) {

    // 通知出すらしい
    val NOTIFICATION_CHANNEL = "cache_notification"
    val SERVICE_NOTIFICATION_CHANNEL = "service_notification"

    // ?
    val JOB_ID = 4545

    /**
     * Android 8からService使うには通知出さないといけなくなった。
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(SERVICE_NOTIFICATION_CHANNEL) == null) {
            val notificationChannel =
                NotificationChannel(SERVICE_NOTIFICATION_CHANNEL, "ExoPlayerでダウンロードテスト通知", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notification = Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL).apply {
            setContentTitle("ExoPlayerでダウンロードテスト")
            setContentText("サービスが実行中です")
            setSmallIcon(R.drawable.ic_file_download_black_24dp)
        }
        startForeground(1, notification.build())
    }

    override fun getDownloadManager(): DownloadManager {
        // Note: This should be a singleton in your app.
        val databaseProvider = ExoDatabaseProvider(this)
        // A download cache should not evict media, so should use a NoopCacheEvictor.
        val downloadCache = SimpleCache(
            File("${this.getExternalFilesDir(null)?.path}/cache"),
            NoOpCacheEvictor(),
            databaseProvider
        )
        // Create a factory for reading the data from the network.
        val dataSourceFactory =
            DefaultHttpDataSourceFactory("@takusan_23")
        // Create the download manager.
        val downloadManager = DownloadManager(
            this,
            databaseProvider,
            downloadCache,
            dataSourceFactory
        )
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onIdle(downloadManager: DownloadManager) {
                super.onIdle(downloadManager)
                println("終了？")
            }
        })
        return downloadManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL) == null) {
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL, "オフライン再生準備", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL).apply {
            setContentTitle("オフライン再生準備")
            setContentText(downloads.size.toString())
            setSmallIcon(R.drawable.ic_file_download_black_24dp)
        }
        return notification.build()
    }

    override fun getScheduler(): Scheduler? {
        return PlatformScheduler(this, JOB_ID)
    }
}