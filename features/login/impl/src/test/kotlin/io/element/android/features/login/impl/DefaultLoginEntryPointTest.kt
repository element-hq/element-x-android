/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.oidc.test.customtab.FakeOidcActionFlow
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultLoginEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultLoginEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LoginFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                accountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
                oidcActionFlow = FakeOidcActionFlow(),
            )
        }
        val callback = object : LoginEntryPoint.Callback {
            override fun onReportProblem() = lambdaError()
        }
        val params = LoginEntryPoint.Params(
            accountProvider = "ac",
            loginHint = "lh",
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(LoginFlowNode::class.java)
        assertThat(result.plugins).contains(LoginFlowNode.Params(params.accountProvider, params.loginHint))
        assertThat(result.plugins).contains(callback)
    }
}
