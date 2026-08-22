/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import io.element.android.features.appupdate.api.AvailableUpdate
import kotlinx.serialization.Serializable

/**
 * The Feral update channel manifest, served publicly at
 * `https://feralisme.fr/media/downloads/android/update.json` and generated at release
 * time by `tools/feral/publish-release.sh` (run on the build/signing machine).
 *
 * Every APK entry carries its own `versionCode` because the build multiplies the base
 * versionCode by 10 and adds a per-ABI digit (see `app/build.gradle.kts`), so each ABI
 * split has a distinct final versionCode.
 */
@Serializable
data class UpdateManifest(
    val schema: Int = 1,
    val versionName: String,
    val minVersionCode: Long = 0,
    val apks: Map<String, UpdateApkEntry> = emptyMap(),
)

@Serializable
data class UpdateApkEntry(
    val url: String,
    val sha256: String,
    val versionCode: Long,
    val size: Long? = null,
)

internal const val UNIVERSAL_ABI = "universal"

/**
 * Release ordinal of an APK versionCode. The last digit is the ABI code added by
 * app/build.gradle.kts (universal = …0, armeabi-v7a = …1, arm64-v8a = …2, x86 = …3,
 * x86_64 = …4), so two APKs of the SAME release must compare equal — otherwise a
 * universal install would be offered the arm64 split of the release it already runs.
 */
internal fun Long.releaseOrdinal(): Long = this / 10

/**
 * Resolve the manifest against this device: pick the best APK for the supported ABIs
 * (falling back to the universal APK) and return it only when it is a strict upgrade
 * (anti-downgrade) that the user has not ignored.
 */
fun UpdateManifest.selectUpdate(
    supportedAbis: List<String>,
    currentVersionCode: Long,
    ignoredVersionCode: Long?,
): AvailableUpdate? {
    val abi = supportedAbis.firstOrNull { apks.containsKey(it) }
        ?: UNIVERSAL_ABI.takeIf { apks.containsKey(it) }
        ?: return null
    val entry = apks.getValue(abi)
    if (entry.versionCode.releaseOrdinal() <= currentVersionCode.releaseOrdinal()) return null
    if (entry.versionCode == ignoredVersionCode) return null
    return AvailableUpdate(
        versionName = versionName,
        versionCode = entry.versionCode,
        url = entry.url,
        sha256 = entry.sha256,
    )
}
