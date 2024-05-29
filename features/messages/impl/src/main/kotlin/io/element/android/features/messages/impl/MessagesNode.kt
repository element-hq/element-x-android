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
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.ApplicationContext
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
    @ApplicationContext
    private val context: Context,
    private val timelineController: TimelineController,
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    private val presenter = presenterFactory.create(this)
    private val callback = plugins<Callback>().firstOrNull()

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
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                analyticsService.capture(room.toAnalyticsViewRoom())
            },
            onDestroy = {
                timelineController.close()
                mediaPlayer.close()
            }
        )
    }

    private fun onRoomDetailsClick() {
        callback?.onRoomDetailsClick()
    }

    private fun onEventClick(event: TimelineItem.Event): Boolean {
        return callback?.onEventClick(event).orFalse()
    }

    private fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
        callback?.onPreviewAttachments(attachments)
    }

    private fun onUserDataClick(userId: UserId) {
        callback?.onUserDataClick(userId)
    }

    private fun onLinkClick(
        context: Context,
        url: String,
        eventSink: (TimelineEvents) -> Unit,
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callback?.onUserDataClick(permalink.userId)
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClick(permalink, eventSink)
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    private fun handleRoomLinkClick(roomLink: PermalinkData.RoomLink, eventSink: (TimelineEvents) -> Unit) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            val eventId = roomLink.eventId
            if (eventId != null) {
                eventSink(TimelineEvents.FocusOnEvent(eventId))
            } else {
                // Click on the same room, ignore
                context.toast("Already viewing this room!")
            }
        } else {
            callback?.onPermalinkClick(roomLink)
        }
    }

    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback?.onShowEventDebugInfoClick(eventId, debugInfo)
    }

    override fun onForwardEventClick(eventId: EventId) {
        callback?.onForwardEventClick(eventId)
    }

    override fun onReportContentClick(eventId: EventId, senderId: UserId) {
        callback?.onReportMessage(eventId, senderId)
    }

    override fun onEditPollClick(eventId: EventId) {
        callback?.onEditPollClick(eventId)
    }

    private fun onSendLocationClick() {
        callback?.onSendLocationClick()
    }

    private fun onCreatePollClick() {
        callback?.onCreatePollClick()
    }

    private fun onJoinCallClick() {
        callback?.onJoinCallClick(room.roomId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val state = presenter.present()
            MessagesView(
                state = state,
                onBackClick = this::navigateUp,
                onRoomDetailsClick = this::onRoomDetailsClick,
                onEventClick = this::onEventClick,
                onPreviewAttachments = this::onPreviewAttachments,
                onUserDataClick = this::onUserDataClick,
                onLinkClick = { onLinkClick(context, it, state.timelineState.eventSink) },
                onSendLocationClick = this::onSendLocationClick,
                onCreatePollClick = this::onCreatePollClick,
                onJoinCallClick = this::onJoinCallClick,
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
