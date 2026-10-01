package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "holiday_alarms_channel"
        const val ACTION_TRIGGER_ALARM = "com.example.ACTION_ALARM_TRIGGER"
        const val ACTION_DISMISS_ALARM = "com.example.ACTION_DISMISS_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TYPE = "extra_type" // "ALARM" or "REMINDER"
        const val EXTRA_IS_LOUD = "extra_is_loud"

        private var currentRingtone: Ringtone? = null

        fun stopAlarm() {
            try {
                currentRingtone?.stop()
                currentRingtone = null
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error stopping ringtone", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received intent action: $action")

        if (action == ACTION_DISMISS_ALARM) {
            stopAlarm()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L).toInt()
            notificationManager.cancel(alarmId)
            return
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, System.currentTimeMillis()).toInt()
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Holiday Reminder"
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "ALARM"
        val isLoud = intent.getBooleanExtra(EXTRA_IS_LOUD, true)

        createNotificationChannel(context)

        // 1. Play sound / ringtone
        try {
            val alertUri = if (type == "ALARM") {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            stopAlarm()
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(if (type == "ALARM") AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone?.play()
            currentRingtone = ringtone
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to play alarm ringtone", e)
        }

        // 2. Vibrate
        try {
            val vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(vibrationPattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to trigger vibration", e)
        }

        // 3. Build & Dispatch High-Priority Heads-Up Notification
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId.toLong())
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 100000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (type == "ALARM") "⏰ ALARM: $title" else "🔔 REMINDER: $title")
            .setContentText("Holiday event scheduled time reached! Tap to open.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (type == "ALARM") NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", dismissPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Holiday Alarms & Reminders"
            val descriptionText = "Notifications for holiday wakeups, countdowns, and gift reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
