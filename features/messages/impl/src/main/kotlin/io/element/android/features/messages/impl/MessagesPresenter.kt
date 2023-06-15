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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuPresenter
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.handleSnackbarMessage
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
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
    private val customReactionPresenter: CustomReactionPresenter,
    private val retrySendMenuPresenter: RetrySendMenuPresenter,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val messageSummaryFormatter: MessageSummaryFormatter,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<MessagesState> {

    @Composable
    override fun present(): MessagesState {
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val timelineState = timelinePresenter.present()
        val actionListState = actionListPresenter.present()
        val customReactionState = customReactionPresenter.present()
        val retryState = retrySendMenuPresenter.present()

        val syncUpdateFlow = room.syncUpdateFlow().collectAsState(0L)
        val roomName: MutableState<String?> = rememberSaveable {
            mutableStateOf(null)
        }
        val roomAvatar: MutableState<AvatarData?> = remember {
            mutableStateOf(null)
        }

        val networkConnectionStatus by networkMonitor.connectivity.collectAsState(initial = networkMonitor.currentConnectivityStatus)

        val snackbarMessage = handleSnackbarMessage(snackbarDispatcher)

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
                is MessagesEvents.HandleAction -> {
                    localCoroutineScope.handleTimelineAction(event.action, event.event, composerState)
                }
                is MessagesEvents.SendReaction -> {
                    localCoroutineScope.sendReaction(event.emoji, event.eventId)
                }
                is MessagesEvents.Dismiss -> actionListState.eventSink(ActionListEvents.Clear)
            }
        }
        return MessagesState(
            roomId = room.roomId,
            roomName = roomName.value,
            roomAvatar = roomAvatar.value,
            composerState = composerState,
            timelineState = timelineState,
            actionListState = actionListState,
            customReactionState = customReactionState,
            retrySendMenuState = retryState,
            hasNetworkConnection = networkConnectionStatus == NetworkStatus.Online,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> notImplementedYet()
            TimelineItemAction.Forward -> notImplementedYet()
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState)
            TimelineItemAction.Reply -> handleActionReply(targetEvent, composerState)
            TimelineItemAction.Developer -> Unit // Handled at UI level
            TimelineItemAction.ReportContent -> notImplementedYet()
        }
    }

    private fun CoroutineScope.sendReaction(
        emoji: String,
        eventId: EventId,
    ) = launch(dispatchers.io) {
        room.sendReaction(emoji, eventId)
            .onFailure { Timber.e(it) }
    }

    private fun notImplementedYet() {
        Timber.v("NotImplementedYet")
    }

    private suspend fun handleActionRedact(event: TimelineItem.Event) {
        if (event.eventId == null) return
        room.redactEvent(event.eventId)
    }

    private fun handleActionEdit(targetEvent: TimelineItem.Event, composerState: MessageComposerState) {
        if (targetEvent.eventId == null) return
        val composerMode = MessageComposerMode.Edit(
            targetEvent.eventId,
            (targetEvent.content as? TimelineItemTextBasedContent)?.body.orEmpty()
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }

    private fun handleActionReply(targetEvent: TimelineItem.Event, composerState: MessageComposerState) {
        if (targetEvent.eventId == null) return
        val textContent = messageSummaryFormatter.format(targetEvent)
        val attachmentThumbnailInfo = when (targetEvent.content) {
            is TimelineItemImageContent -> AttachmentThumbnailInfo(
                mediaSource = targetEvent.content.mediaSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Image,
                blurHash = targetEvent.content.blurhash,
            )
            is TimelineItemVideoContent -> AttachmentThumbnailInfo(
                mediaSource = targetEvent.content.thumbnailSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Video,
                blurHash = targetEvent.content.blurHash,
            )
            is TimelineItemFileContent -> AttachmentThumbnailInfo(
                mediaSource = targetEvent.content.thumbnailSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.File,
                blurHash = null,
            )
            is TimelineItemTextBasedContent,
            is TimelineItemRedactedContent,
            is TimelineItemStateContent,
            is TimelineItemEncryptedContent,
            is TimelineItemUnknownContent -> null
        }
        val composerMode = MessageComposerMode.Reply(
            senderName = targetEvent.safeSenderName,
            eventId = targetEvent.eventId,
            attachmentThumbnailInfo = attachmentThumbnailInfo,
            defaultContent = textContent,
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }
}
