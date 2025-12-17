/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.oidc.test.customtab.FakeOidcActionFlow
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultLoginEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultLoginEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LoginFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                accountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
                oidcActionFlow = FakeOidcActionFlow(),
                appCoroutineScope = backgroundScope,
            )
        }
        val callback = object : LoginEntryPoint.Callback {
            override fun navigateToBugReport() = lambdaError()
            override fun onDone() = lambdaError()
        }
        val params = LoginEntryPoint.Params(
            accountProvider = "ac",
            loginHint = "lh",
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(LoginFlowNode::class.java)
        assertThat(result.plugins).contains(LoginFlowNode.Params(params.accountProvider, params.loginHint))
        assertThat(result.plugins).contains(callback)
    }
}
