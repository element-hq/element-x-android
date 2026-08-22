/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.appupdate.api.ApkDownloadRequest
import io.element.android.features.appupdate.api.ApkDownloader
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

/**
 * A downloaded APK is accepted only when ALL of the following hold:
 *  1. its SHA-256 matches the manifest entry (transport integrity),
 *  2. its signing certificate SHA-256 matches the pinned certificate of the request
 *     (the Feral release certificate for a self-update, the ntfy release certificate
 *     for the helper app) — an attacker cannot produce this without the keystore (authenticity),
 *  3. its package name is the expected one and its versionCode equals the manifest value,
 *     strictly above [ApkDownloadRequest.minVersionCodeExclusive] when set (anti-downgrade).
 * Android enforces the same-signer rule again at install time; this check simply
 * refuses to even prompt the user with a bad file.
 */
@ContributesBinding(AppScope::class)
class DefaultApkDownloader(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val buildMeta: BuildMeta,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ApkDownloader {
    private val downloadDir: File
        get() = File(context.cacheDir, DOWNLOAD_DIR)

    /**
     * File names currently being written. [cleanupStaleDownloads] must skip them: a partially
     * written APK has no readable package info and would otherwise be deleted under a running
     * download (e.g. the ntfy download started in Settings while the room list recomposes).
     */
    private val inProgress: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun downloadAndVerify(request: ApkDownloadRequest): Flow<AppUpdateStep> = flow {
        emit(AppUpdateStep.Downloading(percent = null))
        // One slot per file name: the previous file of this request is replaced, other
        // downloads (e.g. a ntfy APK next to a Feral update) are left alone.
        val file = File(downloadDir.apply { mkdirs() }, request.fileName)
        file.delete()
        inProgress += request.fileName
        val result = try {
            runCatchingExceptions {
                downloadTo(file, request.url, request.sha256) { percent -> emit(AppUpdateStep.Downloading(percent)) }
                check(verifyApk(file, request)) { "APK failed signature/package/version verification" }
                file
            }
        } finally {
            inProgress -= request.fileName
        }
        result.fold(
            onSuccess = { emit(AppUpdateStep.ReadyToInstall(file.absolutePath)) },
            onFailure = { error ->
                Timber.w(error, "APK download/verification failed (${request.packageName})")
                // Never leave a partial or rejected APK behind (CancellationException is rethrown
                // by runCatchingExceptions, so cancelled downloads are cleaned by delete()).
                file.delete()
                emit(AppUpdateStep.Failed)
            },
        )
    }.flowOn(coroutineDispatchers.io)

    override fun install(apkPath: String) {
        val authority = "${buildMeta.applicationId}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, File(apkPath))
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MimeTypes.Apk)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun delete(fileName: String) {
        File(downloadDir, fileName).delete()
    }

    override fun deleteDownloads() {
        downloadDir.listFiles()?.forEach { it.delete() }
    }

    override fun cleanupStaleDownloads() {
        downloadDir.listFiles()?.forEach { file ->
            if (file.name in inProgress) return@forEach
            val info = file.packageArchiveInfo(flags = 0)
            val archiveVersionCode = info?.let { versionCodeOf(it) }
            val stale = when {
                info == null || archiveVersionCode == null -> true
                // Self-update: useless once it is not newer than the running app.
                info.packageName == buildMeta.applicationId -> archiveVersionCode <= buildMeta.versionCode
                // Foreign package (ntfy): useless once installed at that version or newer.
                else -> installedVersionCode(info.packageName)?.let { it >= archiveVersionCode } ?: false
            }
            if (stale) {
                Timber.d("Removing stale APK ${file.name} (versionCode=$archiveVersionCode)")
                file.delete()
            }
        }
    }

    private suspend fun downloadTo(
        file: File,
        url: String,
        expectedSha256: String,
        onProgress: suspend (Int?) -> Unit,
    ) {
        val request = Request.Builder().url(url).build()
        val digest = MessageDigest.getInstance("SHA-256")
        okHttpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            val body = response.body
            val total = body.contentLength()
            var read = 0L
            var lastPercent = -1
            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        read += count
                        if (total > 0) {
                            val percent = ((read * 100) / total).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }
        }
        val sha256 = digest.digest().toHexString()
        check(sha256.equals(expectedSha256, ignoreCase = true)) { "sha256 mismatch" }
    }

    private fun versionCodeOf(info: PackageInfo): Long = PackageInfoCompat.getLongVersionCode(info)

    private fun installedVersionCode(packageName: String): Long? = runCatchingExceptions {
        versionCodeOf(context.packageManager.getPackageInfo(packageName, 0))
    }.getOrNull()

    private fun verifyApk(file: File, request: ApkDownloadRequest): Boolean {
        val pinned = request.signingCertSha256.lowercase()
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            file.packageArchiveInfo(PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            file.packageArchiveInfo(PackageManager.GET_SIGNATURES)
        } ?: return false
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        if (signatures.isNullOrEmpty()) return false
        val certOk = signatures.all { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).toHexString() == pinned
        }
        val archiveVersionCode = versionCodeOf(info)
        val packageOk = info.packageName == request.packageName
        val minVersionCode = request.minVersionCodeExclusive
        val versionOk = archiveVersionCode == request.versionCode &&
            (minVersionCode == null || archiveVersionCode > minVersionCode)
        return certOk && packageOk && versionOk
    }

    private fun File.packageArchiveInfo(flags: Int) =
        context.packageManager.getPackageArchiveInfo(absolutePath, flags)

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val DOWNLOAD_DIR = "updates"
    }
}
