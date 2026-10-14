package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.ScheduledGreetingEntity
import com.example.receiver.ScheduledGreetingReceiver

class ScheduledGreetingScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(greeting: ScheduledGreetingEntity) {
        val triggerTime = greeting.scheduledTimestamp
        if (triggerTime <= System.currentTimeMillis()) {
            Log.w("GreetingScheduler", "Cannot schedule in the past: $triggerTime")
            return
        }

        val intent = Intent(context, ScheduledGreetingReceiver::class.java).apply {
            action = ScheduledGreetingReceiver.ACTION_TRIGGER_GREETING
            putExtra(ScheduledGreetingReceiver.EXTRA_GREETING_ID, greeting.id)
            putExtra(ScheduledGreetingReceiver.EXTRA_RECIPIENT_NAME, greeting.recipientName)
            putExtra(ScheduledGreetingReceiver.EXTRA_RECIPIENT_PHONE, greeting.recipientPhone)
            putExtra(ScheduledGreetingReceiver.EXTRA_MESSAGE_TEXT, greeting.messageText)
            putExtra(ScheduledGreetingReceiver.EXTRA_CARD_IMAGE_PATH, greeting.cardImagePath)
            putExtra(ScheduledGreetingReceiver.EXTRA_TARGET_PLATFORM, greeting.targetPlatform)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            greeting.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("GreetingScheduler", "Scheduled greeting id=${greeting.id} for time=$triggerTime")
        } catch (e: SecurityException) {
            Log.w("GreetingScheduler", "Exact alarm permission missing, fallback to set()", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("GreetingScheduler", "Failed to schedule greeting", e)
        }
    }

    fun cancel(greetingId: Long) {
        val intent = Intent(context, ScheduledGreetingReceiver::class.java).apply {
            action = ScheduledGreetingReceiver.ACTION_TRIGGER_GREETING
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            greetingId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("GreetingScheduler", "Cancelled scheduled greeting id=$greetingId")
    }
}
