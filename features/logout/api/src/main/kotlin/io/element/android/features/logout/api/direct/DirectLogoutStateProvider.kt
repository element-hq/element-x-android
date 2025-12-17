/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
            aDirectLogoutState(logoutAction = AsyncAction.Success(Unit)),
        )
}

fun aDirectLogoutState(
    canDoDirectSignOut: Boolean = true,
    logoutAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (DirectLogoutEvents) -> Unit = {},
) = DirectLogoutState(
    canDoDirectSignOut = canDoDirectSignOut,
    logoutAction = logoutAction,
    eventSink = eventSink,
)
