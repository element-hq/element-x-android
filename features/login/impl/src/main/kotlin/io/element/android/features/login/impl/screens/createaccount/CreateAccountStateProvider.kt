/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.SessionId

open class CreateAccountStateProvider : PreviewParameterProvider<CreateAccountState> {
    override val values: Sequence<CreateAccountState>
        get() = sequenceOf(
            aCreateAccountState(),
            aCreateAccountState(pageProgress = 33),
            aCreateAccountState(createAction = AsyncAction.Loading),
            aCreateAccountState(createAction = AsyncAction.Failure(Throwable("Failed to create account"))),
        )
}

private fun aCreateAccountState(
    pageProgress: Int = 100,
    createAction: AsyncAction<SessionId> = AsyncAction.Uninitialized,
) = CreateAccountState(
    url = "https://example.com",
    isDebugBuild = true,
    pageProgress = pageProgress,
    createAction = createAction,
    eventSink = {}
)
