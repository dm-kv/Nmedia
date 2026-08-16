package ru.netology.nmedia.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import android.os.Build
import ru.netology.nmedia.R
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import android.Manifest
import androidx.core.app.NotificationManagerCompat
import android.app.Notification
import android.content.pm.PackageManager
import kotlin.random.Random
import android.util.Log
import android.content.ContentValues.TAG
import android.app.PendingIntent
import android.content.Intent
import kotlin.jvm.java
import ru.netology.nmedia.activity.AppActivity

class FCMService: FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun parseAction(actionStr: String?): Action {
        return try {
            Action.valueOf(actionStr ?: "")
        } catch (e: IllegalArgumentException) {
            Action.UNKNOWN
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

        val actionStr = message.data[action]
        val contentStr = message.data[content]
        val action = parseAction(actionStr)

        when (action) {
            Action.LIKE -> {
                val likeContent = gson.fromJson(contentStr, Like::class.java)
                handleLike(likeContent)
            }
            Action.NEW_POST -> {
                val newPostContent = gson.fromJson(contentStr, NewPost::class.java)
                handleNewPost(newPostContent)
            }
            Action.UNKNOWN -> {
                Log.w(TAG, "Unknown action received: $contentStr")
            }
        }
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)

    }

    private fun handleNewPost(content: NewPost) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${content.userName} опубликовал новый пост")
            .setContentText(content.postText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.postText)
                    .setSummaryText(content.userName)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Открыть пост",
                createOpenPostIntent(content.postId)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun createOpenPostIntent(postId: Long): PendingIntent {
        val intent = Intent(this, AppActivity::class.java).apply {
            putExtra("POST_ID", postId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            this,
            postId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    override fun onNewToken(token: String) {
        println(token)
    }

    enum class Action {
        LIKE,
        UNKNOWN,
        NEW_POST
    }

    data class Like(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postAuthor: String,
    )

    data class NewPost(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postText: String
    )
}