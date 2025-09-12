/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultSpaceEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultSpaceEntryPoint()
        val nodeInputs = SpaceEntryPoint.Inputs(A_ROOM_ID)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SpaceNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { inputs ->
                    assertThat(inputs).isEqualTo(nodeInputs)
                    SpacePresenter(
                        inputs = inputs,
                        client = FakeMatrixClient(
                            spaceService = FakeSpaceService(
                                spaceRoomListResult = { FakeSpaceRoomList() },
                            )
                        ),
                        seenInvitesStore = InMemorySeenInvitesStore(),
                    )
                },
            )
        }
        val callback = object : SpaceEntryPoint.Callback {
            override fun onOpenRoom(roomId: RoomId) {
                lambdaError()
            }
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .inputs(nodeInputs)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(SpaceNode::class.java)
        assertThat(result.plugins).contains(nodeInputs)
        assertThat(result.plugins).contains(callback)
    }
}
