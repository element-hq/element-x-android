/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.test.FakeElementCallEntryPoint
import io.element.android.features.changeroommemberroles.test.FakeChangeRoomMemberRolesEntryPoint
import io.element.android.features.changeroommemberroles.test.FakeRolesAndPermissionsEntryPoint
import io.element.android.features.knockrequests.test.FakeKnockRequestsListEntryPoint
import io.element.android.features.messages.test.FakeMessagesEntryPoint
import io.element.android.features.poll.test.history.FakePollHistoryEntryPoint
import io.element.android.features.reportroom.test.FakeReportRoomEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetailsedit.test.FakeRoomDetailsEditEntryPoint
import io.element.android.features.securityandprivacy.test.FakeSecurityAndPrivacyEntryPoint
import io.element.android.features.verifysession.test.FakeOutgoingVerificationEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.mediaviewer.test.FakeMediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.test.FakeMediaViewerEntryPoint
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
                pollHistoryEntryPoint = FakePollHistoryEntryPoint(),
                elementCallEntryPoint = FakeElementCallEntryPoint(),
                room = FakeJoinedRoom(),
                analyticsService = FakeAnalyticsService(),
                messagesEntryPoint = FakeMessagesEntryPoint(),
                knockRequestsListEntryPoint = FakeKnockRequestsListEntryPoint(),
                mediaViewerEntryPoint = FakeMediaViewerEntryPoint(),
                mediaGalleryEntryPoint = FakeMediaGalleryEntryPoint(),
                outgoingVerificationEntryPoint = FakeOutgoingVerificationEntryPoint(),
                reportRoomEntryPoint = FakeReportRoomEntryPoint(),
                changeRoomMemberRolesEntryPoint = FakeChangeRoomMemberRolesEntryPoint(),
                rolesAndPermissionsEntryPoint = FakeRolesAndPermissionsEntryPoint(),
                securityAndPrivacyEntryPoint = FakeSecurityAndPrivacyEntryPoint(),
                roomDetailsEditEntryPoint = FakeRoomDetailsEditEntryPoint(),
            )
        }
        val callback = object : RoomDetailsEntryPoint.Callback {
            override fun navigateToGlobalNotificationSettings() = lambdaError()
            override fun navigateToRoom(roomId: RoomId, serverNames: List<String>) = lambdaError()
            override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) = lambdaError()
            override fun startForwardEventFlow(eventId: EventId, fromPinnedEvents: Boolean) = lambdaError()
        }
        val params = RoomDetailsEntryPoint.Params(
            initialElement = RoomDetailsEntryPoint.InitialTarget.RoomDetails,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
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
