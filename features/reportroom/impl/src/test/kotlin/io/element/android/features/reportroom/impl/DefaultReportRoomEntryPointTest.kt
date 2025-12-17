/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultReportRoomEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultReportRoomEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ReportRoomNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { roomId ->
                    assertThat(roomId).isEqualTo(A_ROOM_ID)
                    createReportRoomPresenter()
                }
            )
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            roomId = A_ROOM_ID,
        )
        assertThat(result).isInstanceOf(ReportRoomNode::class.java)
        assertThat(result.plugins).contains(ReportRoomNode.Inputs(A_ROOM_ID))
    }
}
