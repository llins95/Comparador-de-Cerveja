package com.llins95.comparadordecerveja.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.llins95.comparadordecerveja.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
private const val MAX_METADATA_BYTES = 1024 * 1024
private const val MAX_APK_BYTES = 250L * 1024L * 1024L

data class AppUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String,
    val checksumUrl: String,
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
            var checksumUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                if (!url.startsWith("https://")) continue
                when (name) {
                    "Cerva.apk" -> apkUrl = url
                    "Cerva.apk.sha256" -> checksumUrl = url
                }
            }

            AppUpdateInfo(
                versionName = versionName,
                versionCode = versionCode,
                apkUrl = apkUrl ?: error("A release não contém Cerva.apk."),
                checksumUrl = checksumUrl ?: error("A release não contém Cerva.apk.sha256."),
                releaseNotes = release.optString("body").trim(),
            )
        } finally {
            connection.disconnect()
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    suspend fun downloadAndInstall(update: AppUpdateInfo) = withContext(Dispatchers.IO) {
        val expectedChecksum = fetchChecksum(update.checksumUrl)
        val updateDirectory = File(context.cacheDir, "updates").canonicalFile
        updateDirectory.mkdirs()

        val apk = File(updateDirectory, "Cerva-${update.versionCode}.apk").canonicalFile
        require(apk.parentFile == updateDirectory) {
            "O APK não pertence ao diretório seguro de atualizações."
        }

        if (!matchesChecksum(apk, expectedChecksum)) {
            downloadApk(update.apkUrl, apk)
            if (!matchesChecksum(apk, expectedChecksum)) {
                apk.delete()
                error("O arquivo baixado não passou na validação SHA-256.")
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.updates",
            apk,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        withContext(Dispatchers.Main) {
            context.startActivity(intent)
        }
    }

    private fun fetchChecksum(url: String): String {
        val connection = openConnection(url)
        try {
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Não foi possível obter o checksum da atualização."
            }
            val text = readText(connection, 4096)
            return Regex("\\b[0-9a-fA-F]{64}\\b")
                .find(text)
                ?.value
                ?.lowercase()
                ?: error("Checksum da atualização inválido.")
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadApk(url: String, destination: File) {
        val partial = File("${destination.path}.part")
        partial.delete()
        val connection = openConnection(url)
        try {
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Não foi possível baixar o APK da atualização."
            }
            val declaredLength = connection.contentLengthLong
            require(declaredLength <= 0 || declaredLength <= MAX_APK_BYTES) {
                "O APK excede o tamanho máximo permitido."
            }

            connection.inputStream.use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= MAX_APK_BYTES) {
                            "O APK excede o tamanho máximo permitido."
                        }
                        output.write(buffer, 0, read)
                    }
                    output.fd.sync()
                }
            }

            destination.delete()
            require(partial.renameTo(destination)) {
                "Não foi possível preparar o APK baixado."
            }
        } catch (error: Throwable) {
            partial.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun matchesChecksum(file: File, expected: String): Boolean {
        if (!file.isFile) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        return actual.equals(expected, ignoreCase = true)
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
