/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultCanIncludeLogsProviderTest {
    @Test
    fun `logs can be included when there is no session`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(sessionIds = emptyList())
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `logs can be included when there is a single session, even with bug report disabled`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1),
            urls = mapOf(A_SESSION_ID_1 to BugReportUrl.Disabled),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `logs can be included when all the sessions use the same bug report url`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org"),
                A_SESSION_ID_2 to BugReportUrl.Custom("https://a.org"),
            ),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `logs cannot be included when the sessions use different bug report urls`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org"),
                A_SESSION_ID_2 to BugReportUrl.Custom("https://b.org"),
            ),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `logs cannot be included when a session has bug report disabled`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org"),
                A_SESSION_ID_2 to BugReportUrl.Disabled,
            ),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `logs cannot be included when all the sessions have bug report disabled`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Disabled,
                A_SESSION_ID_2 to BugReportUrl.Disabled,
            ),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `logs cannot be included when a session uses a custom url and another one the default url`() = runTest {
        val sut = createDefaultCanIncludeLogsProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org"),
                A_SESSION_ID_2 to BugReportUrl.UseDefault,
            ),
        )
        sut.canIncludeLogs().test {
            assertThat(awaitItem()).isFalse()
        }
    }
}

private val A_SESSION_ID_1 = SessionId("@alice:server1.org")
private val A_SESSION_ID_2 = SessionId("@bob:server2.org")

private fun createDefaultCanIncludeLogsProvider(
    sessionIds: List<SessionId>,
    urls: Map<SessionId, BugReportUrl> = emptyMap(),
) = DefaultCanIncludeLogsProvider(
    enterpriseService = FakeEnterpriseService(
        bugReportUrlFlowResult = { sessionId -> flowOf(urls[sessionId] ?: BugReportUrl.UseDefault) },
    ),
    sessionStore = InMemorySessionStore(
        initialList = sessionIds.map { aSessionData(sessionId = it.value) },
    ),
)
