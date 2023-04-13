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

package io.element.android.appnav

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.di.MatrixClientsHolder
import io.element.android.appnav.root.RootPresenter
import io.element.android.appnav.root.RootView
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.deeplink.DeeplinkParser
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.uitests.openShowkase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(AppScope::class)
class RootFlowNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val authenticationService: MatrixAuthenticationService,
    private val matrixClientsHolder: MatrixClientsHolder,
    private val presenter: RootPresenter,
    private val bugReportEntryPoint: BugReportEntryPoint,
    private val deeplinkParser: DeeplinkParser,
) :
    BackstackNode<RootFlowNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.SplashScreen,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {

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
        backstack.safeRoot(NavTarget.LoggedInFlow(sessionId))
    }

    private fun switchToLogoutFlow() {
        matrixClientsHolder.removeAll()
        backstack.safeRoot(NavTarget.NotLoggedInFlow)
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (UserId) -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val latestKnownUserId = authenticationService.getLatestSessionId()
        if (latestKnownUserId == null) {
            onFailure()
            return
        }
        if (matrixClientsHolder.knowSession(latestKnownUserId)) {
            onSuccess(latestKnownUserId)
            return
        }
        authenticationService.restoreSession(UserId(latestKnownUserId.value))
            .onSuccess { matrixClient ->
                matrixClientsHolder.add(matrixClient)
                onSuccess(matrixClient.sessionId)
            }
            .onFailure {
                Timber.v("Failed to restore session...")
                onFailure()
            }
    }

    private fun onOpenBugReport() {
        backstack.push(NavTarget.BugReport)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = LocalContext.current as Activity
        fun openShowkase() {
            openShowkase(activity)
        }

        val state = presenter.present()
        RootView(
            state = state,
            modifier = modifier,
            onOpenBugReport = this::onOpenBugReport,
            onOpenShowkase = ::openShowkase
        ) {
            Children(
                navModel = backstack,
                // Animate opening the bug report screen
                transitionHandler = rememberDefaultTransitionHandler(),
            )
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
                val inputs = LoggedInFlowNode.Inputs(matrixClient)
                val callback = object : LoggedInFlowNode.Callback {
                    override fun onOpenBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }
                }
                val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                createNode<LoggedInFlowNode>(buildContext, plugins = listOf(inputs, callback) + nodeLifecycleCallbacks)
            }
            NavTarget.NotLoggedInFlow -> createNode<NotLoggedInFlowNode>(buildContext)
            NavTarget.SplashScreen -> splashNode(buildContext)
            NavTarget.BugReport -> {
                val callback = object : BugReportEntryPoint.Callback {
                    override fun onBugReportSent() {
                        backstack.pop()
                    }
                }
                bugReportEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
        }
    }

    private fun splashNode(buildContext: BuildContext) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    suspend fun handleIntent(intent: Intent) {
        deeplinkParser.getFromIntent(intent)
            ?.let { navigateTo(it) }
    }

    private suspend fun navigateTo(deeplinkData: DeeplinkData) {
        Timber.d("Navigating to $deeplinkData")
        attachSession(deeplinkData.sessionId)
            .apply {
                val roomId = deeplinkData.roomId
                if (roomId == null) {
                    // In case room is not provided, ensure the app navigate back to the room list
                    attachRoot()
                } else {
                    attachRoom(roomId)
                    // TODO .attachThread(deeplinkData.threadId)
                }
            }
    }

    private suspend fun attachSession(sessionId: SessionId): LoggedInFlowNode {
        return attachChild {
            backstack.newRoot(NavTarget.LoggedInFlow(sessionId))
        }
    }
}
