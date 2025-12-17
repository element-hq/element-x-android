/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads

import android.app.Activity
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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.MessagesNavigator
import io.element.android.features.messages.impl.MessagesPresenter
import io.element.android.features.messages.impl.MessagesView
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvent
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.analytics.toAnalyticsViewRoom
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.alias.matches
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@ContributesNode(RoomScope::class)
@AssistedInject
class ThreadedMessagesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
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
    private val appNavigationStateService: AppNavigationStateService,
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    data class Inputs(
        val threadRootEventId: ThreadId,
        val focusedEventId: EventId?,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: Callback = callback()

    // TODO use a loading state node to preload this instead of using `runBlocking`
    private val threadedTimeline = runBlocking { room.createTimeline(CreateTimelineParams.Threaded(threadRootEventId = inputs.threadRootEventId)).getOrThrow() }
    private val timelineController = TimelineController(room, threadedTimeline)
    private val presenter = presenterFactory.create(
        navigator = this,
        composerPresenter = messageComposerPresenterFactory.create(timelineController, this),
        timelinePresenter = timelinePresenterFactory.create(timelineController = timelineController, this),
        // TODO add special processor for threaded timeline
        actionListPresenter = actionListPresenterFactory.create(
            postProcessor = TimelineItemActionPostProcessor.Default,
            timelineMode = timelineController.mainTimelineMode(),
        ),
        timelineController = timelineController,
    )

    interface Callback : Plugin {
        fun handleEventClick(timelineMode: Timeline.Mode, event: TimelineItem.Event): Boolean
        fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?)
        fun navigateToRoomMemberDetails(userId: UserId)
        fun handlePermalinkClick(data: PermalinkData)
        fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun handleForwardEventClick(eventId: EventId)
        fun navigateToReportMessage(eventId: EventId, senderId: UserId)
        fun navigateToSendLocation()
        fun navigateToCreatePoll()
        fun navigateToEditPoll(eventId: EventId)
        fun navigateToRoomCall(roomId: RoomId)
        fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?)
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                sessionCoroutineScope.launch { analyticsService.capture(room.toAnalyticsViewRoom()) }
            },
            onStart = {
                appNavigationStateService.onNavigateToThread(id, inputs.threadRootEventId)
            },
            onStop = {
                appNavigationStateService.onLeavingThread(id)
            },
            onDestroy = {
                mediaPlayer.close()
            }
        )
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
                callback.navigateToRoomMemberDetails(permalink.userId)
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
                // Click on the same room, navigate up
                // Note that it can not be enough to go back to the room if the thread has been opened
                // following a permalink from another thread. In this case navigating up will go back
                // to the previous thread. But this should not happen often.
                navigateUp()
            }
        } else {
            callback.handlePermalinkClick(roomLink)
        }
    }

    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback.navigateToEventDebugInfo(eventId, debugInfo)
    }

    override fun forwardEvent(eventId: EventId) {
        callback.handleForwardEventClick(eventId)
    }

    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
        callback.navigateToReportMessage(eventId, senderId)
    }

    override fun navigateToEditPoll(eventId: EventId) {
        callback.navigateToEditPoll(eventId)
    }

    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
        callback.navigateToPreviewAttachments(attachments, inReplyToEventId)
    }

    override fun navigateToRoom(roomId: RoomId, eventId: EventId?, serverNames: List<String>) {
        val permalinkData = PermalinkData.RoomLink(roomId.toRoomIdOrAlias(), eventId, viaParameters = serverNames.toImmutableList())
        callback.handlePermalinkClick(permalinkData)
    }

    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
        callback.navigateToThread(threadRootId, focusedEventId)
    }

    override fun close() = navigateUp()

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val state = presenter.present()
            OnLifecycleEvent { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> state.composerState.eventSink(MessageComposerEvent.SaveDraft)
                    else -> Unit
                }
            }
            MessagesView(
                state = state,
                onBackClick = this::navigateUp,
                onRoomDetailsClick = {},
                onEventContentClick = { isLive, event ->
                    if (isLive) {
                        callback.handleEventClick(timelineController.mainTimelineMode(), event)
                    } else {
                        val detachedTimelineMode = timelineController.detachedTimelineMode()
                        if (detachedTimelineMode != null) {
                            callback.handleEventClick(detachedTimelineMode, event)
                        } else {
                            false
                        }
                    }
                },
                onUserDataClick = callback::navigateToRoomMemberDetails,
                onLinkClick = { url, customTab ->
                    onLinkClick(
                        activity = activity,
                        darkTheme = isDark,
                        url = url,
                        eventSink = state.timelineState.eventSink,
                        customTab = customTab,
                    )
                },
                onSendLocationClick = callback::navigateToSendLocation,
                onCreatePollClick = callback::navigateToCreatePoll,
                onJoinCallClick = { callback.navigateToRoomCall(room.roomId) },
                onViewAllPinnedMessagesClick = {},
                modifier = modifier,
                knockRequestsBannerView = {},
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
