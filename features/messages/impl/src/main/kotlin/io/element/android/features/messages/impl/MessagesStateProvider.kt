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
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionEvents
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryEvents
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvents
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuState
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.aRetrySendMenuState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessagePreviewState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.textcomposer.aRichTextEditorState
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

open class MessagesStateProvider : PreviewParameterProvider<MessagesState> {
    override val values: Sequence<MessagesState>
        get() = sequenceOf(
            aMessagesState(),
            aMessagesState(hasNetworkConnection = false),
            aMessagesState(composerState = aMessageComposerState(showAttachmentSourcePicker = true)),
            aMessagesState(userHasPermissionToSendMessage = false),
            aMessagesState(showReinvitePrompt = true),
            aMessagesState(
                roomName = AsyncData.Uninitialized,
                roomAvatar = AsyncData.Uninitialized,
            ),
            aMessagesState(composerState = aMessageComposerState(showTextFormatting = true)),
            aMessagesState(
                enableVoiceMessages = true,
                voiceMessageComposerState = aVoiceMessageComposerState(showPermissionRationaleDialog = true),
            ),
            aMessagesState(
                composerState = aMessageComposerState(
                    attachmentsState = AttachmentsState.Sending.Processing(persistentListOf())
                ),
            ),
            aMessagesState(
                composerState = aMessageComposerState(
                    attachmentsState = AttachmentsState.Sending.Uploading(0.33f)
                ),
            ),
            aMessagesState(
                callState = RoomCallState.ONGOING,
            ),
            aMessagesState(
                enableVoiceMessages = true,
                voiceMessageComposerState = aVoiceMessageComposerState(
                    voiceMessageState = aVoiceMessagePreviewState(),
                    showSendFailureDialog = true
                ),
            ),
            aMessagesState(
                callState = RoomCallState.DISABLED,
            ),
        )
}

fun aMessagesState(
    roomName: AsyncData<String> = AsyncData.Success("Room name"),
    roomAvatar: AsyncData<AvatarData> = AsyncData.Success(AvatarData("!id:domain", "Room name", size = AvatarSize.TimelineRoom)),
    userHasPermissionToSendMessage: Boolean = true,
    userHasPermissionToRedactOwn: Boolean = false,
    userHasPermissionToRedactOther: Boolean = false,
    userHasPermissionToSendReaction: Boolean = true,
    composerState: MessageComposerState = aMessageComposerState(
        richTextEditorState = aRichTextEditorState(initialText = "Hello", initialFocus = true),
        isFullScreen = false,
        mode = MessageComposerMode.Normal,
    ),
    voiceMessageComposerState: VoiceMessageComposerState = aVoiceMessageComposerState(),
    timelineState: TimelineState = aTimelineState(
        timelineItems = aTimelineItemList(aTimelineItemTextContent()),
    ),
    retrySendMenuState: RetrySendMenuState = aRetrySendMenuState(),
    readReceiptBottomSheetState: ReadReceiptBottomSheetState = aReadReceiptBottomSheetState(),
    actionListState: ActionListState = anActionListState(),
    customReactionState: CustomReactionState = aCustomReactionState(),
    reactionSummaryState: ReactionSummaryState = aReactionSummaryState(),
    hasNetworkConnection: Boolean = true,
    showReinvitePrompt: Boolean = false,
    enableVoiceMessages: Boolean = true,
    callState: RoomCallState = RoomCallState.ENABLED,
    eventSink: (MessagesEvents) -> Unit = {},
) = MessagesState(
    roomId = RoomId("!id:domain"),
    roomName = roomName,
    roomAvatar = roomAvatar,
    userHasPermissionToSendMessage = userHasPermissionToSendMessage,
    userHasPermissionToRedactOwn = userHasPermissionToRedactOwn,
    userHasPermissionToRedactOther = userHasPermissionToRedactOther,
    userHasPermissionToSendReaction = userHasPermissionToSendReaction,
    composerState = composerState,
    voiceMessageComposerState = voiceMessageComposerState,
    typingNotificationState = aTypingNotificationState(),
    timelineState = timelineState,
    retrySendMenuState = retrySendMenuState,
    readReceiptBottomSheetState = readReceiptBottomSheetState,
    actionListState = actionListState,
    customReactionState = customReactionState,
    reactionSummaryState = reactionSummaryState,
    hasNetworkConnection = hasNetworkConnection,
    snackbarMessage = null,
    inviteProgress = AsyncData.Uninitialized,
    showReinvitePrompt = showReinvitePrompt,
    enableTextFormatting = true,
    enableVoiceMessages = enableVoiceMessages,
    callState = callState,
    appName = "Element",
    eventSink = eventSink,
)

fun aReactionSummaryState(
    target: ReactionSummaryState.Summary? = null,
    eventSink: (ReactionSummaryEvents) -> Unit = {}
) = ReactionSummaryState(
    target = target,
    eventSink = eventSink,
)

fun aCustomReactionState(
    target: CustomReactionState.Target = CustomReactionState.Target.None,
    eventSink: (CustomReactionEvents) -> Unit = {},
) = CustomReactionState(
    target = target,
    selectedEmoji = persistentSetOf(),
    eventSink = eventSink,
)

fun aReadReceiptBottomSheetState(
    selectedEvent: TimelineItem.Event? = null,
    eventSink: (ReadReceiptBottomSheetEvents) -> Unit = {},
) = ReadReceiptBottomSheetState(
    selectedEvent = selectedEvent,
    eventSink = eventSink,
)
