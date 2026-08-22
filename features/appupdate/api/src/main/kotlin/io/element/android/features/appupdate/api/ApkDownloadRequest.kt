/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.api

import androidx.compose.runtime.Immutable

/**
 * What to download and the conditions under which the downloaded file is accepted.
 * All checks must pass: sha256 of the bytes, signing certificate of the archive,
 * package name, versionCode == [versionCode] (and > [minVersionCodeExclusive] when set).
 */
@Immutable
data class ApkDownloadRequest(
    val url: String,
    val sha256: String,
    val packageName: String,
    val versionCode: Long,
    /** SHA-256 (DER, lowercase hex) every signer of the archive must match. */
    val signingCertSha256: String,
    /** File name inside the download cache dir; one slot per name. */
    val fileName: String,
    /** Anti-downgrade floor (e.g. the running app's versionCode for a self-update), null = no floor. */
    val minVersionCodeExclusive: Long? = null,
)
