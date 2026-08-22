/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.PrivatePushConfig
import kotlinx.serialization.json.Json
import org.junit.Test

class NtfyManifestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `the real ntfy json shape is parsed, unknown keys ignored`() {
        val manifest = json.decodeFromString(
            NtfyManifest.serializer(),
            """
            {"schema":1,"package":"io.heckel.ntfy","versionName":"1.25.2","versionCode":63,
             "url":"https://feralisme.fr/media/downloads/android/ntfy-1.25.2.apk",
             "sha256":"1111111111111111111111111111111111111111111111111111111111111111","size":2999999,
             "signingCertSha256":"6e145d7ae685eff75468e5067e03a6c3645453343e4e181dac8b6b17ff67489d",
             "server":"https://ntfy.feralisme.fr","future":"ignored"}
            """.trimIndent()
        )
        assertThat(manifest.packageName).isEqualTo("io.heckel.ntfy")
        assertThat(manifest.versionCode).isEqualTo(63L)
        assertThat(manifest.size).isEqualTo(2999999L)
        assertThat(manifest.server).isEqualTo("https://ntfy.feralisme.fr")
    }

    @Test
    fun `the request pins the config certificate and the installed version as anti-downgrade floor`() {
        val request = aNtfyManifest(signingCertSha256 = PrivatePushConfig.NTFY_SIGNING_CERT_SHA256.uppercase())
            .toDownloadRequest(installedVersionCode = 60L)
        assertThat(request).isNotNull()
        assertThat(request!!.packageName).isEqualTo(PrivatePushConfig.NTFY_PACKAGE)
        assertThat(request.versionCode).isEqualTo(63L)
        assertThat(request.signingCertSha256).isEqualTo(PrivatePushConfig.NTFY_SIGNING_CERT_SHA256)
        assertThat(request.fileName).isEqualTo(NTFY_APK_FILE_NAME)
        assertThat(request.minVersionCodeExclusive).isEqualTo(60L)
    }

    @Test
    fun `a manifest without certificate still yields the pinned request with no floor`() {
        val request = aNtfyManifest(signingCertSha256 = null).toDownloadRequest(installedVersionCode = null)
        assertThat(request?.signingCertSha256).isEqualTo(PrivatePushConfig.NTFY_SIGNING_CERT_SHA256)
        assertThat(request?.minVersionCodeExclusive).isNull()
    }

    @Test
    fun `wrong package, wrong certificate or plain http are rejected`() {
        assertThat(aNtfyManifest(packageName = "com.evil").toDownloadRequest(null)).isNull()
        assertThat(aNtfyManifest(signingCertSha256 = "00".repeat(32)).toDownloadRequest(null)).isNull()
        assertThat(aNtfyManifest(url = "http://feralisme.fr/ntfy.apk").toDownloadRequest(null)).isNull()
    }
}
