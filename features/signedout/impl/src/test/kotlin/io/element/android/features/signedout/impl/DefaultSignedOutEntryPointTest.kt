/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.signedout.api.SignedOutEntryPoint
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultSignedOutEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultSignedOutEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SignedOutNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { sessionId ->
                    assertThat(sessionId).isEqualTo(A_SESSION_ID)
                    createSignedOutPresenter()
                }
            )
        }
        val params = SignedOutEntryPoint.Params(A_SESSION_ID)
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
        )
        assertThat(result).isInstanceOf(SignedOutNode::class.java)
        assertThat(result.plugins).contains(SignedOutNode.Inputs(params.sessionId))
    }
}
