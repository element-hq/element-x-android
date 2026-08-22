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
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.AppUpdateConfig
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.appupdate.api.AvailableUpdate
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

/**
 * Downloads a Feral update APK into the app cache and verifies it before install.
 */
interface ApkDownloader {
    /** Streams download progress; ends with [AppUpdateStep.ReadyToInstall] or [AppUpdateStep.Failed]. */
    fun downloadAndVerify(update: AvailableUpdate): Flow<AppUpdateStep>

    /** Hands the verified APK to the system package installer. Needs an Activity context. */
    fun install(activityContext: Context, apkPath: String)

    /** Removes every downloaded APK (cancelled, dismissed or partial downloads). */
    fun deleteDownloads()

    /**
     * Removes downloaded APKs that are no longer useful: unreadable files and any APK whose
     * versionCode is not strictly greater than the running app (i.e. the update we just
     * installed, or a stale one). Called at startup so an installed update never leaves
     * a 100+ MB file behind in the cache.
     */
    fun cleanupStaleDownloads()
}

/**
 * A downloaded APK is accepted only when ALL of the following hold:
 *  1. its SHA-256 matches the manifest entry (transport integrity),
 *  2. its signing certificate SHA-256 matches the pinned Feral release certificate
 *     ([AppUpdateConfig.SIGNING_CERT_SHA256]) — an attacker cannot produce this
 *     without the Feral keystore (authenticity),
 *  3. its versionCode is strictly greater than the installed one (anti-downgrade).
 * Android enforces the same-signer rule again at install time; this check simply
 * refuses to even prompt the user with a bad file.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultApkDownloader(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val buildMeta: BuildMeta,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ApkDownloader {
    private val downloadDir: File
        get() = File(context.cacheDir, DOWNLOAD_DIR)

    override fun downloadAndVerify(update: AvailableUpdate): Flow<AppUpdateStep> = flow {
        emit(AppUpdateStep.Downloading(percent = null))
        // Only ever keep one APK in the cache.
        deleteDownloads()
        val file = File(downloadDir.apply { mkdirs() }, DOWNLOAD_FILE_NAME)
        val result = runCatchingExceptions {
            downloadTo(file, update) { percent -> emit(AppUpdateStep.Downloading(percent)) }
            check(verifyApk(file, update)) { "APK failed signature/version verification" }
            file
        }
        result.fold(
            onSuccess = { emit(AppUpdateStep.ReadyToInstall(file.absolutePath)) },
            onFailure = { error ->
                Timber.w(error, "Feral update download/verification failed")
                // Never leave a partial or rejected APK behind (CancellationException is rethrown
                // by runCatchingExceptions, so cancelled downloads are cleaned by deleteDownloads()).
                file.delete()
                emit(AppUpdateStep.Failed)
            },
        )
    }.flowOn(coroutineDispatchers.io)

    override fun install(activityContext: Context, apkPath: String) {
        val authority = "${buildMeta.applicationId}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, File(apkPath))
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MimeTypes.Apk)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activityContext.startActivity(intent)
    }

    override fun deleteDownloads() {
        downloadDir.listFiles()?.forEach { it.delete() }
    }

    override fun cleanupStaleDownloads() {
        downloadDir.listFiles()?.forEach { file ->
            val archiveVersionCode = archiveVersionCode(file)
            if (archiveVersionCode == null || archiveVersionCode <= buildMeta.versionCode) {
                Timber.d("Removing stale Feral update ${file.name} (versionCode=$archiveVersionCode)")
                file.delete()
            }
        }
    }

    private suspend fun downloadTo(
        file: File,
        update: AvailableUpdate,
        onProgress: suspend (Int?) -> Unit,
    ) {
        val request = Request.Builder().url(update.url).build()
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
        check(sha256.equals(update.sha256, ignoreCase = true)) { "sha256 mismatch" }
    }

    private fun archiveVersionCode(file: File): Long? {
        val info = file.packageArchiveInfo(flags = 0) ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    private fun verifyApk(file: File, update: AvailableUpdate): Boolean {
        val pinned = AppUpdateConfig.SIGNING_CERT_SHA256.lowercase()
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
        val archiveVersionCode = archiveVersionCode(file) ?: return false
        val sameApplication = info.packageName == buildMeta.applicationId
        val versionOk = archiveVersionCode > buildMeta.versionCode &&
            archiveVersionCode == update.versionCode
        return certOk && sameApplication && versionOk
    }

    private fun File.packageArchiveInfo(flags: Int) =
        context.packageManager.getPackageArchiveInfo(absolutePath, flags)

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val DOWNLOAD_DIR = "updates"
        const val DOWNLOAD_FILE_NAME = "feral-update.apk"
    }
}
