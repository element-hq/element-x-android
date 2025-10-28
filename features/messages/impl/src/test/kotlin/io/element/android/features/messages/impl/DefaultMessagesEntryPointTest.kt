/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.runtime.Composable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.forward.api.ForwardEntryPoint
import io.element.android.features.knockrequests.api.list.KnockRequestsListEntryPoint
import io.element.android.features.location.api.SendLocationEntryPoint
import io.element.android.features.location.api.ShowLocationEntryPoint
import io.element.android.features.location.test.FakeLocationService
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.messages.impl.pinned.banner.createPinnedEventsTimelineProvider
import io.element.android.features.messages.impl.timeline.createTimelineController
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.RoomNamesCache
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpanUpdater
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultMessagesEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultMessagesEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MessagesFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                roomListService = FakeRoomListService(),
                sessionId = A_SESSION_ID,
                sendLocationEntryPoint = object : SendLocationEntryPoint {
                    override fun builder(timelineMode: Timeline.Mode) = lambdaError()
                },
                showLocationEntryPoint = object : ShowLocationEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext, inputs: ShowLocationEntryPoint.Inputs) = lambdaError()
                },
                createPollEntryPoint = object : CreatePollEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                elementCallEntryPoint = object : ElementCallEntryPoint {
                    override fun startCall(callType: CallType) = lambdaError()
                    override suspend fun handleIncomingCall(
                        callType: CallType.RoomCall,
                        eventId: EventId,
                        senderId: UserId,
                        roomName: String?,
                        senderName: String?,
                        avatarUrl: String?,
                        timestamp: Long,
                        expirationTimestamp: Long,
                        notificationChannelId: String,
                        textContent: String?,
                    ) = lambdaError()
                },
                mediaViewerEntryPoint = object : MediaViewerEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                forwardEntryPoint = object : ForwardEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                analyticsService = FakeAnalyticsService(),
                locationService = FakeLocationService(),
                room = FakeBaseRoom(),
                roomMemberProfilesCache = RoomMemberProfilesCache(),
                roomNamesCache = RoomNamesCache(),
                mentionSpanUpdater = object : MentionSpanUpdater {
                    override fun updateMentionSpans(text: CharSequence) = text

                    @Composable
                    override fun rememberMentionSpans(text: CharSequence) = text
                },
                mentionSpanTheme = MentionSpanTheme(A_USER_ID),
                pinnedEventsTimelineProvider = createPinnedEventsTimelineProvider(),
                timelineController = createTimelineController(),
                knockRequestsListEntryPoint = object : KnockRequestsListEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                dateFormatter = FakeDateFormatter(),
                coroutineDispatchers = testCoroutineDispatchers(),
            )
        }
        val callback = object : MessagesEntryPoint.Callback {
            override fun onRoomDetailsClick() = lambdaError()
            override fun onUserDataClick(userId: UserId) = lambdaError()
            override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) = lambdaError()
            override fun forwardEvent(eventId: EventId) = lambdaError()
            override fun openRoom(roomId: RoomId) = lambdaError()
        }
        val initialTarget = MessagesEntryPoint.InitialTarget.Messages(focusedEventId = AN_EVENT_ID)
        val params = MessagesEntryPoint.Params(initialTarget)
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(MessagesFlowNode::class.java)
        assertThat(result.plugins).contains(MessagesEntryPoint.Params(initialTarget))
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test initial target to nav target mapping`() {
        assertThat(MessagesEntryPoint.InitialTarget.Messages(focusedEventId = AN_EVENT_ID).toNavTarget())
            .isEqualTo(MessagesFlowNode.NavTarget.Messages(focusedEventId = AN_EVENT_ID))
        assertThat(MessagesEntryPoint.InitialTarget.PinnedMessages.toNavTarget())
            .isEqualTo(MessagesFlowNode.NavTarget.PinnedMessagesList)
    }
}
