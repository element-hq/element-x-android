/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.ShowLocationEntryPoint
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.FakePermissionsPresenter
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultShowLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultShowLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ShowLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { location: Location, description: String? ->
                    ShowLocationPresenter(
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        location = location,
                        description = description,
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val inputs = ShowLocationEntryPoint.Inputs(
            location = Location(37.4219983, -122.084, 10f),
            description = "My location",
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(ShowLocationNode::class.java)
        assertThat(result.plugins).contains(inputs)
    }
}
