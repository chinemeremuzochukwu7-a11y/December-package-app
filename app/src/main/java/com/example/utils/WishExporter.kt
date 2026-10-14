package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.subscription.SubscriptionManager
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URLEncoder

object WishExporter {

    /**
     * Renders a festive greeting wish card directly to a high-resolution Bitmap
     * and saves it into the device's Gallery/Photos (Pictures/HolidayWishes).
     */
    fun saveWishAsImageToGallery(
        context: Context,
        wishText: String,
        occasionTitle: String = "Holiday Wish",
        recipientName: String = ""
    ): Boolean {
        return try {
            val bitmap = renderWishBitmap(context, wishText, occasionTitle, recipientName)
            val filename = "HolidayWish_${System.currentTimeMillis()}.png"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HolidayWishes")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    outputStream?.close()

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val holidayFolder = File(picturesDir, "HolidayWishes")
                if (!holidayFolder.exists()) {
                    holidayFolder.mkdirs()
                }
                val imageFile = File(holidayFolder, filename)
                outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Saves the wish text as a formatted document file into the device Downloads folder.
     */
    fun saveWishAsTextFileToDownloads(
        context: Context,
        wishText: String,
        occasionTitle: String = "Holiday Wish"
    ): Boolean {
        return try {
            val filename = "Wish_${System.currentTimeMillis()}.txt"
            val isPro = SubscriptionManager.isPro.value
            val contentToSave = buildString {
                append("═════════════════════════════════════\n")
                append("      ✨ $occasionTitle ✨\n")
                append("═════════════════════════════════════\n\n")
                append(wishText)
                append("\n\n")
                if (!isPro) {
                    append("─────────────────────────────────────\n")
                    append("Created with Holiday Wishes App 🎄\n")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/HolidayWishes")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(contentToSave.toByteArray())
                    }
                    true
                } else {
                    false
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val folder = File(downloadsDir, "HolidayWishes")
                if (!folder.exists()) folder.mkdirs()
                val file = File(folder, filename)
                FileOutputStream(file).use { stream ->
                    stream.write(contentToSave.toByteArray())
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates a high-res artistic Bitmap representation of the wish.
     */
    private fun renderWishBitmap(
        context: Context,
        wishText: String,
        occasionTitle: String,
        recipientName: String
    ): Bitmap {
        val width = 1080
        val height = 1350 // 4:5 Instagram / Portrait card ratio
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Festive rich gradient background (Crimson to deep burgundy)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#800A1C"),
                Color.parseColor("#38040B"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Elegant double gold border
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#AA8C2C")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val outerRect = RectF(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat())
        val innerRect = RectF(56f, 56f, (width - 56).toFloat(), (height - 56).toFloat())
        canvas.drawRoundRect(outerRect, 24f, 24f, goldBorderPaint)
        canvas.drawRoundRect(innerRect, 18f, 18f, innerBorderPaint)

        // 3. Top Decorative Occasion Banner
        val headerPaint = TextPaint().apply {
            color = Color.parseColor("#FFDF73")
            textSize = 52f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("✨ $occasionTitle ✨", width / 2f, 150f, headerPaint)

        // 4. Optional Recipient
        var textStartY = 240f
        if (recipientName.isNotBlank()) {
            val toPaint = TextPaint().apply {
                color = Color.WHITE
                textSize = 42f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText("Dearest $recipientName,", width / 2f, textStartY, toPaint)
            textStartY += 80f
        }

        // 5. Wish Text Body (Word-wrapped with StaticLayout)
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#FFF8E7")
            textSize = 46f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        val textWidth = width - 180
        @Suppress("DEPRECATION")
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(wishText, 0, wishText.length, textPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(16f, 1.25f)
                .build()
        } else {
            StaticLayout(
                wishText,
                textPaint,
                textWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.25f,
                16f,
                false
            )
        }

        canvas.save()
        // Center vertically in remaining area
        val contentHeight = staticLayout.height
        val targetY = (height / 2f) - (contentHeight / 2f) + 40f
        canvas.translate(90f, targetY.coerceAtLeast(textStartY))
        staticLayout.draw(canvas)
        canvas.restore()

        // 6. Watermark check
        val isPro = SubscriptionManager.isPro.value
        if (!isPro) {
            val watermarkPaint = TextPaint().apply {
                color = Color.parseColor("#B3D4AF37")
                textSize = 30f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            canvas.drawText("✨ Created with Holiday Wishes 🎄", width / 2f, height - 80f, watermarkPaint)
        }

        return bitmap
    }

    /**
     * Direct WhatsApp dispatcher helper.
     */
    fun sendDirectToWhatsApp(
        context: Context,
        message: String,
        phoneNumber: String? = null,
        imageUri: Uri? = null
    ) {
        try {
            val cleanPhone = phoneNumber?.replace(Regex("[^0-9]"), "") ?: ""
            if (imageUri != null) {
                // Send with Image Attachment
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else if (cleanPhone.isNotBlank()) {
                // Direct WhatsApp chat link
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, "UTF-8")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                // WhatsApp generic share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // WhatsApp not installed or error: fallback to general chooser
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (imageUri != null) "image/png" else "text/plain"
                if (imageUri != null) putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
