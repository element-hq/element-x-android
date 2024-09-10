/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings

open class ChangeServerStateProvider : PreviewParameterProvider<ChangeServerState> {
    override val values: Sequence<ChangeServerState>
        get() = sequenceOf(
            aChangeServerState(),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.Error(CommonStrings.error_unknown))),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.SlidingSyncAlert)),
        )
}

fun aChangeServerState(
    changeServerAction: AsyncData<Unit> = AsyncData.Uninitialized,
) = ChangeServerState(
    changeServerAction = changeServerAction,
    eventSink = {}
)
