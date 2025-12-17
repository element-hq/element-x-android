/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultRoomSelectEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultRoomSelectEntryPoint()
        val testMode = RoomSelectMode.Share
        val parentNode = TestParentNode.create { buildContext, plugins ->
            RoomSelectNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { mode ->
                    assertThat(mode).isEqualTo(testMode)
                    createRoomSelectPresenter(mode)
                },
            )
        }
        val callback = object : RoomSelectEntryPoint.Callback {
            override fun onRoomSelected(roomIds: List<RoomId>) = lambdaError()
            override fun onCancel() = lambdaError()
        }
        val params = RoomSelectEntryPoint.Params(testMode)
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(RoomSelectNode::class.java)
        assertThat(result.plugins).contains(RoomSelectNode.Inputs(params.mode))
        assertThat(result.plugins).contains(callback)
    }
}
