/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuState
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessagePreviewState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.wysiwyg.compose.RichTextEditorState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

open class MessagesStateProvider : PreviewParameterProvider<MessagesState> {
    override val values: Sequence<MessagesState>
        get() = sequenceOf(
            aMessagesState(),
            aMessagesState().copy(hasNetworkConnection = false),
            aMessagesState().copy(composerState = aMessageComposerState().copy(showAttachmentSourcePicker = true)),
            aMessagesState().copy(userHasPermissionToSendMessage = false),
            aMessagesState().copy(showReinvitePrompt = true),
            aMessagesState().copy(
                roomName = AsyncData.Uninitialized,
                roomAvatar = AsyncData.Uninitialized,
            ),
            aMessagesState().copy(composerState = aMessageComposerState().copy(showTextFormatting = true)),
            aMessagesState().copy(
                enableVoiceMessages = true,
                voiceMessageComposerState = aVoiceMessageComposerState(showPermissionRationaleDialog = true),
            ),
            aMessagesState().copy(
                composerState = aMessageComposerState().copy(
                    attachmentsState = AttachmentsState.Sending.Processing(persistentListOf())
                ),
            ),
            aMessagesState().copy(
                composerState = aMessageComposerState().copy(
                    attachmentsState = AttachmentsState.Sending.Uploading(0.33f)
                ),
            ),
            aMessagesState().copy(
                callState = RoomCallState.ONGOING,
            ),
            aMessagesState().copy(
                enableVoiceMessages = true,
                voiceMessageComposerState = aVoiceMessageComposerState(
                    voiceMessageState = aVoiceMessagePreviewState(),
                    showSendFailureDialog = true
                ),
            ),
            aMessagesState().copy(
                callState = RoomCallState.DISABLED,
            ),
        )
}

fun aMessagesState() = MessagesState(
    roomId = RoomId("!id:domain"),
    roomName = AsyncData.Success("Room name"),
    roomAvatar = AsyncData.Success(AvatarData("!id:domain", "Room name", size = AvatarSize.TimelineRoom)),
    userHasPermissionToSendMessage = true,
    userHasPermissionToRedact = false,
    userHasPermissionToSendReaction = true,
    composerState = aMessageComposerState().copy(
        richTextEditorState = RichTextEditorState("Hello", initialFocus = true),
        isFullScreen = false,
        mode = MessageComposerMode.Normal,
    ),
    voiceMessageComposerState = aVoiceMessageComposerState(),
    timelineState = aTimelineState().copy(
        timelineItems = aTimelineItemList(aTimelineItemTextContent()),
    ),
    retrySendMenuState = RetrySendMenuState(
        selectedEvent = null,
        eventSink = {},
    ),
    readReceiptBottomSheetState = ReadReceiptBottomSheetState(
        selectedEvent = null,
        eventSink = {},
    ),
    actionListState = anActionListState(),
    customReactionState = CustomReactionState(
        target = CustomReactionState.Target.None,
        eventSink = {},
        selectedEmoji = persistentSetOf(),
    ),
    reactionSummaryState = ReactionSummaryState(
        target = null,
        eventSink = {},
    ),
    hasNetworkConnection = true,
    snackbarMessage = null,
    inviteProgress = AsyncData.Uninitialized,
    showReinvitePrompt = false,
    enableTextFormatting = true,
    enableVoiceMessages = true,
    callState = RoomCallState.ENABLED,
    appName = "Element",
    eventSink = {}
)
