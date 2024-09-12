/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.forward

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

open class ForwardMessagesStateProvider : PreviewParameterProvider<ForwardMessagesState> {
    override val values: Sequence<ForwardMessagesState>
        get() = sequenceOf(
            aForwardMessagesState(),
            aForwardMessagesState(
                forwardAction = AsyncAction.Loading,
            ),
            aForwardMessagesState(
                forwardAction = AsyncAction.Success(
                    listOf(RoomId("!room2:domain")),
                )
            ),
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(Throwable("error")),
            ),
        )
}

fun aForwardMessagesState(
    forwardAction: AsyncAction<List<RoomId>> = AsyncAction.Uninitialized,
    eventSink: (ForwardMessagesEvents) -> Unit = {}
) = ForwardMessagesState(
    forwardAction = forwardAction,
    eventSink = eventSink
)
