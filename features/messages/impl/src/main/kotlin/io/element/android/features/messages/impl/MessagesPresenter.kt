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

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.appconfig.MessageComposerConfig
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryPresenter
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetPresenter
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuPresenter
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.typing.TypingNotificationPresenter
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatter
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.matrix.ui.room.canCall
import io.element.android.libraries.matrix.ui.room.canRedactOtherAsState
import io.element.android.libraries.matrix.ui.room.canRedactOwnAsState
import io.element.android.libraries.matrix.ui.room.canSendMessageAsState
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessagesPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    private val composerPresenter: MessageComposerPresenter,
    private val voiceMessageComposerPresenter: VoiceMessageComposerPresenter,
    timelinePresenterFactory: TimelinePresenter.Factory,
    private val typingNotificationPresenter: TypingNotificationPresenter,
    private val actionListPresenter: ActionListPresenter,
    private val customReactionPresenter: CustomReactionPresenter,
    private val reactionSummaryPresenter: ReactionSummaryPresenter,
    private val retrySendMenuPresenter: RetrySendMenuPresenter,
    private val readReceiptBottomSheetPresenter: ReadReceiptBottomSheetPresenter,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val messageSummaryFormatter: MessageSummaryFormatter,
    private val dispatchers: CoroutineDispatchers,
    private val clipboardHelper: ClipboardHelper,
    private val featureFlagsService: FeatureFlagService,
    private val htmlConverterProvider: HtmlConverterProvider,
    @Assisted private val navigator: MessagesNavigator,
    private val buildMeta: BuildMeta,
    private val timelineController: TimelineController,
) : Presenter<MessagesState> {
    private val timelinePresenter = timelinePresenterFactory.create(navigator = navigator)

    @AssistedFactory
    interface Factory {
        fun create(navigator: MessagesNavigator): MessagesPresenter
    }

    @Composable
    override fun present(): MessagesState {
        htmlConverterProvider.Update(currentUserId = room.sessionId)

        val roomInfo by room.roomInfoFlow.collectAsState(null)
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val voiceMessageComposerState = voiceMessageComposerPresenter.present()
        val timelineState = timelinePresenter.present()
        val typingNotificationState = typingNotificationPresenter.present()
        val actionListState = actionListPresenter.present()
        val customReactionState = customReactionPresenter.present()
        val reactionSummaryState = reactionSummaryPresenter.present()
        val retryState = retrySendMenuPresenter.present()
        val readReceiptBottomSheetState = readReceiptBottomSheetPresenter.present()

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userHasPermissionToSendMessage by room.canSendMessageAsState(type = MessageEventType.ROOM_MESSAGE, updateKey = syncUpdateFlow.value)
        val userHasPermissionToRedactOwn by room.canRedactOwnAsState(updateKey = syncUpdateFlow.value)
        val userHasPermissionToRedactOther by room.canRedactOtherAsState(updateKey = syncUpdateFlow.value)
        val userHasPermissionToSendReaction by room.canSendMessageAsState(type = MessageEventType.REACTION, updateKey = syncUpdateFlow.value)
        val roomName: AsyncData<String> by remember {
            derivedStateOf { roomInfo?.name?.let { AsyncData.Success(it) } ?: AsyncData.Uninitialized }
        }
        val roomAvatar: AsyncData<AvatarData> by remember {
            derivedStateOf { roomInfo?.avatarData()?.let { AsyncData.Success(it) } ?: AsyncData.Uninitialized }
        }

        var hasDismissedInviteDialog by rememberSaveable {
            mutableStateOf(false)
        }

        val canJoinCall by room.canCall(updateKey = syncUpdateFlow.value)

        LaunchedEffect(Unit) {
            // Remove the unread flag on entering but don't send read receipts
            // as those will be handled by the timeline.
            withContext(dispatchers.io) {
                room.setUnreadFlag(isUnread = false)
            }
        }

        val inviteProgress = remember { mutableStateOf<AsyncData<Unit>>(AsyncData.Uninitialized) }
        var showReinvitePrompt by remember { mutableStateOf(false) }
        LaunchedEffect(hasDismissedInviteDialog, composerState.textEditorState.hasFocus(), syncUpdateFlow.value) {
            withContext(dispatchers.io) {
                showReinvitePrompt = !hasDismissedInviteDialog && composerState.textEditorState.hasFocus() && room.isDirect && room.activeMemberCount == 1L
            }
        }
        val networkConnectionStatus by networkMonitor.connectivity.collectAsState()

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        var enableVoiceMessages by remember { mutableStateOf(false) }
        LaunchedEffect(featureFlagsService) {
            enableVoiceMessages = featureFlagsService.isFeatureEnabled(FeatureFlags.VoiceMessages)
        }

        fun handleEvents(event: MessagesEvents) {
            when (event) {
                is MessagesEvents.HandleAction -> {
                    localCoroutineScope.handleTimelineAction(
                        action = event.action,
                        targetEvent = event.event,
                        composerState = composerState,
                        enableTextFormatting = composerState.showTextFormatting,
                        timelineState = timelineState,
                    )
                }
                is MessagesEvents.ToggleReaction -> {
                    localCoroutineScope.toggleReaction(event.reaction, event.eventId)
                }
                is MessagesEvents.InviteDialogDismissed -> {
                    hasDismissedInviteDialog = true

                    if (event.action == InviteDialogAction.Invite) {
                        localCoroutineScope.reinviteOtherUser(inviteProgress)
                    }
                }
                is MessagesEvents.Dismiss -> actionListState.eventSink(ActionListEvents.Clear)
            }
        }

        val callState = when {
            !canJoinCall -> RoomCallState.DISABLED
            roomInfo?.hasRoomCall == true -> RoomCallState.ONGOING
            else -> RoomCallState.ENABLED
        }

        return MessagesState(
            roomId = room.roomId,
            roomName = roomName,
            roomAvatar = roomAvatar,
            userHasPermissionToSendMessage = userHasPermissionToSendMessage,
            userHasPermissionToRedactOwn = userHasPermissionToRedactOwn,
            userHasPermissionToRedactOther = userHasPermissionToRedactOther,
            userHasPermissionToSendReaction = userHasPermissionToSendReaction,
            composerState = composerState,
            voiceMessageComposerState = voiceMessageComposerState,
            timelineState = timelineState,
            typingNotificationState = typingNotificationState,
            actionListState = actionListState,
            customReactionState = customReactionState,
            reactionSummaryState = reactionSummaryState,
            retrySendMenuState = retryState,
            readReceiptBottomSheetState = readReceiptBottomSheetState,
            hasNetworkConnection = networkConnectionStatus == NetworkStatus.Online,
            snackbarMessage = snackbarMessage,
            showReinvitePrompt = showReinvitePrompt,
            inviteProgress = inviteProgress.value,
            enableTextFormatting = MessageComposerConfig.ENABLE_RICH_TEXT_EDITING,
            enableVoiceMessages = enableVoiceMessages,
            appName = buildMeta.applicationName,
            callState = callState,
            eventSink = { handleEvents(it) }
        )
    }

    private fun MatrixRoomInfo.avatarData(): AvatarData {
        return AvatarData(
            id = id.value,
            name = name,
            url = avatarUrl ?: room.avatarUrl,
            size = AvatarSize.TimelineRoom
        )
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        enableTextFormatting: Boolean,
        timelineState: TimelineState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> handleCopyContents(targetEvent)
            TimelineItemAction.CopyLink -> handleCopyLink(targetEvent)
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState, enableTextFormatting)
            TimelineItemAction.Reply,
            TimelineItemAction.ReplyInThread -> handleActionReply(targetEvent, composerState)
            TimelineItemAction.ViewSource -> handleShowDebugInfoAction(targetEvent)
            TimelineItemAction.Forward -> handleForwardAction(targetEvent)
            TimelineItemAction.ReportContent -> handleReportAction(targetEvent)
            TimelineItemAction.EndPoll -> handleEndPollAction(targetEvent, timelineState)
        }
    }

    private fun CoroutineScope.toggleReaction(
        emoji: String,
        eventId: EventId,
    ) = launch(dispatchers.io) {
        timelineController.invokeOnCurrentTimeline {
            toggleReaction(emoji, eventId)
                .onFailure { Timber.e(it) }
        }
    }

    private fun CoroutineScope.reinviteOtherUser(inviteProgress: MutableState<AsyncData<Unit>>) = launch(dispatchers.io) {
        inviteProgress.value = AsyncData.Loading()
        runCatching {
            val memberList = when (val memberState = room.membersStateFlow.value) {
                is MatrixRoomMembersState.Ready -> memberState.roomMembers
                is MatrixRoomMembersState.Error -> memberState.prevRoomMembers.orEmpty()
                else -> emptyList()
            }

            val member = memberList.first { it.userId != room.sessionId }
            room.inviteUserById(member.userId).onFailure { t ->
                Timber.e(t, "Failed to reinvite DM partner")
            }.getOrThrow()
        }.fold(
            onSuccess = {
                inviteProgress.value = AsyncData.Success(Unit)
            },
            onFailure = {
                inviteProgress.value = AsyncData.Failure(it)
            }
        )
    }

    private suspend fun handleActionRedact(event: TimelineItem.Event) {
        if (event.failedToSend) {
            // If the message hasn't been sent yet, just cancel it
            event.transactionId?.let { room.cancelSend(it) }
        } else if (event.eventId != null) {
            room.redactEvent(event.eventId)
        }
    }

    private fun handleActionEdit(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        enableTextFormatting: Boolean,
    ) {
        when (targetEvent.content) {
            is TimelineItemPollContent -> {
                if (targetEvent.eventId == null) return
                navigator.onEditPollClicked(targetEvent.eventId)
            }
            else -> {
                val composerMode = MessageComposerMode.Edit(
                    targetEvent.eventId,
                    (targetEvent.content as? TimelineItemTextBasedContent)?.let {
                        if (enableTextFormatting) {
                            it.htmlBody ?: it.body
                        } else {
                            it.body
                        }
                    }.orEmpty(),
                    targetEvent.transactionId,
                )
                composerState.eventSink(
                    MessageComposerEvents.SetMode(composerMode)
                )
            }
        }
    }

    private fun handleActionReply(targetEvent: TimelineItem.Event, composerState: MessageComposerState) {
        if (targetEvent.eventId == null) return
        val textContent = messageSummaryFormatter.format(targetEvent)
        val attachmentThumbnailInfo = when (targetEvent.content) {
            is TimelineItemImageContent -> AttachmentThumbnailInfo(
                thumbnailSource = targetEvent.content.thumbnailSource ?: targetEvent.content.mediaSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Image,
                blurHash = targetEvent.content.blurhash,
            )
            is TimelineItemStickerContent -> AttachmentThumbnailInfo(
                thumbnailSource = targetEvent.content.thumbnailSource ?: targetEvent.content.mediaSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Image,
                blurHash = targetEvent.content.blurhash,
            )
            is TimelineItemVideoContent -> AttachmentThumbnailInfo(
                thumbnailSource = targetEvent.content.thumbnailSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Video,
                blurHash = targetEvent.content.blurHash,
            )
            is TimelineItemFileContent -> AttachmentThumbnailInfo(
                thumbnailSource = targetEvent.content.thumbnailSource,
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.File,
            )
            is TimelineItemAudioContent -> AttachmentThumbnailInfo(
                textContent = targetEvent.content.body,
                type = AttachmentThumbnailType.Audio,
            )
            is TimelineItemVoiceContent -> AttachmentThumbnailInfo(
                textContent = textContent,
                type = AttachmentThumbnailType.Voice,
            )
            is TimelineItemLocationContent -> AttachmentThumbnailInfo(
                type = AttachmentThumbnailType.Location,
            )
            is TimelineItemPollContent -> AttachmentThumbnailInfo(
                textContent = targetEvent.content.question,
                type = AttachmentThumbnailType.Poll,
            )
            is TimelineItemTextBasedContent,
            is TimelineItemRedactedContent,
            is TimelineItemStateContent,
            is TimelineItemEncryptedContent,
            is TimelineItemLegacyCallInviteContent,
            is TimelineItemUnknownContent -> null
        }
        val composerMode = MessageComposerMode.Reply(
            isThreaded = targetEvent.isThreaded,
            senderName = targetEvent.safeSenderName,
            eventId = targetEvent.eventId,
            attachmentThumbnailInfo = attachmentThumbnailInfo,
            defaultContent = textContent,
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }

    private fun handleShowDebugInfoAction(event: TimelineItem.Event) {
        navigator.onShowEventDebugInfoClicked(event.eventId, event.debugInfo)
    }

    private fun handleForwardAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.onForwardEventClicked(event.eventId)
    }

    private fun handleReportAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.onReportContentClicked(event.eventId, event.senderId)
    }

    private fun handleEndPollAction(
        event: TimelineItem.Event,
        timelineState: TimelineState,
    ) {
        event.eventId?.let { timelineState.eventSink(TimelineEvents.PollEndClicked(it)) }
    }

    private suspend fun handleCopyLink(event: TimelineItem.Event) {
        event.eventId ?: return
        room.getPermalinkFor(event.eventId).fold(
            onSuccess = { permalink ->
                clipboardHelper.copyPlainText(permalink)
                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_link_copied_to_clipboard))
            },
            onFailure = {
                Timber.e(it, "Failed to get permalink for event ${event.eventId}")
                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
            }
        )
    }

    private suspend fun handleCopyContents(event: TimelineItem.Event) {
        val content = when (event.content) {
            is TimelineItemTextBasedContent -> event.content.body
            is TimelineItemStateContent -> event.content.body
            else -> return
        }

        clipboardHelper.copyPlainText(content)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            snackbarDispatcher.post(SnackbarMessage(R.string.screen_room_timeline_message_copied))
        }
    }
}
