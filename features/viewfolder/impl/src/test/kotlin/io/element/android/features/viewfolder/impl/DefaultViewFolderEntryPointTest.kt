/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.root.ViewFolderRootNode
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultViewFolderEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultViewFolderEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ViewFolderRootNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : ViewFolderEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val params = ViewFolderEntryPoint.Params(
            rootPath = "path",
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(ViewFolderRootNode::class.java)
        assertThat(result.plugins).contains(ViewFolderRootNode.Inputs(params.rootPath))
        assertThat(result.plugins).contains(callback)
    }
}
