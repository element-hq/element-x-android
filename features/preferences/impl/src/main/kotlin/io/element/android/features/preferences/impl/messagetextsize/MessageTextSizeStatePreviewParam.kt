/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.preferences.api.store.MessageTextSize

class MessageTextSizeStatePreviewParam : PreviewParameterProvider<MessageTextSizeState> {
    override val values: Sequence<MessageTextSizeState>
        get() = sequenceOf(
            aMessageTextSizeState(),
            aMessageTextSizeState(textSizeSp = MessageTextSize.MIN),
            aMessageTextSizeState(textSizeSp = MessageTextSize.MAX),
        )
}

fun aMessageTextSizeState(
    textSizeSp: Int = MessageTextSize.DEFAULT,
    eventSink: (MessageTextSizeEvent) -> Unit = {},
) = MessageTextSizeState(
    textSizeSp = textSizeSp,
    eventSink = eventSink,
)
