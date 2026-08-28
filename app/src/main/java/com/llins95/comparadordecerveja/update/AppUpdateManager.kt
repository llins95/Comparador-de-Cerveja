package com.llins95.comparadordecerveja.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.llins95.comparadordecerveja.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val MAX_METADATA_BYTES = 1024 * 1024

data class AppUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String,
    val releaseUrl: String,
    val releaseNotes: String,
)

class AppUpdateManager(private val context: Context) {
    private val releasesApi =
        "https://api.github.com/repos/llins95/Comparador-de-Cerveja/releases/latest"

    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val connection = openConnection(releasesApi, acceptJson = true)
        try {
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext null
            }
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "O GitHub respondeu com HTTP ${connection.responseCode}."
            }

            val payload = readText(connection, MAX_METADATA_BYTES)
            val release = JSONObject(payload)
            val tag = release.optString("tag_name").trim()
            val match = Regex("^v(\\d+\\.\\d+\\.\\d+)\\+(\\d+)$").matchEntire(tag)
                ?: error("Tag de versão da Cerva inválida.")

            val versionName = match.groupValues[1]
            val versionCode = match.groupValues[2].toInt()
            if (versionCode <= BuildConfig.VERSION_CODE) {
                return@withContext null
            }

            val assets = release.optJSONArray("assets")
                ?: error("A release da Cerva não contém arquivos.")
            var apkUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                if (name == "Cerva.apk" && url.startsWith("https://")) {
                    apkUrl = url
                    break
                }
            }

            val releaseUrl = release.optString("html_url").trim()
            require(releaseUrl.startsWith("https://")) {
                "Link da release da Cerva inválido."
            }

            AppUpdateInfo(
                versionName = versionName,
                versionCode = versionCode,
                apkUrl = apkUrl ?: error("A release não contém Cerva.apk."),
                releaseUrl = releaseUrl,
                releaseNotes = release.optString("body").trim(),
            )
        } finally {
            connection.disconnect()
        }
    }

    fun openDownload(update: AppUpdateInfo) {
        val primaryIntent = browserIntent(update.apkUrl)
        val fallbackIntent = browserIntent(update.releaseUrl)
        val intent = if (primaryIntent.resolveActivity(context.packageManager) != null) {
            primaryIntent
        } else {
            fallbackIntent
        }
        context.startActivity(intent)
    }

    private fun browserIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun openConnection(url: String, acceptJson: Boolean = false): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Cerva-App-Updater")
            if (acceptJson) {
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }
        }
    }

    private fun readText(connection: HttpURLConnection, maxBytes: Int): String {
        val bytes = connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= maxBytes) { "Resposta de atualização muito grande." }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        }
        return bytes.toString(Charsets.UTF_8)
    }
}
