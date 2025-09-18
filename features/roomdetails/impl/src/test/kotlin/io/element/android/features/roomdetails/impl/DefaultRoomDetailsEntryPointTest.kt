/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.knockrequests.api.list.KnockRequestsListEntryPoint
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.poll.api.history.PollHistoryEntryPoint
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultRoomDetailsEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultRoomDetailsEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            RoomDetailsFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                pollHistoryEntryPoint = object : PollHistoryEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext) = lambdaError()
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
                        textContent: String?
                    ) = lambdaError()
                },
                room = FakeJoinedRoom(),
                analyticsService = FakeAnalyticsService(),
                messagesEntryPoint = object : MessagesEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                knockRequestsListEntryPoint = object : KnockRequestsListEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                mediaViewerEntryPoint = object : MediaViewerEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                mediaGalleryEntryPoint = object : MediaGalleryEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                outgoingVerificationEntryPoint = object : OutgoingVerificationEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                reportRoomEntryPoint = object : ReportRoomEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext, roomId: RoomId) = lambdaError()
                },
                changeRoomMemberRolesEntryPoint = object : ChangeRoomMemberRolesEntryPoint {
                    override fun builder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
            )
        }
        val callback = object : RoomDetailsEntryPoint.Callback {
            override fun onOpenGlobalNotificationSettings() = lambdaError()
            override fun onOpenRoom(roomId: RoomId, serverNames: List<String>) = lambdaError()
            override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) = lambdaError()
            override fun onForwardedToSingleRoom(roomId: RoomId) = lambdaError()
        }
        val params = RoomDetailsEntryPoint.Params(
            initialElement = RoomDetailsEntryPoint.InitialTarget.RoomDetails,
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(RoomDetailsFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test initial target to nav target mapping`() {
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomDetails.toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomDetails)
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomMemberDetails(A_USER_ID).toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomMemberDetails(A_USER_ID))
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomNotificationSettings.toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = true))
    }
}
