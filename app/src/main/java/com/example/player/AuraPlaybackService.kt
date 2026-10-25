package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R

@OptIn(UnstableApi::class)
class AuraPlaybackService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "aura_music_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_UPDATE_NOTIFICATION = "com.example.action.UPDATE_NOTIFICATION"

        var activeMediaSession: MediaSession? = null

        var currentTitle: String = "Aura Music"
        var currentArtist: String = "Reproduciendo música"
        var currentArtworkBytes: ByteArray? = null
        var isPlayingState: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // setMediaNotificationProvider(defaultProvider) // Removed as we handle notification manually

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return activeMediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY -> activeMediaSession?.player?.play()
                ACTION_PAUSE -> activeMediaSession?.player?.pause()
                ACTION_NEXT -> activeMediaSession?.player?.seekToNext()
                ACTION_PREVIOUS -> activeMediaSession?.player?.seekToPrevious()
                ACTION_UPDATE_NOTIFICATION -> {
                    val title = intent.getStringExtra("title")
                    val artist = intent.getStringExtra("artist")
                    val artwork = intent.getByteArrayExtra("artwork")
                    val playing = intent.getBooleanExtra("isPlaying", false)

                    if (!title.isNullOrBlank()) currentTitle = title
                    if (!artist.isNullOrBlank()) currentArtist = artist
                    currentArtworkBytes = artwork
                    isPlayingState = playing

                    postSystemMediaNotification()
                }
            }
        }

        postSystemMediaNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    fun postSystemMediaNotification() {
        val session = activeMediaSession
        val player = session?.player
        val isPlaying = player?.isPlaying ?: isPlayingState

        val artworkBitmap = currentArtworkBytes?.let {
            try { BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pausar",
                getServicePendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Reproducir",
                getServicePendingIntent(ACTION_PLAY)
            )
        }

        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            "Anterior",
            getServicePendingIntent(ACTION_PREVIOUS)
        )

        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "Siguiente",
            getServicePendingIntent(ACTION_NEXT)
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(artworkBitmap)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)

        try {
            val token = session?.sessionCompatToken as? MediaSessionCompat.Token
            val mediaStyle = MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
            if (token != null) {
                mediaStyle.setMediaSession(token)
            }
            builder.setStyle(mediaStyle)
        } catch (ignored: Exception) {}

        val notification = builder.build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AuraPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aurora Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reproducción de música en segundo plano con Aurora Music"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        activeMediaSession = null
        super.onDestroy()
    }
}
