/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultAnalyticsEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node creation`() {
        val entryPoint = DefaultAnalyticsEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            AnalyticsOptInNode(
                buildContext = buildContext,
                plugins = plugins,
                AnalyticsOptInPresenter(
                    buildMeta = aBuildMeta(),
                    analyticsService = FakeAnalyticsService()
                )
            )
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null))
        assertThat(result).isInstanceOf(AnalyticsOptInNode::class.java)
    }
}
