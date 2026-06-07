package com.example.aerisiq.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class ReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val apkUrl: String?,
    val publishedAt: String
)

object GithubUpdater {

    private const val RELEASES_URL = "https://api.github.com/repos/agnelfranciso/AerisIQ/releases/latest"
    private val client = OkHttpClient()

    suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RELEASES_URL)
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            val name = json.optString("name", tagName)
            val releaseBody = json.optString("body", "")
            val publishedAt = json.optString("published_at", "")
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }
            ReleaseInfo(tagName, name, releaseBody, apkUrl, publishedAt)
        } catch (e: Exception) {
            null
        }
    }

    fun isNewerVersion(latestTag: String, currentTag: String): Boolean {
        return try {
            val latest = latestTag.trimStart('v').split(".").map { it.toIntOrNull() ?: 0 }
            val current = currentTag.trimStart('v').split(".").map { it.toIntOrNull() ?: 0 }
            for (i in 0 until maxOf(latest.size, current.size)) {
                val l = latest.getOrElse(i) { 0 }
                val c = current.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(apkUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val body = response.body ?: return@withContext false
            val contentLength = body.contentLength()
            val apkFile = File(context.cacheDir, "aerisiq-update.apk")
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(apkFile)
            val buffer = ByteArray(8192)
            var downloaded = 0L
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloaded += read
                if (contentLength > 0) {
                    val progress = (downloaded * 100 / contentLength).toInt()
                    withContext(Dispatchers.Main) { onProgress(progress) }
                }
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
