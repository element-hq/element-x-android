/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.declineandblock.DeclineInviteAndBlockEntryPoint
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Optional

@RunWith(AndroidJUnit4::class)
class DefaultJoinRoomEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultJoinRoomEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            JoinRoomFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _, _, _ -> createJoinRoomPresenter() },
                acceptDeclineInviteView = { _, _, _, _ -> lambdaError() },
                declineAndBlockEntryPoint = object : DeclineInviteAndBlockEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext, inviteData: InviteData) = lambdaError()
                }

            )
        }
        val inputs = JoinRoomEntryPoint.Inputs(
            roomId = A_ROOM_ID,
            roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
            roomDescription = Optional.ofNullable(null),
            serverNames = emptyList(),
            trigger = JoinedRoom.Trigger.RoomDirectory,
        )
        val result = entryPoint.createNode(parentNode, BuildContext.root(null), inputs)
        assertThat(result).isInstanceOf(JoinRoomFlowNode::class.java)
        assertThat(result.plugins).contains(inputs)
    }
}
