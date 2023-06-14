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
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuState
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.textcomposer.MessageComposerMode

open class MessagesStateProvider : PreviewParameterProvider<MessagesState> {
    override val values: Sequence<MessagesState>
        get() = sequenceOf(
            aMessagesState(),
            aMessagesState().copy(hasNetworkConnection = false),
            aMessagesState().copy(composerState = aMessageComposerState().copy(showAttachmentSourcePicker = true)),
        )
}

fun aMessagesState() = MessagesState(
    roomId = RoomId("!id:domain"),
    roomName = "Room name",
    roomAvatar = AvatarData("!id:domain", "Room name"),
    composerState = aMessageComposerState().copy(
        text = StableCharSequence("Hello"),
        isFullScreen = false,
        mode = MessageComposerMode.Normal("Hello"),
    ),
    timelineState = aTimelineState().copy(
        timelineItems = aTimelineItemList(aTimelineItemTextContent()),
    ),
    retrySendMenuState = RetrySendMenuState(
        selectedEvent = null,
        eventSink = {},
    ),
    actionListState = anActionListState(),
    hasNetworkConnection = true,
    snackbarMessage = null,
    eventSink = {}
)
