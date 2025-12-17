/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.test.declineandblock.FakeDeclineInviteAndBlockEntryPoint
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test
import java.util.Optional

class DefaultJoinRoomEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultJoinRoomEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            JoinRoomFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _, _, _ -> createJoinRoomPresenter() },
                acceptDeclineInviteView = { _, _, _, _ -> lambdaError() },
                declineAndBlockEntryPoint = FakeDeclineInviteAndBlockEntryPoint(),
            )
        }
        val inputs = JoinRoomEntryPoint.Inputs(
            roomId = A_ROOM_ID,
            roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
            roomDescription = Optional.ofNullable(null),
            serverNames = emptyList(),
            trigger = JoinedRoom.Trigger.RoomDirectory,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(JoinRoomFlowNode::class.java)
        assertThat(result.plugins).contains(inputs)
    }
}
