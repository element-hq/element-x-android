/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetailsProvider
import io.element.android.libraries.textcomposer.model.MessageComposerMode

class MessageComposerModeSpecialProvider : PreviewParameterProvider<MessageComposerMode.Special> {
    override val values: Sequence<MessageComposerMode.Special> = sequenceOf(
        aMessageComposerModeEdit()
    ) +
        // Keep only 3 values from InReplyToDetailsProvider
        InReplyToDetailsProvider().values.take(3).map {
            aMessageComposerModeReply(
                replyToDetails = it
            )
        }
}
