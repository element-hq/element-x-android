/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.startchat.api.StartChatEntryPoint
import io.element.android.features.startchat.impl.root.StartChatNode
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultStartChatEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultStartChatEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            StartChatFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                createRoomEntryPoint = object : CreateRoomEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
            )
        }
        val callback = object : StartChatEntryPoint.Callback {
            override fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) = lambdaError()
            override fun onOpenRoomDirectory() = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(StartChatFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
