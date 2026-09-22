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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CardImageExporter {

    /**
     * Renders an Android View directly to a high-resolution Bitmap.
     */
    fun viewToBitmap(view: View): Bitmap {
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
