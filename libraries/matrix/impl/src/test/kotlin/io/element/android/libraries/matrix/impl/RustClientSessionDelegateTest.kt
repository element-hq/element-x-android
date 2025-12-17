/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSession
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RustClientSessionDelegateTest {
    @Test
    fun `saveSessionInKeychain should update the store`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    accessToken = "anAccessToken",
                    refreshToken = "aRefreshToken",
                )
            )
        )
        val sut = aRustClientSessionDelegate(sessionStore)
        sut.saveSessionInKeychain(
            aRustSession(
                accessToken = "at",
                refreshToken = "rt",
            )
        )
        runCurrent()
        val result = sessionStore.getLatestSession()
        assertThat(result!!.accessToken).isEqualTo("at")
        assertThat(result.refreshToken).isEqualTo("rt")
    }
}

fun TestScope.aRustClientSessionDelegate(
    sessionStore: SessionStore = InMemorySessionStore(),
) = RustClientSessionDelegate(
    sessionStore = sessionStore,
    appCoroutineScope = this,
    coroutineDispatchers = testCoroutineDispatchers(),
)
