/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultCreateRoomEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultCreateRoomEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            CreateRoomFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : CreateRoomEntryPoint.Callback {
            override fun onRoomCreated(roomId: RoomId) = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result.plugins).contains(callback)
    }
}
