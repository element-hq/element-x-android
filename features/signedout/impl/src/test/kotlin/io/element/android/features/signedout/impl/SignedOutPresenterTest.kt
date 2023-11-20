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

package io.element.android.features.signedout.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SignedOutPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val appName = "AppName"

    @Test
    fun `present - initial state`() = runTest {
        val aSessionData = aSessionData()
        val sessionStore = InMemorySessionStore().apply {
            storeData(aSessionData)
        }
        val presenter = createSignedOutPresenter(sessionStore = sessionStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo(appName)
            assertThat(initialState.signedOutSession).isEqualTo(aSessionData)
        }
    }

    @Test
    fun `present - sign in again`() = runTest {
        val aSessionData = aSessionData()
        val sessionStore = InMemorySessionStore().apply {
            storeData(aSessionData)
        }
        val presenter = createSignedOutPresenter(sessionStore = sessionStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.signedOutSession).isEqualTo(aSessionData)
            assertThat(sessionStore.getAllSessions()).isNotEmpty()
            initialState.eventSink(SignedOutEvents.SignInAgain)
            assertThat(awaitItem().signedOutSession).isNull()
            assertThat(sessionStore.getAllSessions()).isEmpty()
        }
    }

    private fun createSignedOutPresenter(
        sessionId: SessionId = A_SESSION_ID,
        sessionStore: SessionStore = InMemorySessionStore(),
    ): SignedOutPresenter {
        return SignedOutPresenter(
            sessionId = sessionId.value,
            sessionStore = sessionStore,
            buildMeta = aBuildMeta(applicationName = appName),
        )
    }
}
