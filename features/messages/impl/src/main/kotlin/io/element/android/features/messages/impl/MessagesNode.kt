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
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.alias.matches
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.extensions.toAnalyticsViewRoom
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
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    private val presenter = presenterFactory.create(this)
    private val callback = plugins<Callback>().firstOrNull()

    // TODO Handle navigation to the Event
    data class Inputs(val eventId: EventId?) : NodeInputs

    interface Callback : Plugin {
        fun onRoomDetailsClicked()
        fun onEventClicked(event: TimelineItem.Event): Boolean
        fun onPreviewAttachments(attachments: ImmutableList<Attachment>)
        fun onUserDataClicked(userId: UserId)
        fun onPermalinkClicked(data: PermalinkData)
        fun onShowEventDebugInfoClicked(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun onForwardEventClicked(eventId: EventId)
        fun onReportMessage(eventId: EventId, senderId: UserId)
        fun onSendLocationClicked()
        fun onCreatePollClicked()
        fun onEditPollClicked(eventId: EventId)
        fun onJoinCallClicked(roomId: RoomId)
    }

    init {
        lifecycle.subscribe(
            onCreate = {
                analyticsService.capture(room.toAnalyticsViewRoom())
            },
            onDestroy = {
                mediaPlayer.close()
            }
        )
    }

    private fun onRoomDetailsClicked() {
        callback?.onRoomDetailsClicked()
    }

    private fun onEventClicked(event: TimelineItem.Event): Boolean {
        return callback?.onEventClicked(event).orFalse()
    }

    private fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
        callback?.onPreviewAttachments(attachments)
    }

    private fun onUserDataClicked(userId: UserId) {
        callback?.onUserDataClicked(userId)
    }

    private fun onLinkClicked(
        context: Context,
        url: String,
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                if (permalink.userId in room.membersStateFlow.value.roomMembers().orEmpty().map { it.userId }) {
                    // Open the room member profile
                    callback?.onUserDataClicked(permalink.userId)
                } else {
                    // The user is not a member of the room
                    callback?.onPermalinkClicked(permalink)
                }
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClicked(permalink)
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    private fun handleRoomLinkClicked(roomLink: PermalinkData.RoomLink) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            if (roomLink.eventId != null) {
                // TODO Handle navigation to the Event
                context.toast("TODO Handle navigation to the Event ${roomLink.eventId}")
            } else {
                // Click on the same room, ignore
                context.toast("Already viewing this room!")
            }
        } else {
            callback?.onPermalinkClicked(roomLink)
        }
    }

    override fun onShowEventDebugInfoClicked(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback?.onShowEventDebugInfoClicked(eventId, debugInfo)
    }

    override fun onForwardEventClicked(eventId: EventId) {
        callback?.onForwardEventClicked(eventId)
    }

    override fun onReportContentClicked(eventId: EventId, senderId: UserId) {
        callback?.onReportMessage(eventId, senderId)
    }

    override fun onEditPollClicked(eventId: EventId) {
        callback?.onEditPollClicked(eventId)
    }

    private fun onSendLocationClicked() {
        callback?.onSendLocationClicked()
    }

    private fun onCreatePollClicked() {
        callback?.onCreatePollClicked()
    }

    private fun onJoinCallClicked() {
        callback?.onJoinCallClicked(room.roomId)
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
                onBackPressed = this::navigateUp,
                onRoomDetailsClicked = this::onRoomDetailsClicked,
                onEventClicked = this::onEventClicked,
                onPreviewAttachments = this::onPreviewAttachments,
                onUserDataClicked = this::onUserDataClicked,
                onLinkClicked = { onLinkClicked(context, it) },
                onSendLocationClicked = this::onSendLocationClicked,
                onCreatePollClicked = this::onCreatePollClicked,
                onJoinCallClicked = this::onJoinCallClicked,
                modifier = modifier,
            )
        }
    }
}
