/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.workmanager

import android.net.NetworkCapabilities
import androidx.work.PeriodicWorkRequest
import androidx.work.hasKeyWithValueOfType
import com.google.common.truth.Truth.assertThat
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DefaultSearchBackfillRequestBuilderTest : RobolectricTest() {
    private val expectedTag = workManagerTag(A_SESSION_ID, WorkManagerRequestType.SEARCH_BACKFILL)

    @Test
    fun `build - creates a periodic request with session id and tag`() = runTest {
        val result = createBuilder().build().getOrThrow().single()

        assertThat(result.type).isEqualTo(WorkManagerWorkerType.Default)
        result.request.run {
            assertThat(this).isInstanceOf(PeriodicWorkRequest::class.java)
            assertThat(workSpec.isPeriodic).isTrue()
            assertThat(workSpec.intervalDuration).isEqualTo(1.hours.inWholeMilliseconds)
            assertThat(workSpec.initialDelay).isEqualTo(5.minutes.inWholeMilliseconds)
            assertThat(workSpec.input.hasKeyWithValueOfType<String>(SearchBackfillWorker.SESSION_ID_PARAM)).isTrue()
            assertThat(workSpec.input.getString(SearchBackfillWorker.SESSION_ID_PARAM)).isEqualTo(A_SESSION_ID.value)
            assertThat(tags).contains(expectedTag)
            assertThat(workSpec.workerClassName).isEqualTo(SearchBackfillWorker::class.java.name)
        }
    }

    @Test
    fun `build - requires charging and an unmetered network`() = runTest {
        val result = createBuilder().build().getOrThrow().single()

        val constraints = result.request.workSpec.constraints
        assertThat(constraints.requiresCharging()).isTrue()
        val networkRequest = constraints.requiredNetworkRequest
        assertThat(networkRequest).isNotNull()
        assertThat(networkRequest!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).isTrue()
    }

    @Test
    fun `build - validates the network when not in an air-gapped env`() = runTest {
        val result = createBuilder(isInAirGapEnvironment = false).build().getOrThrow().single()

        val networkRequest = result.request.workSpec.constraints.requiredNetworkRequest!!
        assertThat(networkRequest.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isTrue()
    }

    @Test
    fun `build - does not validate the network when in an air-gapped env`() = runTest {
        val result = createBuilder(isInAirGapEnvironment = true).build().getOrThrow().single()

        val networkRequest = result.request.workSpec.constraints.requiredNetworkRequest!!
        assertThat(networkRequest.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isFalse()
    }

    private fun createBuilder(
        isInAirGapEnvironment: Boolean = false,
    ) = DefaultSearchBackfillRequestBuilder(
        sessionId = A_SESSION_ID,
        networkMonitor = FakeNetworkMonitor().apply { givenIsInAirGappedEnvironment(isInAirGapEnvironment) },
    )
}
