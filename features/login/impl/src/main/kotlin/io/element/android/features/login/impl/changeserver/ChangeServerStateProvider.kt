/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.AsyncData

open class ChangeServerStateProvider : PreviewParameterProvider<ChangeServerState> {
    override val values: Sequence<ChangeServerState>
        get() = sequenceOf(
            aChangeServerState(),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.Error(null))),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.SlidingSyncAlert)),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.UnauthorizedAccountProvider(
                        unauthorisedAccountProviderTitle = "example.com",
                        authorisedAccountProviderTitles = listOf("element.io", "element.org"),
                    )
                )
            ),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.NeedElementPro(
                        unauthorisedAccountProviderTitle = "example.com",
                        applicationId = "applicationId",
                    ),
                )
            ),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.UnsupportedServer
                )
            ),
        )
}

fun aChangeServerState(
    changeServerAction: AsyncData<Unit> = AsyncData.Uninitialized,
) = ChangeServerState(
    changeServerAction = changeServerAction,
    eventSink = {}
)
