/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.knockrequests.api.banner.KnockRequestsBannerRenderer
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationRenderer
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.analytics.toAnalyticsViewRoom
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.alias.matches
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
@AssistedInject
class MessagesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    @ApplicationContext private val context: Context,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
    messageComposerPresenterFactory: MessageComposerPresenter.Factory,
    timelinePresenterFactory: TimelinePresenter.Factory,
    presenterFactory: MessagesPresenter.Factory,
    actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val mediaPlayer: MediaPlayer,
    private val permalinkParser: PermalinkParser,
    private val knockRequestsBannerRenderer: KnockRequestsBannerRenderer,
    private val roomMemberModerationRenderer: RoomMemberModerationRenderer,
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    private val callbacks = plugins<Callback>()

    data class Inputs(
        val focusedEventId: EventId?,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val timelineController = TimelineController(room, room.liveTimeline)
    private val presenter = presenterFactory.create(
        navigator = this,
        composerPresenter = messageComposerPresenterFactory.create(timelineController, this),
        timelinePresenter = timelinePresenterFactory.create(timelineController = timelineController, this),
        actionListPresenter = actionListPresenterFactory.create(
            postProcessor = TimelineItemActionPostProcessor.Default,
            timelineMode = timelineController.mainTimelineMode(),
        ),
        timelineController = timelineController,
    )

    interface Callback : Plugin {
        fun onEventClick(timelineMode: Timeline.Mode, event: TimelineItem.Event): Boolean
        fun onPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?)
        fun onUserDataClick(userId: UserId)
        fun onPermalinkClick(data: PermalinkData)
        fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun onForwardEventClick(eventId: EventId)
        fun onReportMessage(eventId: EventId, senderId: UserId)
        fun onSendLocationClick()
        fun onCreatePollClick()
        fun onEditPollClick(eventId: EventId)
        fun onJoinCallClick(roomId: RoomId)
        fun onOpenThread(threadRootId: ThreadId, focusedEventId: EventId?)
        fun onRoomDetailsClick()
        fun onViewAllPinnedEvents()
        fun onViewKnockRequests()
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                sessionCoroutineScope.launch { analyticsService.capture(room.toAnalyticsViewRoom()) }
            },
            onDestroy = {
                mediaPlayer.close()
            }
        )
    }

    private fun onRoomDetailsClick() {
        callbacks.forEach { it.onRoomDetailsClick() }
    }

    private fun onViewAllPinnedMessagesClick() {
        callbacks.forEach { it.onViewAllPinnedEvents() }
    }

    private fun onViewKnockRequestsClick() {
        callbacks.forEach { it.onViewKnockRequests() }
    }

    private fun onEventClick(timelineMode: Timeline.Mode, event: TimelineItem.Event): Boolean {
        // Note: cannot use `callbacks.all { it.onEventClick(event) }` because:
        // - if callbacks is empty, it will return true and we want to return false.
        // - if a callback returns false, the other callback will not be invoked.
        return callbacks.takeIf { it.isNotEmpty() }
            ?.map { it.onEventClick(timelineMode, event) }
            ?.all { it }
            .orFalse()
    }

    private fun onUserDataClick(userId: UserId) {
        callbacks.forEach { it.onUserDataClick(userId) }
    }

    private fun onLinkClick(
        activity: Activity,
        darkTheme: Boolean,
        url: String,
        eventSink: (TimelineEvents) -> Unit,
        customTab: Boolean
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callbacks.forEach { it.onUserDataClick(permalink.userId) }
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClick(permalink, eventSink)
            }
            is PermalinkData.FallbackLink -> {
                if (customTab) {
                    activity.openUrlInChromeCustomTab(null, darkTheme, url)
                } else {
                    activity.openUrlInExternalApp(url)
                }
            }
            is PermalinkData.RoomEmailInviteLink -> {
                activity.openUrlInChromeCustomTab(null, darkTheme, url)
            }
        }
    }

    private fun handleRoomLinkClick(
        roomLink: PermalinkData.RoomLink,
        eventSink: (TimelineEvents) -> Unit,
    ) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            val eventId = roomLink.eventId
            if (eventId != null) {
                eventSink(TimelineEvents.FocusOnEvent(eventId))
            } else {
                // Click on the same room, ignore
                displaySameRoomToast()
            }
        } else {
            callbacks.forEach { it.onPermalinkClick(roomLink) }
        }
    }

    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callbacks.forEach { it.onShowEventDebugInfoClick(eventId, debugInfo) }
    }

    override fun onForwardEventClick(eventId: EventId) {
        callbacks.forEach { it.onForwardEventClick(eventId) }
    }

    override fun onReportContentClick(eventId: EventId, senderId: UserId) {
        callbacks.forEach { it.onReportMessage(eventId, senderId) }
    }

    override fun onEditPollClick(eventId: EventId) {
        callbacks.forEach { it.onEditPollClick(eventId) }
    }

    override fun onPreviewAttachment(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
        callbacks.forEach { it.onPreviewAttachments(attachments, inReplyToEventId) }
    }

    override fun onNavigateToRoom(roomId: RoomId, eventId: EventId?, serverNames: List<String>) {
        if (roomId == room.roomId) {
            displaySameRoomToast()
        } else {
            val permalinkData = PermalinkData.RoomLink(roomId.toRoomIdOrAlias(), eventId, viaParameters = serverNames.toImmutableList())
            callbacks.forEach { it.onPermalinkClick(permalinkData) }
        }
    }

    override fun onOpenThread(threadRootId: ThreadId, focusedEventId: EventId?) {
        callbacks.forEach { it.onOpenThread(threadRootId, focusedEventId) }
    }

    private fun onSendLocationClick() {
        callbacks.forEach { it.onSendLocationClick() }
    }

    private fun onCreatePollClick() {
        callbacks.forEach { it.onCreatePollClick() }
    }

    private fun onJoinCallClick() {
        callbacks.forEach { it.onJoinCallClick(room.roomId) }
    }

    private fun displaySameRoomToast() {
        context.toast(CommonStrings.screen_room_permalink_same_room_android)
    }

    override fun onNavigateUp() = navigateUp()

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val state = presenter.present()

            BackHandler {
                state.eventSink(MessagesEvents.MarkAsFullyReadAndExit)
            }

            OnLifecycleEvent { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> state.composerState.eventSink(MessageComposerEvents.SaveDraft)
                    else -> Unit
                }
            }
            MessagesView(
                state = state,
                onBackClick = { state.eventSink(MessagesEvents.MarkAsFullyReadAndExit) },
                onRoomDetailsClick = this::onRoomDetailsClick,
                onEventContentClick = { isLive, event ->
                    if (isLive) {
                        onEventClick(timelineController.mainTimelineMode(), event)
                    } else {
                        val detachedTimelineMode = timelineController.detachedTimelineMode()
                        if (detachedTimelineMode != null) {
                            onEventClick(detachedTimelineMode, event)
                        } else {
                            false
                        }
                    }
                },
                onUserDataClick = this::onUserDataClick,
                onLinkClick = { url, customTab ->
                    onLinkClick(
                        activity = activity,
                        darkTheme = isDark,
                        url = url,
                        eventSink = state.timelineState.eventSink,
                        customTab = customTab,
                    )
                },
                onSendLocationClick = this::onSendLocationClick,
                onCreatePollClick = this::onCreatePollClick,
                onJoinCallClick = this::onJoinCallClick,
                onViewAllPinnedMessagesClick = this::onViewAllPinnedMessagesClick,
                modifier = modifier,
                knockRequestsBannerView = {
                    knockRequestsBannerRenderer.View(
                        modifier = Modifier,
                        onViewRequestsClick = this::onViewKnockRequestsClick
                    )
                },
            )
            roomMemberModerationRenderer.Render(
                state = state.roomMemberModerationState,
                onSelectAction = { action, target ->
                    when (action) {
                        is ModerationAction.DisplayProfile -> onUserDataClick(target.userId)
                        else -> state.roomMemberModerationState.eventSink(RoomMemberModerationEvents.ProcessAction(action, target))
                    }
                },
                modifier = Modifier,
            )

            var focusedEventId by rememberSaveable {
                mutableStateOf(inputs.focusedEventId)
            }
            LaunchedEffect(Unit) {
                focusedEventId?.also { eventId ->
                    state.timelineState.eventSink(TimelineEvents.FocusOnEvent(eventId))
                }
                // Reset the focused event id to null to avoid refocusing when restoring node.
                focusedEventId = null
            }
        }
    }
}
