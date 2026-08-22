/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import androidx.compose.runtime.Immutable
import io.element.android.features.appupdate.api.AppUpdateStep

enum class PrivatePushPage {
    Why,
    Install,
    Configure,
    Connect,
    Done,
}

@Immutable
sealed interface ConnectState {
    data object Idle : ConnectState
    data object Connecting : ConnectState
    data object Connected : ConnectState
    data class Problem(val problem: ConnectProblem) : ConnectState
}

@Immutable
sealed interface ConnectProblem {
    data object NtfyNotInstalled : ConnectProblem
    data class WrongServer(val host: String) : ConnectProblem
    data class RegistrationFailed(val reason: String?) : ConnectProblem
}

data class PrivatePushState(
    val page: PrivatePushPage,
    /** Address to paste into ntfy. */
    val serverAddress: String,
    val ntfyInstalled: Boolean,
    val playStoreAvailable: Boolean,
    val fdroidAvailable: Boolean,
    /** In-app download of the ntfy APK from feralisme.fr. */
    val download: AppUpdateStep,
    val connect: ConnectState,
    /** Hint shown on the Configure page after a failed verification (endpoint on another server). */
    val wrongServerHost: String?,
    val addressCopied: Boolean,
    /** The member already said "Later" once: offer to stop re-showing the flow on the push error. */
    val canStopAsking: Boolean,
    val eventSink: (PrivatePushEvents) -> Unit,
)
