/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_APPLICATION_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SignedOutPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val aSessionData = aSessionData()
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData)
        )
        val presenter = createSignedOutPresenter(sessionStore = sessionStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo(AN_APPLICATION_NAME)
            assertThat(initialState.signedOutSession).isEqualTo(aSessionData)
        }
    }

    @Test
    fun `present - sign in again`() = runTest {
        val aSessionData = aSessionData()
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData)
        )
        val presenter = createSignedOutPresenter(sessionStore = sessionStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.signedOutSession).isEqualTo(aSessionData)
            assertThat(sessionStore.getAllSessions()).isNotEmpty()
            assertThat(sessionStore.numberOfSessions()).isEqualTo(1)
            initialState.eventSink(SignedOutEvents.SignInAgain)
            assertThat(awaitItem().signedOutSession).isNull()
            assertThat(sessionStore.getAllSessions()).isEmpty()
            assertThat(sessionStore.numberOfSessions()).isEqualTo(0)
        }
    }
}

internal fun createSignedOutPresenter(
    sessionId: SessionId = A_SESSION_ID,
    sessionStore: SessionStore = InMemorySessionStore(),
): SignedOutPresenter {
    return SignedOutPresenter(
        sessionId = sessionId,
        sessionStore = sessionStore,
        buildMeta = aBuildMeta(applicationName = AN_APPLICATION_NAME),
    )
}
