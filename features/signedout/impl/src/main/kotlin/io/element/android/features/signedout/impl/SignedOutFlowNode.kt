/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.backpresshandlerstrategies.DontHandleBackPress
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.Remove
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

/**
 * Display a [SignedOutNode] for each session whose token has become invalid, one after the other.
 *
 * Each newly signed out session is pushed on top of the ones already displayed. When several sessions are signed out at once,
 * the most recently used one is pushed last, so it is displayed first. When a session is removed from the [SessionStore] (by
 * the user confirming the signed out screen), its node is popped and the next one, if any, is displayed.
 *
 * The root of the backstack is an empty node, so nothing is rendered when there is no signed out session.
 */
@ContributesNode(AppScope::class)
@AssistedInject
class SignedOutFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val sessionStore: SessionStore,
) : BaseFlowNode<SignedOutFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Empty,
        savedStateMap = buildContext.savedStateMap,
        // A signed out screen can only be left by removing its session, the back key is handled by the screen itself.
        backPressHandler = DontHandleBackPress(),
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Empty : NavTarget

        @Parcelize
        data class SignedOut(val sessionId: SessionId) : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        sessionStore.sessionsFlow()
            .map { sessions -> sessions.filter { !it.isTokenValid }.map { SessionId(it.userId) } }
            .distinctUntilChanged()
            .onEach(::onSignedOutSessionsChange)
            .launchIn(lifecycleScope)
    }

    /**
     * @param signedOutSessionIds the signed out sessions, ordered from the most recently used to the least recently used.
     */
    private fun onSignedOutSessionsChange(signedOutSessionIds: List<SessionId>) {
        val displayedSessionIds = displayedSessionIds()
        // Remove the screens of the sessions which have been removed
        backstack.elements.value
            .filter { element ->
                val navTarget = element.key.navTarget
                element.targetState != BackStack.State.DESTROYED && navTarget is NavTarget.SignedOut && navTarget.sessionId !in signedOutSessionIds
            }
            .forEach { element -> backstack.accept(Remove(element.key)) }
        // Push the newly signed out sessions, the most recently used one last so that it is on top
        signedOutSessionIds
            .filter { it !in displayedSessionIds }
            .asReversed()
            .forEach { sessionId ->
                Timber.d("Session $sessionId has been signed out, display the signed out screen")
                backstack.push(NavTarget.SignedOut(sessionId))
            }
    }

    private fun displayedSessionIds(): List<SessionId> = backstack.elements.value
        .filter { it.targetState != BackStack.State.DESTROYED }
        .mapNotNull { (it.key.navTarget as? NavTarget.SignedOut)?.sessionId }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Empty -> node(buildContext) {}
            is NavTarget.SignedOut -> createNode<SignedOutNode>(
                buildContext = buildContext,
                plugins = listOf(SignedOutNode.Inputs(navTarget.sessionId)),
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier = modifier)
    }
}
