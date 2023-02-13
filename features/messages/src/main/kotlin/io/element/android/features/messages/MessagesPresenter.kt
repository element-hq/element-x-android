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

package io.element.android.features.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.messages.actionlist.ActionListPresenter
import io.element.android.features.messages.actionlist.model.TimelineItemAction
import io.element.android.features.messages.textcomposer.MessageComposerEvents
import io.element.android.features.messages.textcomposer.MessageComposerPresenter
import io.element.android.features.messages.textcomposer.MessageComposerState
import io.element.android.features.messages.timeline.TimelineEvents
import io.element.android.features.messages.timeline.TimelinePresenter
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.content.TimelineItemTextBasedContent
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MessagesPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val composerPresenter: MessageComposerPresenter,
    private val timelinePresenter: TimelinePresenter,
    private val actionListPresenter: ActionListPresenter,
) : Presenter<MessagesState> {

    @Composable
    override fun present(): MessagesState {
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val timelineState = timelinePresenter.present()
        val actionListState = actionListPresenter.present()

        val syncUpdateFlow = room.syncUpdateFlow().collectAsState(0L)
        val roomName: MutableState<String?> = rememberSaveable {
            mutableStateOf(null)
        }
        val roomAvatar: MutableState<AvatarData?> = remember {
            mutableStateOf(null)
        }
        LaunchedEffect(syncUpdateFlow) {
            roomAvatar.value =
                AvatarData(
                    id = room.roomId.value,
                    name = room.name,
                    url = room.avatarUrl,
                    size = AvatarSize.SMALL
                )
            roomName.value = room.name
        }
        LaunchedEffect(composerState.mode.relatedEventId) {
            timelineState.eventSink(TimelineEvents.SetHighlightedEvent(composerState.mode.relatedEventId))
        }
        fun handleEvents(event: MessagesEvents) {
            when (event) {
                is MessagesEvents.HandleAction -> localCoroutineScope.handleTimelineAction(event.action, event.messageEvent, composerState)
            }
        }
        return MessagesState(
            roomId = room.roomId,
            roomName = roomName.value,
            roomAvatar = roomAvatar.value,
            composerState = composerState,
            timelineState = timelineState,
            actionListState = actionListState,
            eventSink = ::handleEvents
        )
    }

    fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.MessageEvent,
        composerState: MessageComposerState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> notImplementedYet()
            TimelineItemAction.Forward -> notImplementedYet()
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState)
            TimelineItemAction.Reply -> handleActionReply(targetEvent, composerState)
        }
    }

    private fun notImplementedYet() {
        Timber.v("NotImplementedYet")
    }

    private suspend fun handleActionRedact(event: TimelineItem.MessageEvent) {
        room.redactEvent(event.id)
    }

    private fun handleActionEdit(targetEvent: TimelineItem.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Edit(
            targetEvent.id,
            (targetEvent.content as? TimelineItemTextBasedContent)?.body.orEmpty()
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }

    private fun handleActionReply(targetEvent: TimelineItem.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Reply(targetEvent.safeSenderName, targetEvent.id, "")
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }
}
