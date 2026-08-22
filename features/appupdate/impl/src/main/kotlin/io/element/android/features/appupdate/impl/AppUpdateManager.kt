/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.appupdate.api.AvailableUpdate
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

/**
 * App-scoped owner of the update download.
 *
 * The download runs on the application coroutine scope, so it survives the room list
 * (and its banner presenter) leaving composition: opening a room or backgrounding the
 * app no longer cancels a 100+ MB download half-way. The presenter only observes
 * [step] and forwards user intents.
 */
@SingleIn(AppScope::class)
@Inject
class AppUpdateManager(
    private val apkDownloader: ApkDownloader,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    private val _step = MutableStateFlow<AppUpdateStep>(AppUpdateStep.Idle)
    val step: StateFlow<AppUpdateStep> = _step.asStateFlow()

    /**
     * Path of an APK whose download just completed and has not been offered to the
     * installer yet. Consumed once (by whichever presenter is on screen) so the installer
     * opens automatically right after a download, but never re-opens by itself later.
     */
    private val _pendingAutoInstall = MutableStateFlow<String?>(null)
    val pendingAutoInstall: StateFlow<String?> = _pendingAutoInstall.asStateFlow()

    private var downloadJob: Job? = null

    /** Starts the download unless one is already running. */
    fun startDownload(update: AvailableUpdate) {
        if (_step.value is AppUpdateStep.Downloading) return
        _pendingAutoInstall.value = null
        downloadJob = appCoroutineScope.launch {
            apkDownloader.downloadAndVerify(update).collect { progress ->
                _step.value = progress
            }
            val finalStep = _step.value
            if (finalStep is AppUpdateStep.ReadyToInstall) {
                _pendingAutoInstall.value = finalStep.apkPath
            }
        }
    }

    /** Returns the pending auto-install path once, or null if none (or already consumed). */
    fun consumePendingAutoInstall(): String? = _pendingAutoInstall.getAndUpdate { null }

    /** Opens the package installer if a verified APK is ready. Returns false otherwise. */
    fun install(activityContext: Context): Boolean {
        val ready = _step.value as? AppUpdateStep.ReadyToInstall ?: return false
        apkDownloader.install(activityContext, ready.apkPath)
        return true
    }

    /** Cancels any running download, forgets the result and removes downloaded files. */
    fun cancelAndReset() {
        downloadJob?.cancel()
        downloadJob = null
        _pendingAutoInstall.value = null
        _step.value = AppUpdateStep.Idle
        appCoroutineScope.launch(coroutineDispatchers.io) {
            apkDownloader.deleteDownloads()
        }
    }

    /** Removes APKs left by a previous (installed or stale) update; no-op while one is in use. */
    fun cleanupStaleDownloads() {
        if (_step.value !is AppUpdateStep.Idle) return
        appCoroutineScope.launch(coroutineDispatchers.io) {
            apkDownloader.cleanupStaleDownloads()
        }
    }
}
