/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultOutgoingVerificationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultOutgoingVerificationEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            OutgoingVerificationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _ ->
                    createOutgoingVerificationPresenter()
                }
            )
        }
        val callback = object : OutgoingVerificationEntryPoint.Callback {
            override fun navigateToLearnMoreAboutEncryption() = lambdaError()
            override fun onBack() = lambdaError()
            override fun onDone() = lambdaError()
        }
        val params = OutgoingVerificationEntryPoint.Params(
            showDeviceVerifiedScreen = true,
            verificationRequest = anOutgoingSessionVerificationRequest(),
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(OutgoingVerificationNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}
