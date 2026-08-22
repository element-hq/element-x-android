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

/**
 * Downloads a Feral update APK into the app cache and verifies it before install.
 *
 * A downloaded APK is accepted only when ALL of the following hold:
 *  1. its SHA-256 matches the manifest entry (transport integrity),
 *  2. its signing certificate SHA-256 matches the pinned Feral release certificate
 *     ([AppUpdateConfig.SIGNING_CERT_SHA256]) — an attacker cannot produce this
 *     without the Feral keystore (authenticity),
 *  3. its versionCode is strictly greater than the installed one (anti-downgrade).
 * Android enforces the same-signer rule again at install time; this check simply
 * refuses to even prompt the user with a bad file.
 */
@Inject
class ApkDownloader(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val buildMeta: BuildMeta,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    fun downloadAndVerify(update: AvailableUpdate): Flow<AppUpdateStep> = flow {
        emit(AppUpdateStep.Downloading(percent = null))
        val result = runCatchingExceptions {
            val file = downloadTo(update) { percent -> emit(AppUpdateStep.Downloading(percent)) }
            check(verifyApk(file, update)) { "APK failed signature/version verification" }
            file
        }
        result.fold(
            onSuccess = { file -> emit(AppUpdateStep.ReadyToInstall(file.absolutePath)) },
            onFailure = { emit(AppUpdateStep.Failed) },
        )
    }.flowOn(coroutineDispatchers.io)

    fun install(activityContext: Context, apkPath: String) {
        val authority = "${buildMeta.applicationId}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, File(apkPath))
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MimeTypes.Apk)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activityContext.startActivity(intent)
    }

    private suspend fun downloadTo(
        update: AvailableUpdate,
        onProgress: suspend (Int?) -> Unit,
    ): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(dir, "feral-update.apk")
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
        return file
    }

    private fun verifyApk(file: File, update: AvailableUpdate): Boolean {
        val pm = context.packageManager
        val pinned = AppUpdateConfig.SIGNING_CERT_SHA256.lowercase()
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNATURES)
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
        val archiveVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        val sameApplication = info.packageName == buildMeta.applicationId
        val versionOk = archiveVersionCode > buildMeta.versionCode &&
            archiveVersionCode == update.versionCode
        return certOk && sameApplication && versionOk
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }
}
