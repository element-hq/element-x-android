/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.api

import kotlinx.coroutines.flow.Flow

/**
 * Downloads an APK into the app cache and verifies it before handing it to the system installer.
 * Used by the Feral self-updater and by the private-notifications flow (ntfy helper app).
 */
interface ApkDownloader {
    /** Streams download progress; ends with [AppUpdateStep.ReadyToInstall] or [AppUpdateStep.Failed]. */
    fun downloadAndVerify(request: ApkDownloadRequest): Flow<AppUpdateStep>

    /**
     * Hands the verified APK to the system package installer. Uses the application context
     * with FLAG_ACTIVITY_NEW_TASK: presenters run inside Molecule (no Compose UI composition,
     * so there is no LocalContext there) — reading LocalContext in a presenter crashes the app.
     */
    fun install(apkPath: String)

    /** Deletes one downloaded file by [ApkDownloadRequest.fileName]. */
    fun delete(fileName: String)

    /** Removes every downloaded APK (cancelled, dismissed or partial downloads). */
    fun deleteDownloads()

    /**
     * Removes downloaded APKs that are no longer useful: unreadable files, a self-update whose
     * versionCode is not strictly greater than the running app, or a foreign package already
     * installed at that version or newer. Called at startup so an installed update never leaves
     * a 100+ MB file behind in the cache.
     */
    fun cleanupStaleDownloads()
}
