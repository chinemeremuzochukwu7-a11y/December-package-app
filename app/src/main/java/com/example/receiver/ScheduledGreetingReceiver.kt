package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.MainActivity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder

class ScheduledGreetingReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "scheduled_greetings_channel"
        const val ACTION_TRIGGER_GREETING = "com.example.ACTION_TRIGGER_SCHEDULED_GREETING"
        const val ACTION_DIRECT_WHATSAPP = "com.example.ACTION_DIRECT_WHATSAPP_SEND"
        const val ACTION_DISMISS = "com.example.ACTION_DISMISS_GREETING"

        const val EXTRA_GREETING_ID = "extra_greeting_id"
        const val EXTRA_RECIPIENT_NAME = "extra_recipient_name"
        const val EXTRA_RECIPIENT_PHONE = "extra_recipient_phone"
        const val EXTRA_MESSAGE_TEXT = "extra_message_text"
        const val EXTRA_CARD_IMAGE_PATH = "extra_card_image_path"
        const val EXTRA_TARGET_PLATFORM = "extra_target_platform"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("ScheduledGreetingRx", "Received intent action: $action")

        val greetingId = intent.getLongExtra(EXTRA_GREETING_ID, 0L)
        val recipientName = intent.getStringExtra(EXTRA_RECIPIENT_NAME) ?: "Friend"
        val recipientPhone = intent.getStringExtra(EXTRA_RECIPIENT_PHONE).orEmpty()
        val messageText = intent.getStringExtra(EXTRA_MESSAGE_TEXT).orEmpty()
        val cardImagePath = intent.getStringExtra(EXTRA_CARD_IMAGE_PATH)

        if (action == ACTION_DISMISS) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(greetingId.toInt())
            return
        }

        if (action == ACTION_DIRECT_WHATSAPP) {
            // Dismiss notification first
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(greetingId.toInt())

            // Launch WhatsApp
            launchWhatsAppDirect(context, messageText, recipientPhone, cardImagePath)
            return
        }

        // Mark as delivered in Room DB
        if (greetingId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AppDatabase.getDatabase(context).scheduledGreetingDao().markDelivered(greetingId)
                } catch (e: Exception) {
                    Log.e("ScheduledGreetingRx", "Error updating DB: ${e.message}")
                }
            }
        }

        createNotificationChannel(context)

        // Play festive notification sound
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("ScheduledGreetingRx", "Sound error: ${e.message}")
        }

        // Vibrate
        try {
            val vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
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
        } catch (ignored: Exception) {}

        // Prepare Action 1: Direct WhatsApp send PendingIntent
        val whatsAppIntent = Intent(context, ScheduledGreetingReceiver::class.java).apply {
            this.action = ACTION_DIRECT_WHATSAPP
            putExtra(EXTRA_GREETING_ID, greetingId)
            putExtra(EXTRA_RECIPIENT_NAME, recipientName)
            putExtra(EXTRA_RECIPIENT_PHONE, recipientPhone)
            putExtra(EXTRA_MESSAGE_TEXT, messageText)
            putExtra(EXTRA_CARD_IMAGE_PATH, cardImagePath)
        }
        val whatsAppPendingIntent = PendingIntent.getBroadcast(
            context,
            (greetingId + 1000).toInt(),
            whatsAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prepare Action 2: Share via Other Media PendingIntent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (cardImagePath != null) "image/png" else "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
            if (cardImagePath != null) {
                val file = File(cardImagePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Send Holiday Greeting to $recipientName").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val sharePendingIntent = PendingIntent.getActivity(
            context,
            (greetingId + 2000).toInt(),
            chooserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prepare Content Intent (Open App)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            greetingId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prepare Dismiss Intent
        val dismissIntent = Intent(context, ScheduledGreetingReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra(EXTRA_GREETING_ID, greetingId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (greetingId + 3000).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle("⏰ Time to Send Holiday Greeting to $recipientName! 🎄")
            .setContentText(messageText.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText("Recipient: $recipientName\n\n$messageText"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_dialog_email, "🚀 Send on WhatsApp", whatsAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_share, "📤 Share via Media", sharePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(greetingId.toInt(), notificationBuilder.build())
    }

    private fun launchWhatsAppDirect(
        context: Context,
        message: String,
        phoneNumber: String,
        cardImagePath: String?
    ) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        try {
            var imageUri: Uri? = null
            if (cardImagePath != null) {
                val file = File(cardImagePath)
                if (file.exists()) {
                    imageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }
            }

            if (imageUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else if (cleanPhone.isNotBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, "UTF-8")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to universal chooser
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Holiday Greeting").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Scheduled Holiday Greetings"
            val descriptionText = "Automated alarms and notifications to send scheduled holiday cards and wishes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
