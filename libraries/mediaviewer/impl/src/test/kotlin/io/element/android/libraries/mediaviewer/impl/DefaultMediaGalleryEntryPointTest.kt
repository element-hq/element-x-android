/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.gallery.root.MediaGalleryFlowNode
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultMediaGalleryEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultMediaGalleryEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MediaGalleryFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                mediaViewerEntryPoint = object : MediaViewerEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                }
            )
        }
        val callback = object : MediaGalleryEntryPoint.Callback {
            override fun onBackClick() = lambdaError()
            override fun onViewInTimeline(eventId: EventId) = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(MediaGalleryFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
