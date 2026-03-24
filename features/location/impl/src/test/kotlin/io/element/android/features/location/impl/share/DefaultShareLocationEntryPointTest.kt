/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.FakePermissionsPresenter
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.dateformatter.test.FakeDurationFormatter
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultShareLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultShareLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ShareLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { timelineMode: Timeline.Mode ->
                    ShareLocationPresenter(
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        room = FakeJoinedRoom(),
                        timelineMode = timelineMode,
                        analyticsService = FakeAnalyticsService(),
                        messageComposerContext = FakeMessageComposerContext(),
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        featureFlagService = FakeFeatureFlagService(),
                        client = FakeMatrixClient(),
                        durationFormatter = FakeDurationFormatter(),
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val timelineMode = Timeline.Mode.Live
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            timelineMode = timelineMode,
        )
        assertThat(result).isInstanceOf(ShareLocationNode::class.java)
        assertThat(result.plugins).contains(ShareLocationNode.Inputs(timelineMode))
    }
}
