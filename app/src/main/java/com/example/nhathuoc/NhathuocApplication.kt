package com.example.nhathuoc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NhathuocApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Default channel — orders, rewards, system alerts
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DEFAULT,
                "Thông báo chung",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo đơn hàng, điểm thưởng và các cập nhật hệ thống"
            }
        )

        // Chat channel — high importance so heads-up banner shows
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CHAT,
                "Tin nhắn tư vấn",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tin nhắn mới từ chuyên viên dược"
            }
        )
    }

    companion object {
        const val CHANNEL_DEFAULT = "nhathuoc_default"
        const val CHANNEL_CHAT    = "nhathuoc_chat"
    }
}
