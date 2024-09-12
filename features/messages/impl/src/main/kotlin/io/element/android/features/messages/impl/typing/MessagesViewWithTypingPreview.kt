/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.MessagesView
import io.element.android.features.messages.impl.aMessagesState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@PreviewsDayNight
@Composable
internal fun MessagesViewWithTypingPreview(
    @PreviewParameter(TypingNotificationStateForMessagesProvider::class) typingState: TypingNotificationState
) = ElementPreview {
    MessagesView(
        state = aMessagesState().copy(typingNotificationState = typingState),
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventClick = { false },
        onUserDataClick = {},
        onLinkClick = {},
        onPreviewAttachments = {},
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = {},
    )
}
