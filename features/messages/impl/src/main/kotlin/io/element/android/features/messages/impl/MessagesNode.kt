/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.analytics.toAnalyticsViewRoom
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.alias.matches
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList

@ContributesNode(RoomScope::class)
class MessagesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: MatrixRoom,
    private val analyticsService: AnalyticsService,
    presenterFactory: MessagesPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val mediaPlayer: MediaPlayer,
    private val permalinkParser: PermalinkParser,
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    private val presenter = presenterFactory.create(this)
    private val callbacks = plugins<Callback>()

    data class Inputs(val focusedEventId: EventId?) : NodeInputs

    private val inputs = inputs<Inputs>()

    interface Callback : Plugin {
        fun onRoomDetailsClick()
        fun onEventClick(event: TimelineItem.Event): Boolean
        fun onPreviewAttachments(attachments: ImmutableList<Attachment>)
        fun onUserDataClick(userId: UserId)
        fun onPermalinkClick(data: PermalinkData)
        fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun onForwardEventClick(eventId: EventId)
        fun onReportMessage(eventId: EventId, senderId: UserId)
        fun onSendLocationClick()
        fun onCreatePollClick()
        fun onEditPollClick(eventId: EventId)
        fun onJoinCallClick(roomId: RoomId)
        fun onViewAllPinnedEvents()
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                analyticsService.capture(room.toAnalyticsViewRoom())
            },
            onDestroy = {
                mediaPlayer.close()
            }
        )
    }

    private fun onRoomDetailsClick() {
        callbacks.forEach { it.onRoomDetailsClick() }
    }

    private fun onEventClick(event: TimelineItem.Event): Boolean {
        // Note: cannot use `callbacks.all { it.onEventClick(event) }` because:
        // - if callbacks is empty, it will return true and we want to return false.
        // - if a callback returns false, the other callback will not be invoked.
        return callbacks.takeIf { it.isNotEmpty() }
            ?.map { it.onEventClick(event) }
            ?.all { it }
            .orFalse()
    }

    private fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
        callbacks.forEach { it.onPreviewAttachments(attachments) }
    }

    private fun onUserDataClick(userId: UserId) {
        callbacks.forEach { it.onUserDataClick(userId) }
    }

    private fun onLinkClick(
        activity: Activity,
        darkTheme: Boolean,
        url: String,
        eventSink: (TimelineEvents) -> Unit,
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callbacks.forEach { it.onUserDataClick(permalink.userId) }
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClick(activity, permalink, eventSink)
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                activity.openUrlInChromeCustomTab(null, darkTheme, url)
            }
        }
    }

    private fun handleRoomLinkClick(
        context: Context,
        roomLink: PermalinkData.RoomLink,
        eventSink: (TimelineEvents) -> Unit,
    ) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            val eventId = roomLink.eventId
            if (eventId != null) {
                eventSink(TimelineEvents.FocusOnEvent(eventId))
            } else {
                // Click on the same room, ignore
                context.toast("Already viewing this room!")
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

    private fun onViewAllPinnedMessagesClick() {
        callbacks.forEach { it.onViewAllPinnedEvents() }
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

    @Composable
    override fun View(modifier: Modifier) {
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val state = presenter.present()
            OnLifecycleEvent { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> state.composerState.eventSink(MessageComposerEvents.SaveDraft)
                    else -> Unit
                }
            }
            MessagesView(
                state = state,
                onBackClick = this::navigateUp,
                onRoomDetailsClick = this::onRoomDetailsClick,
                onEventClick = this::onEventClick,
                onPreviewAttachments = this::onPreviewAttachments,
                onUserDataClick = this::onUserDataClick,
                onLinkClick = { url -> onLinkClick(activity, isDark, url, state.timelineState.eventSink) },
                onSendLocationClick = this::onSendLocationClick,
                onCreatePollClick = this::onCreatePollClick,
                onJoinCallClick = this::onJoinCallClick,
                onViewAllPinnedMessagesClick = this::onViewAllPinnedMessagesClick,
                modifier = modifier,
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
