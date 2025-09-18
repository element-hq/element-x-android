/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultHomeEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultHomeEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            HomeFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                matrixClient = FakeMatrixClient(),
                presenter = createHomePresenter(),
                inviteFriendsUseCase = { lambdaError() },
                analyticsService = FakeAnalyticsService(),
                acceptDeclineInviteView = { _, _, _, _ -> lambdaError() },
                directLogoutView = { _ -> lambdaError() },
                reportRoomEntryPoint = { _, _, _ -> lambdaError() },
                declineInviteAndBlockUserEntryPoint = { _, _, _ -> lambdaError() },
                changeRoomMemberRolesEntryPoint = { _, _ -> lambdaError() },
                leaveRoomRenderer = { _, _, _ -> lambdaError() },
            )
        }
        val callback = object : HomeEntryPoint.Callback {
            override fun onRoomClick(roomId: RoomId) = lambdaError()
            override fun onStartChatClick() = lambdaError()
            override fun onSettingsClick() = lambdaError()
            override fun onSetUpRecoveryClick() = lambdaError()
            override fun onSessionConfirmRecoveryKeyClick() = lambdaError()
            override fun onRoomSettingsClick(roomId: RoomId) = lambdaError()
            override fun onReportBugClick() = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(HomeFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
