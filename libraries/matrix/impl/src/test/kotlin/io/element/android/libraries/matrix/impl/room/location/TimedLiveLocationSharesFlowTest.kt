/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimedLiveLocationSharesFlowTest {
    @Test
    fun `it keeps emitting shares for subsequent expiries without upstream changes`() = runTest {
        val shares = listOf(
            aLiveLocationShare(userId = "@alice:server", endTimestamp = 1_000),
            aLiveLocationShare(userId = "@bob:server", endTimestamp = 2_000),
            aLiveLocationShare(userId = "@carol:server", endTimestamp = 3_000),
        )

        flowOf(shares)
            .timedByExpiry(currentTimeMillis = { testScheduler.currentTime })
            .test {
                assertThat(awaitItem()).isEqualTo(shares)

                advanceTimeBy(1_000)
                assertThat(awaitItem()).isEqualTo(shares.drop(1))

                advanceTimeBy(999)
                expectNoEvents()

                advanceTimeBy(1)
                assertThat(awaitItem()).isEqualTo(shares.drop(2))

                advanceTimeBy(999)
                expectNoEvents()

                advanceTimeBy(1)
                assertThat(awaitItem()).isEmpty()

                awaitComplete()
            }
    }

    @Test
    fun `it does not double-emit when a share is already expired on receipt`() = runTest {
        val shares = listOf(
            aLiveLocationShare(userId = "@alice:server", endTimestamp = 500),
            aLiveLocationShare(userId = "@bob:server", endTimestamp = 2_000),
        )

        flowOf(shares)
            .timedByExpiry(currentTimeMillis = { 1_000 + testScheduler.currentTime })
            .test {
                assertThat(awaitItem()).isEqualTo(shares.drop(1))
                expectNoEvents()

                advanceTimeBy(999)
                expectNoEvents()

                advanceTimeBy(1)
                assertThat(awaitItem()).isEmpty()

                awaitComplete()
            }
    }

    @Test
    fun `it reschedules timed emission when upstream shares change`() = runTest {
        val upstream = MutableSharedFlow<List<LiveLocationShare>>(extraBufferCapacity = 1)
        val initialShares = listOf(aLiveLocationShare(endTimestamp = 10_000))
        val updatedShares = listOf(
            aLiveLocationShare(userId = "@alice:server", endTimestamp = 10_000),
            aLiveLocationShare(userId = "@bob:server", endTimestamp = 6_000),
        )

        upstream
            .timedByExpiry(currentTimeMillis = { testScheduler.currentTime })
            .test {
                upstream.emit(initialShares)
                assertThat(awaitItem()).isEqualTo(initialShares)

                advanceTimeBy(5_000)
                upstream.emit(updatedShares)
                assertThat(awaitItem()).isEqualTo(updatedShares)

                advanceTimeBy(999)
                expectNoEvents()

                advanceTimeBy(1)
                assertThat(awaitItem()).isEqualTo(updatedShares.take(1))

                advanceTimeBy(3_999)
                expectNoEvents()

                advanceTimeBy(1)
                assertThat(awaitItem()).isEmpty()
            }
    }

    @Test
    fun `it completes after the last scheduled re-emission when upstream completes`() = runTest {
        val shares = listOf(aLiveLocationShare(endTimestamp = 1_000))
        flowOf(shares)
            .timedByExpiry(currentTimeMillis = { testScheduler.currentTime })
            .test {
                assertThat(awaitItem()).isEqualTo(shares)

                advanceTimeBy(1_000)
                assertThat(awaitItem()).isEmpty()

                awaitComplete()
            }
    }

    @Test
    fun `it completes immediately when upstream emits nothing`() = runTest {
        emptyFlow<List<LiveLocationShare>>()
            .timedByExpiry(currentTimeMillis = { testScheduler.currentTime })
            .test {
                awaitComplete()
            }
    }
}

private fun aLiveLocationShare(
    userId: String = "@user:server",
    endTimestamp: Long,
): LiveLocationShare {
    return LiveLocationShare(
        userId = UserId(userId),
        lastLocation = null,
        startTimestamp = 0L,
        endTimestamp = endTimestamp,
    )
}
