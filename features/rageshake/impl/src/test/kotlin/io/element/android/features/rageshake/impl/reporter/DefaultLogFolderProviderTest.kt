/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultLogFolderProviderTest {
    // The default URL depends on the build configuration, and can be empty.
    private val expectedDefaultFolder = LogFolder.Default
    private val expectedLocalOnlyFolder = if (RageshakeConfig.BUG_REPORT_URL.isEmpty()) LogFolder.Default else LogFolder.LocalOnly

    @Test
    fun `when there is no session, the configuration without session is used`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = emptyList(),
            urls = mapOf(null to BugReportUrl.Custom("https://a.org/submit")),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.Server("a.org_submit"))
        }
    }

    @Test
    fun `a single session using the default url uses the default folder`() = runTest {
        val sut = createDefaultLogFolderProvider(sessionIds = listOf(A_SESSION_ID_1))
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(expectedDefaultFolder)
        }
    }

    @Test
    fun `a single session using a custom url uses a server folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1),
            urls = mapOf(A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org/submit")),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.Server("a.org_submit"))
        }
    }

    @Test
    fun `a single session with bug report disabled uses the local-only folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1),
            urls = mapOf(A_SESSION_ID_1 to BugReportUrl.Disabled),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(expectedLocalOnlyFolder)
        }
    }

    @Test
    fun `sessions using the same custom url use the server folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org/submit"),
                A_SESSION_ID_2 to BugReportUrl.Custom("https://a.org/submit"),
            ),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.Server("a.org_submit"))
        }
    }

    @Test
    fun `sessions using the default url use the default folder`() = runTest {
        val sut = createDefaultLogFolderProvider(sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2))
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(expectedDefaultFolder)
        }
    }

    @Test
    fun `sessions using different custom urls use the local-only folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org/submit"),
                A_SESSION_ID_2 to BugReportUrl.Custom("https://b.org/submit"),
            ),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.LocalOnly)
        }
    }

    @Test
    fun `sessions using a custom url and the default url use the local-only folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org/submit")),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.LocalOnly)
        }
    }

    @Test
    fun `sessions using a custom url and disabled bug report use the local-only folder`() = runTest {
        val sut = createDefaultLogFolderProvider(
            sessionIds = listOf(A_SESSION_ID_1, A_SESSION_ID_2),
            urls = mapOf(
                A_SESSION_ID_1 to BugReportUrl.Custom("https://a.org/submit"),
                A_SESSION_ID_2 to BugReportUrl.Disabled,
            ),
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.LocalOnly)
        }
    }

    @Test
    fun `the folder changes when a session is removed`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData(sessionId = A_SESSION_ID_1.value), aSessionData(sessionId = A_SESSION_ID_2.value)),
        )
        val sut = DefaultLogFolderProvider(
            enterpriseService = FakeEnterpriseService(
                bugReportUrlFlowResult = { sessionId ->
                    flowOf(if (sessionId == A_SESSION_ID_1) BugReportUrl.Custom("https://a.org/submit") else BugReportUrl.Custom("https://b.org/submit"))
                },
            ),
            sessionStore = sessionStore,
        )
        sut.logFolderFlow().test {
            assertThat(awaitItem()).isEqualTo(LogFolder.LocalOnly)
            sessionStore.removeSession(A_SESSION_ID_2.value)
            assertThat(awaitItem()).isEqualTo(LogFolder.Server("a.org_submit"))
        }
    }
}

private val A_SESSION_ID_1 = SessionId("@alice:server1.org")
private val A_SESSION_ID_2 = SessionId("@bob:server2.org")

private fun createDefaultLogFolderProvider(
    sessionIds: List<SessionId>,
    urls: Map<SessionId?, BugReportUrl> = emptyMap(),
) = DefaultLogFolderProvider(
    enterpriseService = FakeEnterpriseService(
        bugReportUrlFlowResult = { sessionId -> flowOf(urls[sessionId] ?: BugReportUrl.UseDefault) },
    ),
    sessionStore = InMemorySessionStore(
        initialList = sessionIds.map { aSessionData(sessionId = it.value) },
    ),
)
