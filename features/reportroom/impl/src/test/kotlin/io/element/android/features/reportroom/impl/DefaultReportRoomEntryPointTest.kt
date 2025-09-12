/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultReportRoomEntryPointTest {
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
        val result = entryPoint.createNode(parentNode, BuildContext.root(null), A_ROOM_ID)
        assertThat(result).isInstanceOf(ReportRoomNode::class.java)
        assertThat(result.plugins).contains(ReportRoomNode.Inputs(A_ROOM_ID))
    }
}
