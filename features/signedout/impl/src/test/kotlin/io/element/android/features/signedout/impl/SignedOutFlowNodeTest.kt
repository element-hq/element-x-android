/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.features.signedout.impl.SignedOutFlowNode.NavTarget
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.node.FakeNodeFactoriesBindings
import io.element.android.tests.testutils.node.FakeParentNode
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val ALICE = "@alice:example.com"
private const val BOB = "@bob:example.com"
private const val CHARLIE = "@charlie:example.com"

class SignedOutFlowNodeTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given no signed out session, then nothing is displayed`() = runTest {
        val flowNode = createFlowNode(
            InMemorySessionStore(initialList = listOf(aSessionData(sessionId = ALICE, isTokenValid = true))),
        )
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(NavTarget.Empty)
    }

    @Test
    fun `given signed out sessions at startup, then the most recently used one is on top`() = runTest {
        val flowNode = createFlowNode(
            InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = ALICE, isTokenValid = false),
                    aSessionData(sessionId = BOB, isTokenValid = true),
                    aSessionData(sessionId = CHARLIE, isTokenValid = false),
                ),
            ),
        )
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(
            NavTarget.Empty,
            NavTarget.SignedOut(SessionId(CHARLIE)),
            NavTarget.SignedOut(SessionId(ALICE)),
        ).inOrder()
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.SignedOut(SessionId(ALICE)))
    }

    @Test
    fun `when sessions are signed out later, then they are displayed on top of the displayed one`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = ALICE, isTokenValid = true),
                aSessionData(sessionId = BOB, isTokenValid = true),
                aSessionData(sessionId = CHARLIE, isTokenValid = true),
            ),
        )
        val flowNode = createFlowNode(sessionStore)
        runCurrent()
        sessionStore.updateData(aSessionData(sessionId = BOB, isTokenValid = false))
        runCurrent()
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.SignedOut(SessionId(BOB)))
        sessionStore.updateData(aSessionData(sessionId = ALICE, isTokenValid = false))
        runCurrent()
        sessionStore.updateData(aSessionData(sessionId = CHARLIE, isTokenValid = false))
        runCurrent()
        // Another emission must not duplicate the screens
        sessionStore.updateData(aSessionData(sessionId = BOB, isTokenValid = false, deviceId = "anotherDeviceId"))
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(
            NavTarget.Empty,
            NavTarget.SignedOut(SessionId(BOB)),
            NavTarget.SignedOut(SessionId(ALICE)),
            NavTarget.SignedOut(SessionId(CHARLIE)),
        ).inOrder()
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.SignedOut(SessionId(CHARLIE)))
    }

    @Test
    fun `when the displayed session is removed, then the next one is displayed`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = ALICE, isTokenValid = false),
                aSessionData(sessionId = BOB, isTokenValid = false),
            ),
        )
        val flowNode = createFlowNode(sessionStore)
        runCurrent()
        sessionStore.removeSession(ALICE)
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(NavTarget.Empty, NavTarget.SignedOut(SessionId(BOB))).inOrder()
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.SignedOut(SessionId(BOB)))
        sessionStore.removeSession(BOB)
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(NavTarget.Empty)
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.Empty)
    }

    @Test
    fun `when a queued session is removed, then its screen is removed`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = ALICE, isTokenValid = false),
                aSessionData(sessionId = BOB, isTokenValid = false),
            ),
        )
        val flowNode = createFlowNode(sessionStore)
        runCurrent()
        sessionStore.removeSession(BOB)
        runCurrent()
        assertThat(flowNode.navTargets()).containsExactly(NavTarget.Empty, NavTarget.SignedOut(SessionId(ALICE))).inOrder()
        assertThat(flowNode.backstack.activeElement).isEqualTo(NavTarget.SignedOut(SessionId(ALICE)))
    }

    private fun SignedOutFlowNode.navTargets() = backstack.elements.value
        .filter { it.targetState != BackStack.State.DESTROYED }
        .map { it.key.navTarget }

    private fun createFlowNode(sessionStore: InMemorySessionStore): SignedOutFlowNode {
        val parentNode = FakeParentNode(
            graph = FakeNodeFactoriesBindings(
                mapOf(
                    SignedOutNode::class to AssistedNodeFactory { buildContext, plugins ->
                        SignedOutNode(
                            buildContext = buildContext,
                            plugins = plugins,
                            presenterFactory = { sessionId -> SignedOutPresenter(sessionId, sessionStore) },
                        )
                    },
                )
            )
        )
        return SignedOutFlowNode(
            buildContext = BuildContext(
                ancestryInfo = AncestryInfo.Child(anchor = parentNode),
                savedStateMap = null,
                customisations = NodeCustomisationDirectoryImpl(),
            ),
            plugins = emptyList(),
            sessionStore = sessionStore,
        ).also { it.parentNodeTestHelper() }
    }
}
