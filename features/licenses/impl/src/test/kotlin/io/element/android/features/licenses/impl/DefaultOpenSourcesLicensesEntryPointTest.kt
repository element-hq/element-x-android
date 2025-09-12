/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultOpenSourcesLicensesEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultOpenSourcesLicensesEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            DependenciesFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val result = entryPoint.getNode(parentNode, BuildContext.root(null))
        assertThat(result).isInstanceOf(DependenciesFlowNode::class.java)
    }
}
