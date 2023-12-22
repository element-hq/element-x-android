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

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuState
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId

@Immutable
data class MessagesState(
    val roomId: RoomId,
    val roomName: Async<String>,
    val roomAvatar: Async<AvatarData>,
    val userHasPermissionToSendMessage: Boolean,
    val userHasPermissionToRedact: Boolean,
    val composerState: MessageComposerState,
    val voiceMessageComposerState: VoiceMessageComposerState,
    val timelineState: TimelineState,
    val actionListState: ActionListState,
    val customReactionState: CustomReactionState,
    val reactionSummaryState: ReactionSummaryState,
    val retrySendMenuState: RetrySendMenuState,
    val readReceiptBottomSheetState: ReadReceiptBottomSheetState,
    val hasNetworkConnection: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val inviteProgress: Async<Unit>,
    val showReinvitePrompt: Boolean,
    val enableTextFormatting: Boolean,
    val enableVoiceMessages: Boolean,
    val callState: RoomCallState,
    val appName: String,
    val eventSink: (MessagesEvents) -> Unit
)

enum class RoomCallState {
    ENABLED,
    ONGOING,
    DISABLED
}
