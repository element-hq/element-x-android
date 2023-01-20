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

package io.element.android.x.features.messages

import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.actionlist.ActionListState
import io.element.android.x.features.messages.textcomposer.MessageComposerState
import io.element.android.x.features.messages.timeline.TimelineState
import io.element.android.x.matrix.core.RoomId

@Immutable
data class MessagesState(
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatar: AvatarData?,
    val composerState: MessageComposerState,
    val timelineState: TimelineState,
    val actionListState: ActionListState,
    val eventSink: (MessagesEvents) -> Unit
)
