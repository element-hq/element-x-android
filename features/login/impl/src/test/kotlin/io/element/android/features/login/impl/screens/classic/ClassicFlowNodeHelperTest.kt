/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.login.impl.screens.classic

import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.classic.ElementClassicConnection
import io.element.android.features.login.impl.classic.ElementClassicConnectionState
import io.element.android.features.login.impl.classic.FakeElementClassicConnection
import io.element.android.features.login.impl.classic.anElementClassicReady
import io.element.android.features.login.impl.classic.anElementClassicSession
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Use AndroidJUnit4 for the test with the Bitmap.
@RunWith(AndroidJUnit4::class)
class ClassicFlowNodeHelperTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `after a few seconds in Idle, NavigateToOnBoarding is emitted`() = runTest {
        createHelper()
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToOnBoarding)
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to onboarding if a session with the same account already exists`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(
                        sessionId = A_USER_ID.value,
                    )
                )
            ),
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady()
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToOnBoarding)
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to onboarding if Element Classic is not found`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    ElementClassicConnectionState.ElementClassicNotFound
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToOnBoarding)
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to onboarding if Element Classic has no session`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    ElementClassicConnectionState.ElementClassicReadyNoSession
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToOnBoarding)
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to onboarding if there has been an error`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    ElementClassicConnectionState.Error(A_FAILURE_REASON)
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToOnBoarding)
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to login with classic when the session can be retrieved`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady()
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to login with classic when the session can be retrieved - ignore avatar update`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady()
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                // When the avatar is retrieved, no new event is emitted
                elementClassicConnection.emitState(
                    anElementClassicReady(
                        avatar = createBitmap(1, 1)
                    )
                )
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to login with classic when the session can be retrieved and navigate again once the session is verified`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady(
                        elementClassicSession = anElementClassicSession(
                            secrets = A_SECRET,
                        )
                    )
                )
                val readyState = awaitItem()
                assertThat(readyState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                // When the secret with the key backup is retrieved, NavigateToLoginWithClassic is emitted again
                elementClassicConnection.emitState(
                    anElementClassicReady(
                        elementClassicSession = anElementClassicSession(
                            secrets = A_SECRET + A_SECRET,
                        )
                    )
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to login with classic if a session with another account already exists`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        createHelper(
            elementClassicConnection = elementClassicConnection,
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(
                        sessionId = A_USER_ID_2.value,
                    )
                )
            ),
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady()
                )
                val finalState = awaitItem()
                assertThat(finalState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }

    @Test
    fun `navigate to login with classic but do not navigate to OnBoarding once the user is logged in`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection()
        val sessionStore = InMemorySessionStore(
            initialList = listOf()
        )
        createHelper(
            elementClassicConnection = elementClassicConnection,
            sessionStore = sessionStore,
        )
            .navigationEventFlow()
            .test {
                val initialState = awaitItem()
                assertThat(initialState).isEqualTo(NavigationEvent.Idle)
                elementClassicConnection.emitState(
                    anElementClassicReady()
                )
                val navigateToLoginWithClassicState = awaitItem()
                assertThat(navigateToLoginWithClassicState).isEqualTo(NavigationEvent.NavigateToLoginWithClassic(A_USER_ID))
                // User actually logs in
                sessionStore.addSession(
                    aSessionData(
                        sessionId = A_USER_ID.value,
                    )
                )
                advanceTimeBy(10_000)
                expectNoEvents()
            }
    }
}

private fun createHelper(
    elementClassicConnection: ElementClassicConnection = FakeElementClassicConnection(),
    sessionStore: SessionStore = InMemorySessionStore(),
) = ClassicFlowNodeHelper(
    elementClassicConnection = elementClassicConnection,
    sessionStore = sessionStore,
)
