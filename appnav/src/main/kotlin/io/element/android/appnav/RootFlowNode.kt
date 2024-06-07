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

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.state.MutableSavedStateMap
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.di.MatrixClientsHolder
import io.element.android.appnav.intent.IntentResolver
import io.element.android.appnav.intent.ResolvedIntent
import io.element.android.appnav.root.RootNavStateFlowFactory
import io.element.android.appnav.root.RootPresenter
import io.element.android.appnav.root.RootView
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.api.oidc.OidcActionFlow
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.features.signedout.api.SignedOutEntryPoint
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.sessionstorage.api.LoggedInState
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
    private val navStateFlowFactory: RootNavStateFlowFactory,
    private val matrixClientsHolder: MatrixClientsHolder,
    private val presenter: RootPresenter,
    private val bugReportEntryPoint: BugReportEntryPoint,
    private val viewFolderEntryPoint: ViewFolderEntryPoint,
    private val signedOutEntryPoint: SignedOutEntryPoint,
    private val intentResolver: IntentResolver,
    private val oidcActionFlow: OidcActionFlow,
) : BaseFlowNode<RootFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    override fun onBuilt() {
        matrixClientsHolder.restoreWithSavedState(buildContext.savedStateMap)
        super.onBuilt()
        observeNavState()
    }

    override fun onSaveInstanceState(state: MutableSavedStateMap) {
        super.onSaveInstanceState(state)
        matrixClientsHolder.saveIntoSavedState(state)
        navStateFlowFactory.saveIntoSavedState(state)
    }

    private fun observeNavState() {
        navStateFlowFactory.create(buildContext.savedStateMap)
            .distinctUntilChanged()
            .onEach { navState ->
                Timber.v("navState=$navState")
                when (navState.loggedInState) {
                    is LoggedInState.LoggedIn -> {
                        if (navState.loggedInState.isTokenValid) {
                            tryToRestoreLatestSession(
                                onSuccess = { sessionId -> switchToLoggedInFlow(sessionId, navState.cacheIndex) },
                                onFailure = { switchToNotLoggedInFlow() }
                            )
                        } else {
                            switchToSignedOutFlow(SessionId(navState.loggedInState.sessionId))
                        }
                    }
                    LoggedInState.NotLoggedIn -> {
                        switchToNotLoggedInFlow()
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun switchToLoggedInFlow(sessionId: SessionId, navId: Int) {
        backstack.safeRoot(NavTarget.LoggedInFlow(sessionId, navId))
    }

    private fun switchToNotLoggedInFlow() {
        matrixClientsHolder.removeAll()
        backstack.safeRoot(NavTarget.NotLoggedInFlow)
    }

    private fun switchToSignedOutFlow(sessionId: SessionId) {
        backstack.safeRoot(NavTarget.SignedOutFlow(sessionId))
    }

    private suspend fun restoreSessionIfNeeded(
        sessionId: SessionId,
        onFailure: () -> Unit,
        onSuccess: (SessionId) -> Unit,
    ) {
        matrixClientsHolder.getOrRestore(sessionId)
            .onSuccess {
                Timber.v("Succeed to restore session $sessionId")
                onSuccess(sessionId)
            }
            .onFailure {
                Timber.e(it, "Failed to restore session $sessionId")
                onFailure()
            }
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (SessionId) -> Unit,
        onFailure: () -> Unit
    ) {
        val latestSessionId = authenticationService.getLatestSessionId()
        if (latestSessionId == null) {
            onFailure()
            return
        }
        restoreSessionIfNeeded(latestSessionId, onFailure, onSuccess)
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
            BackstackView()
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object SplashScreen : NavTarget

        @Parcelize
        data object NotLoggedInFlow : NavTarget

        @Parcelize
        data class LoggedInFlow(
            val sessionId: SessionId,
            val navId: Int
        ) : NavTarget

        @Parcelize
        data class SignedOutFlow(
            val sessionId: SessionId
        ) : NavTarget

        @Parcelize
        data object BugReport : NavTarget

        @Parcelize
        data class ViewLogs(
            val rootPath: String,
        ) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                val matrixClient = matrixClientsHolder.getOrNull(navTarget.sessionId) ?: return splashNode(buildContext).also {
                    Timber.w("Couldn't find any session, go through SplashScreen")
                }
                val inputs = LoggedInAppScopeFlowNode.Inputs(matrixClient)
                val callback = object : LoggedInAppScopeFlowNode.Callback {
                    override fun onOpenBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }
                }
                createNode<LoggedInAppScopeFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
            NavTarget.NotLoggedInFlow -> {
                val callback = object : NotLoggedInFlowNode.Callback {
                    override fun onOpenBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }
                }
                createNode<NotLoggedInFlowNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.SignedOutFlow -> {
                signedOutEntryPoint.nodeBuilder(this, buildContext)
                    .params(
                        SignedOutEntryPoint.Params(
                            sessionId = navTarget.sessionId
                        )
                    )
                    .build()
            }
            NavTarget.SplashScreen -> splashNode(buildContext)
            NavTarget.BugReport -> {
                val callback = object : BugReportEntryPoint.Callback {
                    override fun onBugReportSent() {
                        backstack.pop()
                    }

                    override fun onViewLogs(basePath: String) {
                        backstack.push(NavTarget.ViewLogs(rootPath = basePath))
                    }
                }
                bugReportEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.ViewLogs -> {
                val callback = object : ViewFolderEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                val params = ViewFolderEntryPoint.Params(
                    rootPath = navTarget.rootPath,
                )
                viewFolderEntryPoint
                    .nodeBuilder(this, buildContext)
                    .params(params)
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
        val resolvedIntent = intentResolver.resolve(intent) ?: return
        when (resolvedIntent) {
            is ResolvedIntent.Navigation -> navigateTo(resolvedIntent.deeplinkData)
            is ResolvedIntent.Oidc -> onOidcAction(resolvedIntent.oidcAction)
            is ResolvedIntent.Permalink -> navigateTo(resolvedIntent.permalinkData)
            is ResolvedIntent.IncomingShare -> onIncomingShare(resolvedIntent.intent)
        }
    }

    private suspend fun onIncomingShare(intent: Intent) {
        // Is there a session already?
        val latestSessionId = authenticationService.getLatestSessionId()
        if (latestSessionId == null) {
            // No session, open login
            switchToNotLoggedInFlow()
        } else {
            attachSession(latestSessionId)
                .attachIncomingShare(intent)
        }
    }

    private suspend fun navigateTo(permalinkData: PermalinkData) {
        Timber.d("Navigating to $permalinkData")
        attachSession(null)
            .apply {
                when (permalinkData) {
                    is PermalinkData.FallbackLink -> Unit
                    is PermalinkData.RoomEmailInviteLink -> Unit
                    is PermalinkData.RoomLink -> {
                        attachRoom(
                            roomIdOrAlias = permalinkData.roomIdOrAlias,
                            trigger = JoinedRoom.Trigger.MobilePermalink,
                            serverNames = permalinkData.viaParameters,
                            eventId = permalinkData.eventId,
                        )
                    }
                    is PermalinkData.UserLink -> {
                        attachUser(permalinkData.userId)
                    }
                }
            }
    }

    private suspend fun navigateTo(deeplinkData: DeeplinkData) {
        Timber.d("Navigating to $deeplinkData")
        attachSession(deeplinkData.sessionId)
            .apply {
                when (deeplinkData) {
                    is DeeplinkData.Root -> Unit // The room list will always be shown, observing FtueState
                    is DeeplinkData.Room -> attachRoom(deeplinkData.roomId.toRoomIdOrAlias())
                }
            }
    }

    private fun onOidcAction(oidcAction: OidcAction) {
        oidcActionFlow.post(oidcAction)
    }

    // [sessionId] will be null for permalink.
    private suspend fun attachSession(sessionId: SessionId?): LoggedInFlowNode {
        // TODO handle multi-session
        return waitForChildAttached<LoggedInAppScopeFlowNode, NavTarget> { navTarget ->
            navTarget is NavTarget.LoggedInFlow && (sessionId == null || navTarget.sessionId == sessionId)
        }
            .attachSession()
    }
}
