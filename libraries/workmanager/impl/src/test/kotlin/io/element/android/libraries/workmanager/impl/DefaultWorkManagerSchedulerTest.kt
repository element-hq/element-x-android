/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.impl

import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.test.observer.FakeSessionObserver
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
        val sessionObserver = FakeSessionObserver()

        val workManager = spyk<WorkManager>()

        DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            sessionObserver = sessionObserver,
        )

        // We remove the session
        sessionObserver.onSessionDeleted(sessionId)

        runCurrent()

        // The session is now gone and work associated with the session is cancelled
        verify { workManager.cancelAllWorkByTag("notifications-$sessionId") }
    }

    @Test
    fun `submit builds the request and enqueues it`() = runTest {
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            sessionObserver = FakeSessionObserver(),
        )

        scheduler.submit(FakeWorkManagerRequest())

        verify { workManager.enqueue(any<List<WorkRequest>>()) }
    }

    @Test
    fun `submit won't do anything if building the work request fails`() = runTest {
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            sessionObserver = FakeSessionObserver(),
        )

        scheduler.submit(FakeWorkManagerRequest(result = Result.failure(IllegalStateException("Test error"))))

        verify(exactly = 0) { workManager.enqueue(any<List<WorkRequest>>()) }
    }

    @Test
    fun `cancel will cancel all pending work for a session id`() = runTest {
        val workManager = spyk<WorkManager>()

        val scheduler = DefaultWorkManagerScheduler(
            lazyWorkManager = lazy { workManager },
            sessionObserver = FakeSessionObserver(),
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
