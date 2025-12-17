/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.features.poll.impl.data.PollRepository
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.LiveTimelineProvider
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultCreatePollEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultCreatePollEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            CreatePollNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { timelineMode: Timeline.Mode, backNavigator: () -> Unit, mode: CreatePollMode ->
                    CreatePollPresenter(
                        repositoryFactory = {
                            val room = FakeJoinedRoom()
                            PollRepository(room, LiveTimelineProvider(room), timelineMode)
                        },
                        analyticsService = FakeAnalyticsService(),
                        messageComposerContext = FakeMessageComposerContext(),
                        navigateUp = backNavigator,
                        mode = mode,
                        timelineMode = timelineMode,
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val params = CreatePollEntryPoint.Params(
            timelineMode = Timeline.Mode.Live,
            mode = CreatePollMode.NewPoll,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
        )
        assertThat(result).isInstanceOf(CreatePollNode::class.java)
        assertThat(result.plugins).contains(CreatePollNode.Inputs(timelineMode = params.timelineMode, mode = params.mode))
    }
}
