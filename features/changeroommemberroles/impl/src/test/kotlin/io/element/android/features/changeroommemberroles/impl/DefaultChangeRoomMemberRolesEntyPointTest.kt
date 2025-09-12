/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultChangeRoomMemberRolesEntyPointTest {
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultChangeRoomMemberRolesEntyPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ChangeRoomMemberRolesRootNode(
                buildContext = buildContext,
                plugins = plugins,
                roomComponentFactory = { },
            )
        }
        val room = FakeJoinedRoom()
        val listType = ChangeRoomMemberRolesListType.Admins
        val result = entryPoint.builder(parentNode, BuildContext.root(null))
            .room(FakeJoinedRoom())
            .listType(listType)
            .build()
        assertThat(result).isInstanceOf(ChangeRoomMemberRolesRootNode::class.java)
        // Search for the Inputs plugin
        val input = result.plugins.filterIsInstance<ChangeRoomMemberRolesRootNode.Inputs>().single()
        assertThat(input.joinedRoom.roomId).isEqualTo(room.roomId)
        assertThat(input.listType).isEqualTo(listType)
    }
}
