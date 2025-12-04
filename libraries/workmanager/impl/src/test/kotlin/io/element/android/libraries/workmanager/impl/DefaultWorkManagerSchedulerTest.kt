/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.impl

import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultWorkManagerSchedulerTest {
    @Test
    fun `starts observing sessions on init to remove work for logged out sessions`() = runTest {
        val sessionId = "@session1:matrix.org"
        val sessionStore = InMemorySessionStore(initialList = listOf(aSessionData(sessionId = sessionId)))

        val workManager = spyk<WorkManager>()

        DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            appCoroutineScope = backgroundScope,
            sessionStore = sessionStore,
        )

        // We have a single initial session
        assertThat(sessionStore.numberOfSessions()).isEqualTo(1)

        runCurrent()

        // We remove the session
        sessionStore.removeSession(sessionId)

        runCurrent()

        // The session is now gone and work associated with the session is cancelled
        assertThat(sessionStore.numberOfSessions()).isEqualTo(0)
        verify { workManager.cancelAllWorkByTag("notifications-$sessionId") }
    }

    @Test
    fun `submit builds the request and enqueues it`() = runTest {
        val sessionStore = InMemorySessionStore()
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            appCoroutineScope = backgroundScope,
            sessionStore = sessionStore,
        )

        scheduler.submit(FakeWorkManagerRequest())

        verify { workManager.enqueue(any<List<WorkRequest>>()) }
    }

    @Test
    fun `submit won't do anything if building the work request fails`() = runTest {
        val sessionStore = InMemorySessionStore()
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            appCoroutineScope = backgroundScope,
            sessionStore = sessionStore,
        )

        scheduler.submit(FakeWorkManagerRequest(result = Result.failure(IllegalStateException("Test error"))))

        verify(exactly = 0) { workManager.enqueue(any<List<WorkRequest>>()) }
    }

    @Test
    fun `cancel will cancel all pending work for a session id`() = runTest {
        val sessionStore = InMemorySessionStore()
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            appCoroutineScope = backgroundScope,
            sessionStore = sessionStore,
        )

        val sessionId = SessionId("@alice:matrix.org")
        val tagToRemove = workManagerTag(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC)
        val mockSessionA = mockk<WorkRequest> {
            every { tags } returns setOf(tagToRemove)
        }
        scheduler.submit(FakeWorkManagerRequest(result = Result.success(listOf(mockSessionA))))

        scheduler.cancel(sessionId)

        verify { workManager.cancelAllWorkByTag(tagToRemove) }
    }
}

private class FakeWorkManagerRequest(
    private val result: Result<List<WorkRequest>> = Result.success(listOf()),
) : WorkManagerRequest {
    override fun build(): Result<List<WorkRequest>> {
        return result
    }
}
