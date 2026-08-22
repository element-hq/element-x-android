/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.appupdate.api.ApkDownloader
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.privatepush.impl.system.InstalledAppsDetector
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * App-scoped owner of the ntfy APK download (twin of the updater's AppUpdateManager), so the
 * download survives page changes and the presenter leaving composition.
 */
@SingleIn(AppScope::class)
@Inject
class NtfyInstaller(
    private val manifestFetcher: NtfyManifestFetcher,
    private val apkDownloader: ApkDownloader,
    private val installedAppsDetector: InstalledAppsDetector,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    private val _step = MutableStateFlow<AppUpdateStep>(AppUpdateStep.Idle)
    val step: StateFlow<AppUpdateStep> = _step.asStateFlow()

    /** Path of a verified APK not yet offered to the installer; consumed once. */
    private val _pendingAutoInstall = MutableStateFlow<String?>(null)
    val pendingAutoInstall: StateFlow<String?> = _pendingAutoInstall.asStateFlow()

    private var downloadJob: Job? = null

    fun startDownload() {
        if (_step.value is AppUpdateStep.Downloading) return
        _pendingAutoInstall.value = null
        downloadJob = appCoroutineScope.launch {
            _step.value = AppUpdateStep.Downloading(percent = null)
            val request = manifestFetcher.fetch()
                ?.toDownloadRequest(installedAppsDetector.installedVersionCode(PrivatePushConfig.NTFY_PACKAGE))
            if (request == null) {
                Timber.w("ntfy manifest missing or rejected")
                _step.value = AppUpdateStep.Failed
                return@launch
            }
            apkDownloader.downloadAndVerify(request).collect { _step.value = it }
            (_step.value as? AppUpdateStep.ReadyToInstall)?.let { _pendingAutoInstall.value = it.apkPath }
        }
    }

    fun consumePendingAutoInstall(): String? = _pendingAutoInstall.getAndUpdate { null }

    /** Opens the package installer if a verified APK is ready. Returns false otherwise. */
    fun install(): Boolean {
        val ready = _step.value as? AppUpdateStep.ReadyToInstall ?: return false
        apkDownloader.install(ready.apkPath)
        return true
    }

    /** Cancels any running download, forgets the result and removes the ntfy file. */
    fun cancelAndReset() {
        downloadJob?.cancel()
        downloadJob = null
        _pendingAutoInstall.value = null
        _step.value = AppUpdateStep.Idle
        appCoroutineScope.launch(coroutineDispatchers.io) {
            apkDownloader.delete(NTFY_APK_FILE_NAME)
        }
    }
}
