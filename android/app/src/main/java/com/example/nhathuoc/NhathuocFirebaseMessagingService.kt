package com.example.nhathuoc

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NhathuocFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Persist token so MainActivity can register it with the backend on next launch
        getSharedPreferences(FCM_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .putBoolean(KEY_TOKEN_SYNCED, false)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // App is in foreground — Firebase won't auto-show, so we do it here
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body           = message.notification?.body  ?: message.data["body"]  ?: ""
        val type           = message.data["type"]           ?: ""
        val refId          = message.data["refId"]          ?: ""
        val notificationId = message.data["notificationId"] ?: ""
        showNotification(title, body, type, refId, notificationId)
    }

    private fun showNotification(title: String, body: String, type: String, refId: String, notificationId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_NOTIFICATION_REF_ID, refId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (type.uppercase()) {
            "CHAT" -> NhathuocApplication.CHANNEL_CHAT
            else   -> NhathuocApplication.CHANNEL_DEFAULT
        }

        // Stable ID per (type, refId) so same event updates instead of stacking
        val notifId = (type + refId).hashCode()
        val groupKey = "com.example.nhathuoc.$channelId"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .build()

        manager.notify(notifId, notification)

        // Count active notifications in this group to build summary
        val active = manager.activeNotifications
            .filter { it.notification.group == groupKey && it.id != GROUP_SUMMARY_ID }
        val count = active.size + 1  // +1 for the one we just posted

        if (count >= 2) {
            val inboxStyle = NotificationCompat.InboxStyle()
            active.forEach { inboxStyle.addLine(it.notification.extras.getString("android.text") ?: "") }
            inboxStyle.addLine(body)
            inboxStyle.setBigContentTitle("$count thông báo mới")

            val summary = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Medstore")
                .setContentText("$count thông báo mới")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(inboxStyle)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            manager.notify(GROUP_SUMMARY_ID, summary)
        }
    }

    companion object {
        const val FCM_PREFS            = "fcm_prefs"
        const val KEY_FCM_TOKEN        = "fcm_token"
        const val KEY_TOKEN_SYNCED     = "fcm_token_synced"
        const val EXTRA_NOTIFICATION_TYPE   = "notification_type"
        const val EXTRA_NOTIFICATION_REF_ID = "notification_ref_id"
        const val EXTRA_NOTIFICATION_ID     = "notification_id"
        private const val GROUP_SUMMARY_ID  = 0
    }
}
