/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.share.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

open class ShareStateProvider : PreviewParameterProvider<ShareState> {
    override val values: Sequence<ShareState>
        get() = sequenceOf(
            aShareState(),
            aShareState(
                shareAction = AsyncAction.Loading,
            ),
            aShareState(
                shareAction = AsyncAction.Success(
                    listOf(RoomId("!room2:domain")),
                )
            ),
            aShareState(
                shareAction = AsyncAction.Failure(Throwable("error")),
            ),
        )
}

fun aShareState(
    shareAction: AsyncAction<List<RoomId>> = AsyncAction.Uninitialized,
    eventSink: (ShareEvents) -> Unit = {}
) = ShareState(
    shareAction = shareAction,
    eventSink = eventSink
)
