/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class TypingNotificationStateForMessagesProvider : PreviewParameterProvider<TypingNotificationState> {
    override val values: Sequence<TypingNotificationState>
        get() = sequenceOf(
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice"),
                    aTypingRoomMember(displayName = "Bob"),
                ),
            ),
            aTypingNotificationState(
                    typingMembers = listOf(aTypingRoomMember()),
                    reserveSpace = true
            ),
            aTypingNotificationState(reserveSpace = true),
        )
}
