/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.api.direct

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class DirectLogoutStateProvider : PreviewParameterProvider<DirectLogoutState> {
    override val values: Sequence<DirectLogoutState>
        get() = sequenceOf(
            aDirectLogoutState(),
            aDirectLogoutState(logoutAction = AsyncAction.ConfirmingNoParams),
            aDirectLogoutState(logoutAction = AsyncAction.Loading),
            aDirectLogoutState(logoutAction = AsyncAction.Failure(Exception("Error"))),
            aDirectLogoutState(logoutAction = AsyncAction.Success("success")),
        )
}

fun aDirectLogoutState(
    canDoDirectSignOut: Boolean = true,
    logoutAction: AsyncAction<String?> = AsyncAction.Uninitialized,
    eventSink: (DirectLogoutEvents) -> Unit = {},
) = DirectLogoutState(
    canDoDirectSignOut = canDoDirectSignOut,
    logoutAction = logoutAction,
    eventSink = eventSink,
)
