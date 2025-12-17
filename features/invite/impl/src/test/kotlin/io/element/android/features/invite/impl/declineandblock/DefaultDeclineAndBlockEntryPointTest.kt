/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.test.anInviteData
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultDeclineAndBlockEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultDeclineAndBlockEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            DeclineAndBlockNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { inviteData -> createDeclineAndBlockPresenter() }
            )
        }
        val inviteData = anInviteData()
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inviteData = inviteData
        )
        assertThat(result).isInstanceOf(DeclineAndBlockNode::class.java)
        assertThat(result.plugins).contains(DeclineAndBlockNode.Inputs(inviteData))
    }
}
