/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.di.MatrixSessionCache
import io.element.android.appnav.root.RootNavStateFlowFactory
import io.element.android.appnav.store.getLatestSessionId
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(AppScope::class)
@Inject
class LoggedInAccountSwitcherFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val navStateFlowFactory: RootNavStateFlowFactory,
    private val sessionStore: SessionStore,
    private val matrixSessionCache: MatrixSessionCache,
) : BaseFlowNode<LoggedInAccountSwitcherFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onOpenBugReport()
        fun onAddAccount()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class LoggedInFlow(
            val sessionId: SessionId,
            val navId: Int
        ) : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        observeNavState()
    }

    private fun observeNavState() {
        navStateFlowFactory.create(buildContext.savedStateMap)
            .distinctUntilChanged()
            .onEach { navState ->
                Timber.v("navState=$navState")
                if (navState.loggedInState is LoggedInState.LoggedIn && navState.loggedInState.isTokenValid) {
                    tryToRestoreLatestSession(
                        onSuccess = { sessionId -> switchToLoggedInFlow(sessionId, navState.cacheIndex) },
                        onFailure = { },
                    )
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun switchToLoggedInFlow(sessionId: SessionId, navId: Int) {
        backstack.safeRoot(NavTarget.LoggedInFlow(sessionId, navId))
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (SessionId) -> Unit,
        onFailure: () -> Unit
    ) {
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            onFailure()
            return
        }
        restoreSessionIfNeeded(latestSessionId, onFailure, onSuccess)
    }

    private suspend fun restoreSessionIfNeeded(
        sessionId: SessionId,
        onFailure: () -> Unit,
        onSuccess: (SessionId) -> Unit,
    ) {
        matrixSessionCache.getOrRestore(sessionId)
            .onSuccess {
                Timber.v("Succeed to restore session $sessionId")
                onSuccess(sessionId)
            }
            .onFailure {
                Timber.e(it, "Failed to restore session $sessionId")
                onFailure()
            }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> node(buildContext) {}
            is NavTarget.LoggedInFlow -> {
                val matrixClient = matrixSessionCache.getOrNull(navTarget.sessionId) ?: return node(buildContext) {}.also {
                    Timber.w("Couldn't find any session, go through SplashScreen")
                }
                val inputs = LoggedInAppScopeFlowNode.Inputs(matrixClient)
                val callback = object : LoggedInAppScopeFlowNode.Callback {
                    override fun onOpenBugReport() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onAddAccount() {
                        plugins<Callback>().forEach { it.onAddAccount() }
                    }
                }
                createNode<LoggedInAppScopeFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
        }
    }

    suspend fun attachSession(sessionId: SessionId): LoggedInFlowNode {
        return waitForChildAttached<LoggedInAppScopeFlowNode, NavTarget> { navTarget ->
            navTarget is NavTarget.LoggedInFlow && navTarget.sessionId == sessionId
        }
            .attachSession()
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(
            transitionHandler = rememberBackstackFader()
        )
    }
}
