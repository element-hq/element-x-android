/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.appnav

import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.intent.IntentResolver
import io.element.android.appnav.root.RootNavStateFlowFactory
import io.element.android.appnav.root.RootPresenter
import io.element.android.appnav.session.FakeSyncOrchestratorFactory
import io.element.android.appnav.session.MatrixSessionCache
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.api.LoginParams
import io.element.android.features.login.test.FakeLoginEntryPoint
import io.element.android.features.login.test.FakeLoginIntentResolver
import io.element.android.features.login.test.accesscontrol.FakeAccountProviderAccessControl
import io.element.android.features.preferences.test.FakeCacheService
import io.element.android.features.rageshake.test.FakeBugReportEntryPoint
import io.element.android.features.rageshake.test.logs.FakeAnnouncementService
import io.element.android.features.share.test.FakeShareIntentHandler
import io.element.android.features.signedout.test.FakeSignedOutEntryPoint
import io.element.android.libraries.accountselect.test.FakeAccountSelectEntryPoint
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
import io.element.android.libraries.oauth.test.FakeOAuthActionFlow
import io.element.android.libraries.oauth.test.FakeOAuthIntentResolver
import io.element.android.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.analytics.test.watchers.FakeAnalyticsColdStartWatcher
import io.element.android.services.apperror.test.FakeAppErrorStateService
import io.element.android.tests.testutils.node.FakeNodeFactoriesBindings
import io.element.android.tests.testutils.node.FakeParentNode
import io.element.android.tests.testutils.presenter.NotUsedPresenter
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val A_LOGIN_LINK = "https://mobile.element.io/element/?account_provider=example.com&login_hint=mxid:@alice:example.com"

private val A_LOGIN_PARAMS = LoginParams(
    accountProvider = "example.com",
    loginHint = "mxid:@alice:example.com",
)

private val A_LOGIN_ENTRY_POINT_PARAMS = LoginEntryPoint.Params(
    accountProvider = A_LOGIN_PARAMS.accountProvider,
    loginHint = A_LOGIN_PARAMS.loginHint,
)

class RootFlowNodeTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given no session, when a login link is handled before the nav state is observed, then the login params are kept`() = runTest {
        var loginEntryPointParams: LoginEntryPoint.Params? = null
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint { buildContext, params ->
                loginEntryPointParams = params
                node(buildContext) {}
            },
        )
        rootFlowNode.parentNodeTestHelper()
        // The intent is handled first, this is what happens when the deep link cold starts the app.
        rootFlowNode.handleIntent(aLoginIntent())
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        // Then the first nav state emission lands, it must not override the login params.
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        assertThat(loginEntryPointParams).isEqualTo(A_LOGIN_ENTRY_POINT_PARAMS)
    }

    @Test
    fun `given no session, when a login link is handled after the nav state is observed, then the login params are applied`() = runTest {
        var loginEntryPointParams: LoginEntryPoint.Params? = null
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint { buildContext, params ->
                loginEntryPointParams = params
                node(buildContext) {}
            },
        )
        rootFlowNode.parentNodeTestHelper()
        // The nav state is observed first, this is what happens when the app is already running.
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(null))
        rootFlowNode.handleIntent(aLoginIntent())
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        assertThat(loginEntryPointParams).isEqualTo(A_LOGIN_ENTRY_POINT_PARAMS)
    }

    private fun aLoginIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(A_LOGIN_LINK))

    private fun TestScope.createRootFlowNode(
        loginEntryPoint: LoginEntryPoint,
        sessionStore: SessionStore = InMemorySessionStore(),
    ): RootFlowNode {
        val matrixSessionCache = MatrixSessionCache(
            authenticationService = FakeMatrixAuthenticationService(),
            syncOrchestratorFactory = FakeSyncOrchestratorFactory(),
            analyticsService = FakeAnalyticsService(),
        )
        val parentNode = FakeParentNode(
            graph = FakeNodeFactoriesBindings(
                mapOf(
                    NotLoggedInFlowNode::class to AssistedNodeFactory { buildContext, plugins ->
                        NotLoggedInFlowNode(
                            buildContext = buildContext,
                            plugins = plugins,
                            loginEntryPoint = loginEntryPoint,
                            imageLoaderHolder = FakeImageLoaderHolder(),
                            analyticsColdStartWatcher = FakeAnalyticsColdStartWatcher(),
                        )
                    }
                )
            )
        )
        return RootFlowNode(
            buildContext = BuildContext(
                ancestryInfo = AncestryInfo.Child(anchor = parentNode),
                savedStateMap = null,
                customisations = NodeCustomisationDirectoryImpl(),
            ),
            plugins = emptyList(),
            sessionStore = sessionStore,
            accountProviderAccessControl = FakeAccountProviderAccessControl { true },
            navStateFlowFactory = RootNavStateFlowFactory(
                sessionStore = sessionStore,
                cacheService = FakeCacheService(),
                matrixSessionCache = matrixSessionCache,
                imageLoaderHolder = FakeImageLoaderHolder(),
                sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(),
            ),
            matrixSessionCache = matrixSessionCache,
            presenter = RootPresenter(
                crashDetectionPresenter = NotUsedPresenter(),
                rageshakeDetectionPresenter = NotUsedPresenter(),
                appErrorStateService = FakeAppErrorStateService(),
                analyticsService = FakeAnalyticsService(),
                sdkMetadata = FakeSdkMetadata("sha"),
            ),
            bugReportEntryPoint = FakeBugReportEntryPoint(),
            signedOutEntryPoint = FakeSignedOutEntryPoint(),
            accountSelectEntryPoint = FakeAccountSelectEntryPoint(),
            intentResolver = IntentResolver(
                deeplinkParser = { null },
                loginIntentResolver = FakeLoginIntentResolver { A_LOGIN_PARAMS },
                oAuthIntentResolver = FakeOAuthIntentResolver { null },
                permalinkParser = FakePermalinkParser(),
                shareIntentHandler = FakeShareIntentHandler(),
            ),
            oAuthActionFlow = FakeOAuthActionFlow(),
            featureFlagService = FakeFeatureFlagService(),
            announcementService = FakeAnnouncementService(),
            analyticsService = FakeAnalyticsService(),
            analyticsColdStartWatcher = FakeAnalyticsColdStartWatcher(),
            appCoroutineScope = backgroundScope,
        )
    }
}
