/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class LoggedInStateProvider : PreviewParameterProvider<LoggedInState> {
    override val values: Sequence<LoggedInState>
        get() = sequenceOf(
            aLoggedInState(),
            aLoggedInState(showSyncSpinner = true),
            aLoggedInState(pusherRegistrationState = AsyncData.Failure(PusherRegistrationFailure.NoDistributorsAvailable())),
            aLoggedInState(forceNativeSlidingSyncMigration = true),
        )
}

fun aLoggedInState(
    showSyncSpinner: Boolean = false,
    pusherRegistrationState: AsyncData<Unit> = AsyncData.Uninitialized,
    forceNativeSlidingSyncMigration: Boolean = false,
) = LoggedInState(
    showSyncSpinner = showSyncSpinner,
    pusherRegistrationState = pusherRegistrationState,
    ignoreRegistrationError = false,
    forceNativeSlidingSyncMigration = forceNativeSlidingSyncMigration,
    eventSink = {},
)
