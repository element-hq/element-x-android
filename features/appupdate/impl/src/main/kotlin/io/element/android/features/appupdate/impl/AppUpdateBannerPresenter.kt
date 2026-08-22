/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.appupdate.api.AppUpdateBannerEvents
import io.element.android.features.appupdate.api.AppUpdateBannerState
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.appupdate.api.AvailableUpdate
import io.element.android.libraries.architecture.Presenter

/**
 * Presents the "update available" banner. The download itself is owned by the
 * app-scoped [AppUpdateManager]; this presenter only mirrors its state and forwards
 * user intents, so navigating away from the room list never interrupts a download.
 *
 * ⚠ This presenter runs inside Molecule (HomeFlowNode.launchMolecule), NOT in a Compose UI
 * composition: CompositionLocals such as LocalContext are NOT available here and reading
 * them throws at runtime (it crashed 26.08.0 right after sign-in). Everything needing a
 * Context lives in the injected manager/downloader.
 */
@Inject
class AppUpdateBannerPresenter(
    private val appUpdateChecker: AppUpdateChecker,
    private val appUpdateManager: AppUpdateManager,
) : Presenter<AppUpdateBannerState> {
    @Composable
    override fun present(): AppUpdateBannerState {
        var availableUpdate by remember { mutableStateOf<AvailableUpdate?>(null) }
        val step by appUpdateManager.step.collectAsState()
        val pendingAutoInstall by appUpdateManager.pendingAutoInstall.collectAsState()

        LaunchedEffect(Unit) {
            appUpdateManager.cleanupStaleDownloads()
            availableUpdate = appUpdateChecker.checkForUpdate()
        }

        // A download that completes while the banner is on screen opens the installer once.
        LaunchedEffect(pendingAutoInstall) {
            if (pendingAutoInstall != null && appUpdateManager.consumePendingAutoInstall() != null) {
                appUpdateManager.install()
            }
        }

        fun handleEvents(event: AppUpdateBannerEvents) {
            when (event) {
                AppUpdateBannerEvents.StartUpdate -> {
                    val update = availableUpdate ?: return
                    when (step) {
                        is AppUpdateStep.Downloading -> Unit
                        is AppUpdateStep.ReadyToInstall -> appUpdateManager.install()
                        AppUpdateStep.Idle,
                        AppUpdateStep.Failed -> appUpdateManager.startDownload(update)
                    }
                }
            }
        }

        return AppUpdateBannerState(
            update = availableUpdate,
            step = step,
            eventSink = ::handleEvents,
        )
    }
}
