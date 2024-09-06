/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.waitlistscreen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.SessionId

open class WaitListStateProvider : PreviewParameterProvider<WaitListState> {
    override val values: Sequence<WaitListState>
        get() = sequenceOf(
            aWaitListState(loginAction = AsyncData.Uninitialized),
            aWaitListState(loginAction = AsyncData.Loading()),
            aWaitListState(loginAction = AsyncData.Failure(Throwable("error"))),
            aWaitListState(loginAction = AsyncData.Failure(Throwable(message = "IO_ELEMENT_X_WAIT_LIST"))),
            aWaitListState(loginAction = AsyncData.Success(SessionId("@alice:element.io"))),
            // Add other state here
        )
}

fun aWaitListState(
    appName: String = "Element X",
    serverName: String = "server.org",
    loginAction: AsyncData<SessionId> = AsyncData.Uninitialized,
) = WaitListState(
    appName = appName,
    serverName = serverName,
    loginAction = loginAction,
    eventSink = {}
)
