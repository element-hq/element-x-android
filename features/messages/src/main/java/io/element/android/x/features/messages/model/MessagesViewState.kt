/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.messages.model

import androidx.compose.runtime.Stable
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.textcomposer.MessageComposerMode

@Stable
data class MessagesViewState(
    val roomId: String,
    val roomName: String? = null,
    val roomAvatar: AvatarData? = null,
    val timelineItems: Async<List<MessagesTimelineItemState>> = Uninitialized,
    val hasMoreToLoad: Boolean = true,
    val itemActionsSheetState: Async<MessagesItemActionsSheetState> = Uninitialized,
    val snackbarContent: String? = null,
    val highlightedEventId: String? = null,
    val composerMode: MessageComposerMode = MessageComposerMode.Normal(""),
) : MavericksState {

    @Suppress("unused")
    constructor(roomId: String) : this(
        roomId = roomId,
        roomName = null,
        roomAvatar = null
    )
}
