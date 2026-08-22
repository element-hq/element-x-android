/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.appupdate.api.ApkDownloadRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val NTFY_APK_FILE_NAME = "ntfy.apk"

/** Mirrors https://feralisme.fr/media/downloads/android/ntfy.json. */
@Serializable
data class NtfyManifest(
    val schema: Int = 1,
    @SerialName("package") val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val url: String,
    val sha256: String,
    val size: Long? = null,
    val signingCertSha256: String? = null,
    val server: String? = null,
)

/**
 * Builds the pinned download request, or null when the manifest does not describe the ntfy we expect.
 * The signing certificate comes from [PrivatePushConfig] (compile-time pin); the manifest value is only cross-checked.
 */
fun NtfyManifest.toDownloadRequest(installedVersionCode: Long?): ApkDownloadRequest? {
    if (packageName != PrivatePushConfig.NTFY_PACKAGE) return null
    val manifestCert = signingCertSha256
    if (manifestCert != null && !manifestCert.equals(PrivatePushConfig.NTFY_SIGNING_CERT_SHA256, ignoreCase = true)) return null
    if (!url.startsWith("https://")) return null
    return ApkDownloadRequest(
        url = url,
        sha256 = sha256,
        packageName = packageName,
        versionCode = versionCode,
        signingCertSha256 = PrivatePushConfig.NTFY_SIGNING_CERT_SHA256,
        fileName = NTFY_APK_FILE_NAME,
        // Never downgrade an installed ntfy.
        minVersionCodeExclusive = installedVersionCode,
    )
}
