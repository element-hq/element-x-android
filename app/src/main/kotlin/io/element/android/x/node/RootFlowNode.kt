/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.features.rageshake.bugreport.BugReportNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.matrix.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.x.di.MatrixClientsHolder
import io.element.android.x.root.RootPresenter
import io.element.android.x.root.RootView
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class RootFlowNode(
    private val buildContext: BuildContext,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
    private val appComponentOwner: DaggerComponentOwner,
    private val authenticationService: MatrixAuthenticationService,
    private val matrixClientsHolder: MatrixClientsHolder,
    private val presenter: RootPresenter
) :
    ParentNode<RootFlowNode.NavTarget>(
        navModel = backstack,
        buildContext = buildContext
    ),

    DaggerComponentOwner by appComponentOwner {

    override fun onBuilt() {
        super.onBuilt()
        observeLoggedInState()
    }

    private fun observeLoggedInState() {
        authenticationService.isLoggedIn()
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                Timber.v("isLoggedIn=$isLoggedIn")
                if (isLoggedIn) {
                    tryToRestoreLatestSession(
                        onSuccess = { switchToLoggedInFlow(it) },
                        onFailure = { switchToLogoutFlow() }
                    )
                } else {
                    switchToLogoutFlow()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun switchToLoggedInFlow(sessionId: SessionId) {
        backstack.safeRoot(NavTarget.LoggedInFlow(sessionId = sessionId))
    }

    private fun switchToLogoutFlow() {
        matrixClientsHolder.removeAll()
        backstack.safeRoot(NavTarget.NotLoggedInFlow)
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (SessionId) -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val latestKnownSessionId = authenticationService.getLatestSessionId()
        if (latestKnownSessionId == null) {
            onFailure()
            return
        }
        if (matrixClientsHolder.knowSession(latestKnownSessionId)) {
            onSuccess(latestKnownSessionId)
            return
        }
        val matrixClient = authenticationService.restoreSession(latestKnownSessionId)
        if (matrixClient == null) {
            Timber.v("Failed to restore session...")
            onFailure()
        } else {
            matrixClientsHolder.add(matrixClient)
            onSuccess(matrixClient.sessionId)
        }
    }

    private fun onOpenBugReport() {
        backstack.push(NavTarget.BugReport)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RootView(
            state = state,
            modifier = modifier,
            onOpenBugReport = this::onOpenBugReport,
        ) {
            Children(
                navModel = backstack,
                // Animate opening the bug report screen
                transitionHandler = rememberDefaultTransitionHandler(),
            )
        }
    }

    private val bugReportNodeCallback = object : BugReportNode.Callback {
        override fun onBugReportSent() {
            backstack.pop()
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object SplashScreen : NavTarget

        @Parcelize
        object NotLoggedInFlow : NavTarget

        @Parcelize
        data class LoggedInFlow(val sessionId: SessionId) : NavTarget

        @Parcelize
        object BugReport : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                val matrixClient = matrixClientsHolder.getOrNull(navTarget.sessionId) ?: return splashNode(buildContext).also {
                    Timber.w("Couldn't find any session, go through SplashScreen")
                    backstack.newRoot(NavTarget.SplashScreen)
                }
                LoggedInFlowNode(
                    buildContext = buildContext,
                    sessionId = navTarget.sessionId,
                    matrixClient = matrixClient,
                    onOpenBugReport = this::onOpenBugReport
                )
            }
            NavTarget.NotLoggedInFlow -> NotLoggedInFlowNode(buildContext)
            NavTarget.SplashScreen -> splashNode(buildContext)
            NavTarget.BugReport -> createNode<BugReportNode>(buildContext, plugins = listOf(bugReportNodeCallback))
        }
    }

    private fun splashNode(buildContext: BuildContext) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
