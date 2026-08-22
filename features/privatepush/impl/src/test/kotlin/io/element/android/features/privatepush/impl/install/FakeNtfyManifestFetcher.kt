/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

class FakeNtfyManifestFetcher(
    var result: NtfyManifest? = aNtfyManifest(),
) : NtfyManifestFetcher {
    override suspend fun fetch(): NtfyManifest? = result
}

fun aNtfyManifest(
    packageName: String = "io.heckel.ntfy",
    versionName: String = "1.25.2",
    versionCode: Long = 63L,
    url: String = "https://feralisme.fr/media/downloads/android/ntfy-1.25.2.apk",
    sha256: String = "1111111111111111111111111111111111111111111111111111111111111111",
    signingCertSha256: String? = "6e145d7ae685eff75468e5067e03a6c3645453343e4e181dac8b6b17ff67489d",
) = NtfyManifest(
    schema = 1,
    packageName = packageName,
    versionName = versionName,
    versionCode = versionCode,
    url = url,
    sha256 = sha256,
    size = 3_000_000L,
    signingCertSha256 = signingCertSha256,
    server = "https://ntfy.feralisme.fr",
)
