/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.forward

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId

@Composable
fun ForwardMessagesView(
    state: ForwardMessagesState,
    onForwardSuccess: (List<RoomId>) -> Unit,
) {
    AsyncActionView(
        async = state.forwardAction,
        onSuccess = {
            onForwardSuccess(it)
        },
        onErrorDismiss = {
            state.eventSink(ForwardMessagesEvents.ClearError)
        },
    )
}

@PreviewsDayNight
@Composable
internal fun ForwardMessagesViewPreview(@PreviewParameter(ForwardMessagesStateProvider::class) state: ForwardMessagesState) = ElementPreview {
    ForwardMessagesView(
        state = state,
        onForwardSuccess = {}
    )
}
