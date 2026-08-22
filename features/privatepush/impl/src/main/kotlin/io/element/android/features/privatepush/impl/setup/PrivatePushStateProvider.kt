/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.appupdate.api.AppUpdateStep

open class PrivatePushStateProvider : PreviewParameterProvider<PrivatePushState> {
    override val values: Sequence<PrivatePushState>
        get() = sequenceOf(
            aPrivatePushState(),
            aPrivatePushState(page = PrivatePushPage.Why, ntfyInstalled = true),
            aPrivatePushState(page = PrivatePushPage.Install),
            aPrivatePushState(page = PrivatePushPage.Install, playStoreAvailable = false, fdroidAvailable = false),
            aPrivatePushState(page = PrivatePushPage.Install, download = AppUpdateStep.Downloading(percent = null)),
            aPrivatePushState(page = PrivatePushPage.Install, download = AppUpdateStep.Downloading(percent = 42)),
            aPrivatePushState(page = PrivatePushPage.Install, download = AppUpdateStep.ReadyToInstall("/cache/updates/ntfy.apk")),
            aPrivatePushState(page = PrivatePushPage.Install, download = AppUpdateStep.Failed),
            aPrivatePushState(page = PrivatePushPage.Install, ntfyInstalled = true),
            aPrivatePushState(page = PrivatePushPage.Configure, ntfyInstalled = true),
            aPrivatePushState(page = PrivatePushPage.Configure, ntfyInstalled = true, addressCopied = true),
            aPrivatePushState(page = PrivatePushPage.Configure, ntfyInstalled = true, wrongServerHost = "ntfy.sh"),
            aPrivatePushState(page = PrivatePushPage.Connect, ntfyInstalled = true),
            aPrivatePushState(page = PrivatePushPage.Connect, ntfyInstalled = true, connect = ConnectState.Connecting),
            aPrivatePushState(page = PrivatePushPage.Connect, connect = ConnectState.Problem(ConnectProblem.NtfyNotInstalled)),
            aPrivatePushState(page = PrivatePushPage.Connect, ntfyInstalled = true, connect = ConnectState.Problem(ConnectProblem.WrongServer("ntfy.sh"))),
            aPrivatePushState(page = PrivatePushPage.Connect, ntfyInstalled = true, connect = ConnectState.Problem(ConnectProblem.RegistrationFailed("timeout"))),
            aPrivatePushState(page = PrivatePushPage.Connect, ntfyInstalled = true, connect = ConnectState.Problem(ConnectProblem.RegistrationFailed(null))),
            aPrivatePushState(page = PrivatePushPage.Done, ntfyInstalled = true, connect = ConnectState.Connected),
        )
}

fun aPrivatePushState(
    page: PrivatePushPage = PrivatePushPage.Why,
    serverAddress: String = PrivatePushConfig.SERVER_URL,
    ntfyInstalled: Boolean = false,
    playStoreAvailable: Boolean = true,
    fdroidAvailable: Boolean = true,
    download: AppUpdateStep = AppUpdateStep.Idle,
    connect: ConnectState = ConnectState.Idle,
    wrongServerHost: String? = null,
    addressCopied: Boolean = false,
    eventSink: (PrivatePushEvents) -> Unit = {},
) = PrivatePushState(
    page = page,
    serverAddress = serverAddress,
    ntfyInstalled = ntfyInstalled,
    playStoreAvailable = playStoreAvailable,
    fdroidAvailable = fdroidAvailable,
    download = download,
    connect = connect,
    wrongServerHost = wrongServerHost,
    addressCopied = addressCopied,
    eventSink = eventSink,
)
