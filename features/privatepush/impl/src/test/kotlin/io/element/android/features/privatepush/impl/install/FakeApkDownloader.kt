/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import io.element.android.features.appupdate.api.ApkDownloadRequest
import io.element.android.features.appupdate.api.ApkDownloader
import io.element.android.features.appupdate.api.AppUpdateStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeApkDownloader(
    private val outcome: AppUpdateStep = AppUpdateStep.ReadyToInstall(APK_PATH),
) : ApkDownloader {
    val requests = mutableListOf<ApkDownloadRequest>()
    val installed = mutableListOf<String>()
    val deletedFiles = mutableListOf<String>()
    var deleteDownloadsCalls = 0
    var cleanupCalls = 0

    override fun downloadAndVerify(request: ApkDownloadRequest): Flow<AppUpdateStep> = flow {
        requests += request
        emit(AppUpdateStep.Downloading(percent = null))
        emit(AppUpdateStep.Downloading(percent = 50))
        emit(outcome)
    }

    override fun install(apkPath: String) {
        installed += apkPath
    }

    override fun delete(fileName: String) {
        deletedFiles += fileName
    }

    override fun deleteDownloads() {
        deleteDownloadsCalls++
    }

    override fun cleanupStaleDownloads() {
        cleanupCalls++
    }

    companion object {
        const val APK_PATH = "/cache/updates/ntfy.apk"
    }
}
