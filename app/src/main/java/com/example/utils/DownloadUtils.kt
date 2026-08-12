package com.example.utils

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL

object DownloadUtils {

    suspend fun saveImageFromDataUrlOrHttp(
        context: Context,
        url: String,
        fileNamePrefix: String = "PapAI_Generated"
    ) {
        withContext(Dispatchers.IO) {
            try {
                if (url.startsWith("data:image")) {
                    saveBase64Image(context, url, fileNamePrefix)
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    downloadHttpFile(context, url, fileNamePrefix)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Format URL tidak didukung untuk diunduh", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal mengunduh gambar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveBase64Image(context: Context, dataUrl: String, fileNamePrefix: String) {
        val base64Data = dataUrl.substringAfter(",")
        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: throw Exception("Gambar tidak valid")

        val filename = "${fileNamePrefix}_${System.currentTimeMillis()}.png"

        var outputStream: OutputStream? = null
        var imageUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PapAI")
            }
            val resolver = context.contentResolver
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                outputStream = resolver.openOutputStream(imageUri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val papAiDir = File(imagesDir, "PapAI")
            if (!papAiDir.exists()) {
                papAiDir.mkdirs()
            }
            val imageFile = File(papAiDir, filename)
            outputStream = FileOutputStream(imageFile)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Gambar berhasil disimpan di Galeri (PapAI)", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadHttpFile(context: Context, urlString: String, fileNamePrefix: String) {
        val filename = "${fileNamePrefix}_${System.currentTimeMillis()}.jpg"
        val request = DownloadManager.Request(Uri.parse(urlString)).apply {
            setTitle("Mengunduh Gambar Pap AI")
            setDescription("Mengunduh hasil gambar buatan AI...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "PapAI/$filename")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(context, "Pengunduhan dimulai...", Toast.LENGTH_SHORT).show()
        }
    }
}
