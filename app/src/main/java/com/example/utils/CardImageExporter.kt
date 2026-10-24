package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout
import com.example.model.CardTemplate
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class CardExportSize(
    val label: String,
    val width: Int,
    val height: Int,
    val ratioLabel: String,
    val description: String
) {
    CLASSIC_GREETING("Classic Greeting (3:4)", 1200, 1600, "3:4", "Standard luxury greeting card format"),
    STORY_PORTRAIT("Story / Status (9:16)", 1080, 1920, "9:16", "Ideal for Instagram, WhatsApp & Facebook Stories"),
    SQUARE_POST("Square Post (1:1)", 1080, 1080, "1:1", "Perfect for Instagram feeds & Facebook posts"),
    COMPACT_HD("Compact HD (9:16)", 720, 1280, "9:16", "Fast lightweight sharing for chats")
}

object CardImageExporter {

    /**
     * Renders a pristine, high-resolution Bitmap containing ONLY the specific card.
     * Incorporates user uploaded photos, festive ornaments, borders, and custom aspect ratios.
     * Ensures zero device UI buttons, bars, or background elements are captured.
     */
    fun renderCardBitmap(
        template: CardTemplate,
        customTitle: String? = null,
        recipientName: String = "",
        customMessage: String? = null,
        senderName: String = "",
        fontStyle: String = "Classic",
        isPro: Boolean = false,
        exportSize: CardExportSize = CardExportSize.CLASSIC_GREETING,
        photoUri: String? = null,
        context: Context? = null
    ): Bitmap {
        val width = exportSize.width
        val height = exportSize.height
        val scale = (width / 1080f).coerceIn(0.6f, 1.4f)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryColor = (0xFF000000 or template.primaryColorHex).toInt()
        val secondaryColor = (0xFF000000 or template.secondaryColorHex).toInt()
        val goldColor = 0xFFFFD700.toInt()

        // 1. Draw gradient card background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(primaryColor, secondaryColor),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        val cardRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(cardRect, 48f * scale, 48f * scale, bgPaint)

        // 2. Draw luxury outer border & inner accent border
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            style = Paint.Style.STROKE
            strokeWidth = 10f * scale
        }
        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88FFD700.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
        }
        canvas.drawRoundRect(RectF(28f * scale, 28f * scale, width - 28f * scale, height - 28f * scale), 36f * scale, 36f * scale, outerBorderPaint)
        canvas.drawRoundRect(RectF(48f * scale, 48f * scale, width - 48f * scale, height - 48f * scale), 28f * scale, 28f * scale, innerBorderPaint)

        // 3. Decorative top stars and sparkles
        val decorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 32f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✦  ★  ✦  ★  ✦", width / 2f, 110f * scale, decorPaint)

        // 4. Subtitle
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textSize = 26f * scale
            letterSpacing = 0.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(template.subtitle.uppercase(), width / 2f, 160f * scale, subtitlePaint)

        // 5. Main Title
        val titleText = customTitle?.takeIf { it.isNotBlank() } ?: template.title
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 56f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, width / 2f, 235f * scale, titlePaint)

        // Decorative divider under title
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            strokeWidth = 3.5f * scale
        }
        canvas.drawLine(width * 0.25f, 275f * scale, width * 0.75f, 275f * scale, dividerPaint)

        var currentY = 320f * scale

        // 5.5 Framed Holiday Photo if user uploaded an image
        if (!photoUri.isNullOrBlank() && context != null) {
            try {
                val inputUri = Uri.parse(photoUri)
                val stream = context.contentResolver.openInputStream(inputUri)
                val rawBitmap = android.graphics.BitmapFactory.decodeStream(stream)
                stream?.close()
                if (rawBitmap != null) {
                    val frameDiameter = (width * 0.28f).coerceIn(160f * scale, 340f * scale)
                    val frameRadius = frameDiameter / 2f
                    val centerX = width / 2f
                    val centerY = currentY + frameRadius

                    val minDim = minOf(rawBitmap.width, rawBitmap.height)
                    val srcX = (rawBitmap.width - minDim) / 2
                    val srcY = (rawBitmap.height - minDim) / 2
                    val squareBitmap = Bitmap.createBitmap(rawBitmap, srcX, srcY, minDim, minDim)
                    val scaledPhoto = Bitmap.createScaledBitmap(squareBitmap, frameDiameter.toInt(), frameDiameter.toInt(), true)

                    // Draw circular photo using BitmapShader
                    val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = android.graphics.BitmapShader(scaledPhoto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    }

                    // Shadow behind photo frame
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0x55000000
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(centerX, centerY + 5f * scale, frameRadius + 3f * scale, shadowPaint)

                    // Photo content
                    canvas.save()
                    canvas.translate(centerX - frameRadius, centerY - frameRadius)
                    val roundRect = RectF(0f, 0f, frameDiameter, frameDiameter)
                    canvas.drawRoundRect(roundRect, frameRadius, frameRadius, photoPaint)
                    canvas.restore()

                    // Gold decorative circular ring border
                    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = goldColor
                        style = Paint.Style.STROKE
                        strokeWidth = 7f * scale
                    }
                    canvas.drawCircle(centerX, centerY, frameRadius, ringPaint)

                    currentY += frameDiameter + 30f * scale
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 6. Recipient Greeting
        if (recipientName.isNotBlank()) {
            val toPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = goldColor
                textSize = 34f * scale
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Dearest $recipientName,", width / 2f, currentY, toPaint)
            currentY += 65f * scale
        } else {
            currentY += 15f * scale
        }

        // 7. Message Body (StaticLayout for clean multi-line word wrapping)
        val messageText = customMessage?.takeIf { it.isNotBlank() } ?: template.defaultMessage
        val messagePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = (if (exportSize == CardExportSize.SQUARE_POST) 30f else 34f) * scale
            typeface = if (fontStyle == "Serif") Typeface.SERIF else Typeface.DEFAULT
        }
        val messageWidth = (width - (220f * scale).toInt()).coerceAtLeast(350)
        val messageLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(messageText, 0, messageText.length, messagePaint, messageWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(14f * scale, 1.15f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                messageText,
                messagePaint,
                messageWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.15f,
                14f * scale,
                false
            )
        }

        val paddingLeft = (width - messageWidth) / 2f
        canvas.save()
        canvas.translate(paddingLeft, currentY)
        messageLayout.draw(canvas)
        canvas.restore()

        currentY += messageLayout.height + 65f * scale

        // 8. Sender Line
        if (senderName.isNotBlank()) {
            val senderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = goldColor
                textSize = 32f * scale
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("With love & warm wishes,", width / 2f, currentY, senderPaint)
            currentY += 50f * scale
            val senderNamePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 38f * scale
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(senderName, width / 2f, currentY, senderNamePaint)
        }

        // 9. Bottom Festive Ornaments
        val bottomDecorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 28f * scale
            textAlign = Paint.Align.CENTER
        }
        val bottomDecorY = if (!isPro) height - 90f * scale else height - 60f * scale
        canvas.drawText("🎄  ❄️  ✨  ❄️  🎄", width / 2f, bottomDecorY, bottomDecorPaint)

        // 10. Watermark if free user
        if (!isPro) {
            val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xAAFFFFFF.toInt()
                textSize = 20f * scale
                typeface = Typeface.DEFAULT
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("✨ Created with Holiday Wishes 🎄", width / 2f, height - 45f * scale, watermarkPaint)
        }

        return bitmap
    }

    /**
     * Renders an Android View directly to a high-resolution Bitmap.
     * If bounds are provided, renders only the specific card area.
     */
    fun viewToBitmap(view: View, bounds: androidx.compose.ui.geometry.Rect? = null): Bitmap {
        if (bounds != null && bounds.width > 20 && bounds.height > 20) {
            val width = bounds.width.toInt().coerceAtLeast(1)
            val height = bounds.height.toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.translate(-bounds.left, -bounds.top)
            view.draw(canvas)
            return bitmap
        }

        val width = if (view.width > 0) view.width else 1080
        val height = if (view.height > 0) view.height else 1440

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Saves the card bitmap to the local cache and returns a FileProvider content Uri
     * for native Android sharing sheet.
     */
    fun saveBitmapForSharing(context: Context, bitmap: Bitmap, cardTitle: String): Uri? {
        return try {
            val imagesFolder = File(context.cacheDir, "images")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs()
            }
            val fileName = "card_${System.currentTimeMillis()}.png"
            val file = File(imagesFolder, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Launches the native Android sharing chooser with the generated card image.
     */
    fun shareCardImage(context: Context, bitmap: Bitmap, cardTitle: String, shareText: String) {
        val uri = saveBitmapForSharing(context, bitmap, cardTitle)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, cardTitle)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Holiday Greeting Card"))
        } else {
            Toast.makeText(context, "Could not prepare card for sharing", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Saves the card bitmap directly to the device Gallery/Photos using modern MediaStore APIs
     * (scoped storage compatible for Android 10+ and backwards compatible for Android 9-).
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, cardTitle: String): Boolean {
        val filename = "HolidayCard_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HolidayWishes")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                // Android 9 and lower
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val holidayFolder = File(picturesDir, "HolidayWishes")
                if (!holidayFolder.exists()) {
                    holidayFolder.mkdirs()
                }
                val imageFile = File(holidayFolder, filename)
                outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                // Add to MediaStore gallery index
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
        } finally {
            try {
                outputStream?.close()
            } catch (ignored: Exception) {}
        }
    }
}
