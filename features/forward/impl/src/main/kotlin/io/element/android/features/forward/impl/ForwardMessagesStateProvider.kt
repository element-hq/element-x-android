/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.forward.impl

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
                forwardAction = AsyncAction.Failure(RuntimeException("error")),
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
