/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.features.roomdirectory.api.RoomDirectoryEntryPoint
import io.element.android.features.roomdirectory.impl.root.RoomDirectoryNode
import io.element.android.features.roomdirectory.impl.root.createRoomDirectoryPresenter
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultRoomDirectoryEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultRoomDirectoryEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            RoomDirectoryNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = createRoomDirectoryPresenter(),
            )
        }
        val callback = object : RoomDirectoryEntryPoint.Callback {
            override fun navigateToRoom(roomDescription: RoomDescription) = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(RoomDirectoryNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
