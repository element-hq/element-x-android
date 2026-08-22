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
 * State of the Feral "update available" banner (in-app updater).
 * `update == null` means nothing to show.
 */
data class AppUpdateBannerState(
    val update: AvailableUpdate?,
    val step: AppUpdateStep,
    val eventSink: (AppUpdateBannerEvents) -> Unit,
)

/** A newer Feral release, resolved for this device's ABI. */
@Immutable
data class AvailableUpdate(
    val versionName: String,
    val versionCode: Long,
    val url: String,
    val sha256: String,
)

@Immutable
sealed interface AppUpdateStep {
    data object Idle : AppUpdateStep
    data class Downloading(val percent: Int?) : AppUpdateStep
    data class ReadyToInstall(val apkPath: String) : AppUpdateStep
    data object Failed : AppUpdateStep
}

fun anAppUpdateBannerState(
    update: AvailableUpdate? = null,
    step: AppUpdateStep = AppUpdateStep.Idle,
    eventSink: (AppUpdateBannerEvents) -> Unit = {},
) = AppUpdateBannerState(
    update = update,
    step = step,
    eventSink = eventSink,
)

fun anAvailableUpdate(
    versionName: String = "26.08.1",
    versionCode: Long = 1L,
    url: String = "https://feralisme.fr/media/downloads/android/Feral-26.08.1-arm64-v8a.apk",
    sha256: String = "0000000000000000000000000000000000000000000000000000000000000000",
) = AvailableUpdate(
    versionName = versionName,
    versionCode = versionCode,
    url = url,
    sha256 = sha256,
)
