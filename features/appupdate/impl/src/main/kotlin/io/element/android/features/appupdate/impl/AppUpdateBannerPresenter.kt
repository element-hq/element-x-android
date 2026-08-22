/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.zacsweers.metro.Inject
import io.element.android.features.appupdate.api.AppUpdateBannerEvents
import io.element.android.features.appupdate.api.AppUpdateBannerState
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.appupdate.api.AvailableUpdate
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch

@Inject
class AppUpdateBannerPresenter(
    private val appUpdateChecker: AppUpdateChecker,
    private val apkDownloader: ApkDownloader,
) : Presenter<AppUpdateBannerState> {
    @Composable
    override fun present(): AppUpdateBannerState {
        val activityContext = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var availableUpdate by remember { mutableStateOf<AvailableUpdate?>(null) }
        var step by remember { mutableStateOf<AppUpdateStep>(AppUpdateStep.Idle) }

        LaunchedEffect(Unit) {
            availableUpdate = appUpdateChecker.checkForUpdate()
        }

        fun handleEvents(event: AppUpdateBannerEvents) {
            when (event) {
                AppUpdateBannerEvents.StartUpdate -> {
                    val update = availableUpdate ?: return
                    when (val currentStep = step) {
                        is AppUpdateStep.Downloading -> Unit
                        is AppUpdateStep.ReadyToInstall ->
                            apkDownloader.install(activityContext, currentStep.apkPath)
                        AppUpdateStep.Idle,
                        AppUpdateStep.Failed -> coroutineScope.launch {
                            apkDownloader.downloadAndVerify(update).collect { progress ->
                                step = progress
                            }
                            val finalStep = step
                            if (finalStep is AppUpdateStep.ReadyToInstall) {
                                apkDownloader.install(activityContext, finalStep.apkPath)
                            }
                        }
                    }
                }
                AppUpdateBannerEvents.Dismiss -> {
                    val update = availableUpdate
                    availableUpdate = null
                    if (update != null) {
                        coroutineScope.launch {
                            appUpdateChecker.ignoreVersion(update.versionCode)
                        }
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
