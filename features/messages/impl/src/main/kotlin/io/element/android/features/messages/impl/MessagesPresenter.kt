/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.PinUnpinAction
import io.element.android.appconfig.MessageComposerConfig
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerState
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
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.powerlevels.canPinUnpin
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.room.powerlevels.canSendMessage
import io.element.android.libraries.matrix.ui.messages.reply.map
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.room.canCall
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessagesPresenter @AssistedInject constructor(
    @Assisted private val navigator: MessagesNavigator,
    private val room: MatrixRoom,
    private val composerPresenter: Presenter<MessageComposerState>,
    private val voiceMessageComposerPresenter: Presenter<VoiceMessageComposerState>,
    timelinePresenterFactory: TimelinePresenter.Factory,
    private val timelineProtectionPresenter: Presenter<TimelineProtectionState>,
    private val actionListPresenterFactory: ActionListPresenter.Factory,
    private val customReactionPresenter: Presenter<CustomReactionState>,
    private val reactionSummaryPresenter: Presenter<ReactionSummaryState>,
    private val readReceiptBottomSheetPresenter: Presenter<ReadReceiptBottomSheetState>,
    private val pinnedMessagesBannerPresenter: Presenter<PinnedMessagesBannerState>,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val dispatchers: CoroutineDispatchers,
    private val clipboardHelper: ClipboardHelper,
    private val featureFlagsService: FeatureFlagService,
    private val htmlConverterProvider: HtmlConverterProvider,
    private val buildMeta: BuildMeta,
    private val timelineController: TimelineController,
    private val permalinkParser: PermalinkParser,
    private val analyticsService: AnalyticsService,
) : Presenter<MessagesState> {
    private val timelinePresenter = timelinePresenterFactory.create(navigator = navigator)
    private val actionListPresenter = actionListPresenterFactory.create(TimelineItemActionPostProcessor.Default)

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
        val timelineProtectionState = timelineProtectionPresenter.present()
        val actionListState = actionListPresenter.present()
        val customReactionState = customReactionPresenter.present()
        val reactionSummaryState = reactionSummaryPresenter.present()
        val readReceiptBottomSheetState = readReceiptBottomSheetPresenter.present()
        val pinnedMessagesBannerState = pinnedMessagesBannerPresenter.present()

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()

        val userEventPermissions by userEventPermissions(syncUpdateFlow.value)

        val roomName: AsyncData<String> by remember {
            derivedStateOf { roomInfo?.name?.let { AsyncData.Success(it) } ?: AsyncData.Uninitialized }
        }
        val roomAvatar: AsyncData<AvatarData> by remember {
            derivedStateOf { roomInfo?.avatarData()?.let { AsyncData.Success(it) } ?: AsyncData.Uninitialized }
        }
        val heroes by remember {
            derivedStateOf { roomInfo?.heroes().orEmpty().toPersistentList() }
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
                showReinvitePrompt = !hasDismissedInviteDialog && composerState.textEditorState.hasFocus() && room.isDm && room.activeMemberCount == 1L
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
                        timelineProtectionState = timelineProtectionState,
                    )
                }
                is MessagesEvents.ToggleReaction -> {
                    localCoroutineScope.toggleReaction(event.emoji, event.uniqueId)
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
            heroes = heroes,
            composerState = composerState,
            userEventPermissions = userEventPermissions,
            voiceMessageComposerState = voiceMessageComposerState,
            timelineState = timelineState,
            timelineProtectionState = timelineProtectionState,
            actionListState = actionListState,
            customReactionState = customReactionState,
            reactionSummaryState = reactionSummaryState,
            readReceiptBottomSheetState = readReceiptBottomSheetState,
            hasNetworkConnection = networkConnectionStatus == NetworkStatus.Online,
            snackbarMessage = snackbarMessage,
            showReinvitePrompt = showReinvitePrompt,
            inviteProgress = inviteProgress.value,
            enableTextFormatting = MessageComposerConfig.ENABLE_RICH_TEXT_EDITING,
            enableVoiceMessages = enableVoiceMessages,
            appName = buildMeta.applicationName,
            callState = callState,
            pinnedMessagesBannerState = pinnedMessagesBannerState,
            eventSink = { handleEvents(it) }
        )
    }

    @Composable
    private fun userEventPermissions(updateKey: Long): State<UserEventPermissions> {
        return produceState(UserEventPermissions.DEFAULT, key1 = updateKey) {
            value = UserEventPermissions(
                canSendMessage = room.canSendMessage(type = MessageEventType.ROOM_MESSAGE).getOrElse { true },
                canSendReaction = room.canSendMessage(type = MessageEventType.REACTION).getOrElse { true },
                canRedactOwn = room.canRedactOwn().getOrElse { false },
                canRedactOther = room.canRedactOther().getOrElse { false },
                canPinUnpin = room.canPinUnpin().getOrElse { false },
            )
        }
    }

    private fun MatrixRoomInfo.avatarData(): AvatarData {
        return AvatarData(
            id = id.value,
            name = name,
            url = avatarUrl ?: room.avatarUrl,
            size = AvatarSize.TimelineRoom
        )
    }

    private fun MatrixRoomInfo.heroes(): List<AvatarData> {
        return heroes.map { user ->
            user.getAvatarData(size = AvatarSize.TimelineRoom)
        }
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        timelineProtectionState: TimelineProtectionState,
        enableTextFormatting: Boolean,
        timelineState: TimelineState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> handleCopyContents(targetEvent)
            TimelineItemAction.CopyLink -> handleCopyLink(targetEvent)
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState, enableTextFormatting)
            TimelineItemAction.Reply,
            TimelineItemAction.ReplyInThread -> handleActionReply(targetEvent, composerState, timelineProtectionState)
            TimelineItemAction.ViewSource -> handleShowDebugInfoAction(targetEvent)
            TimelineItemAction.Forward -> handleForwardAction(targetEvent)
            TimelineItemAction.ReportContent -> handleReportAction(targetEvent)
            TimelineItemAction.EndPoll -> handleEndPollAction(targetEvent, timelineState)
            TimelineItemAction.Pin -> handlePinAction(targetEvent)
            TimelineItemAction.Unpin -> handleUnpinAction(targetEvent)
            TimelineItemAction.ViewInTimeline -> Unit
        }
    }

    private suspend fun handlePinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        analyticsService.capture(
            PinUnpinAction(
                from = PinUnpinAction.From.Timeline,
                kind = PinUnpinAction.Kind.Pin,
            )
        )
        timelineController.invokeOnCurrentTimeline {
            pinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to pin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    private suspend fun handleUnpinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        analyticsService.capture(
            PinUnpinAction(
                from = PinUnpinAction.From.Timeline,
                kind = PinUnpinAction.Kind.Unpin,
            )
        )
        timelineController.invokeOnCurrentTimeline {
            unpinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to unpin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    private fun CoroutineScope.toggleReaction(
        emoji: String,
        uniqueId: UniqueId,
    ) = launch(dispatchers.io) {
        timelineController.invokeOnCurrentTimeline {
            toggleReaction(emoji, uniqueId)
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
        timelineController.invokeOnCurrentTimeline {
            redactEvent(eventId = event.eventId, transactionId = event.transactionId, reason = null)
                .onFailure { Timber.e(it) }
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
                navigator.onEditPollClick(targetEvent.eventId)
            }
            else -> {
                val composerMode = MessageComposerMode.Edit(
                    targetEvent.eventId,
                    targetEvent.transactionId,
                    (targetEvent.content as? TimelineItemTextBasedContent)?.let {
                        if (enableTextFormatting) {
                            it.htmlBody ?: it.body
                        } else {
                            it.body
                        }
                    }.orEmpty(),
                )
                composerState.eventSink(
                    MessageComposerEvents.SetMode(composerMode)
                )
            }
        }
    }

    private suspend fun handleActionReply(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        timelineProtectionState: TimelineProtectionState,
    ) {
        if (targetEvent.eventId == null) return
        timelineController.invokeOnCurrentTimeline {
            val replyToDetails = loadReplyDetails(targetEvent.eventId).map(permalinkParser)
            val composerMode = MessageComposerMode.Reply(
                replyToDetails = replyToDetails,
                hideImage = timelineProtectionState.hideMediaContent(targetEvent.eventId),
            )
            composerState.eventSink(
                MessageComposerEvents.SetMode(composerMode)
            )
        }
    }

    private fun handleShowDebugInfoAction(event: TimelineItem.Event) {
        navigator.onShowEventDebugInfoClick(event.eventId, event.debugInfo)
    }

    private fun handleForwardAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.onForwardEventClick(event.eventId)
    }

    private fun handleReportAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.onReportContentClick(event.eventId, event.senderId)
    }

    private fun handleEndPollAction(
        event: TimelineItem.Event,
        timelineState: TimelineState,
    ) {
        event.eventId?.let { timelineState.eventSink(TimelineEvents.EndPoll(it)) }
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
