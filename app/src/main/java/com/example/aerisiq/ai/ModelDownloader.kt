package com.example.aerisiq.ai

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import java.io.File

class ModelDownloader(private val context: Context) {
    
    companion object {
        // Switch to the 0.5B LiteRT model to prevent Native Out-Of-Memory (OOM) crashes on devices
        const val MODEL_URL = "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task"
        const val MODEL_FILENAME = "Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task"
    }

    fun isModelDownloaded(): Boolean {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), MODEL_FILENAME)
        if (!file.exists() || file.length() < 100_000_000L) return false

        // Check if the file is fully downloaded according to DownloadManager
        val existingId = getActiveDownloadId()
        if (existingId != -1L) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(existingId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    cursor.close()
                    // If DownloadManager says it's successful, we trust it regardless of arbitrary byte counts.
                    if (status == DownloadManager.STATUS_SUCCESSFUL) return true
                    // If it's still running, it's not downloaded yet.
                    if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) return false
                } else {
                    cursor.close()
                }
            } else {
                cursor?.close()
            }
        }
        
        // Fallback if DownloadManager history was cleared, but the file exists and has substantial size
        return file.length() > 500_000_000L
    }

    fun getActiveDownloadId(): Long {
        val prefs = context.getSharedPreferences("aerisiq_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("active_download_id", -1L)
    }

    fun downloadModel(): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        // Prevent queuing multiple duplicate downloads
        val existingId = getActiveDownloadId()
        if (existingId != -1L) {
            val query = DownloadManager.Query().setFilterById(existingId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                        cursor.close()
                        return existingId
                    }
                }
                cursor.close()
            }
        }

        // Delete any existing corrupted file to prevent DownloadManager from renaming the new one to "-1"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), MODEL_FILENAME)
        if (file.exists()) {
            file.delete()
        }

        val request = DownloadManager.Request(Uri.parse(MODEL_URL))
            .setTitle("Qwen 2.5 Local AI")
            .setDescription("Downloading intelligence model")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, MODEL_FILENAME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val id = downloadManager.enqueue(request)
        
        context.getSharedPreferences("aerisiq_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("active_download_id", id)
            .apply()
            
        return id
    }

    fun getDownloadProgress(downloadId: Long): Float {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)
        
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            if (statusIndex >= 0 && downloadedIndex >= 0 && totalIndex >= 0) {
                val status = cursor.getInt(statusIndex)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    return 1f
                }
                
                val downloaded = cursor.getLong(downloadedIndex)
                val total = cursor.getLong(totalIndex)
                if (total > 0) {
                    return downloaded.toFloat() / total.toFloat()
                }
            }
        }
        cursor.close()
        return 0f
    }
}
