/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.OneTimeWorkRequest
import androidx.work.hasKeyWithValueOfType
import com.google.common.truth.Truth.assertThat
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(AndroidJUnit4::class)
class DefaultSyncPendingNotificationsRequestBuilderTest {
    @Test
    fun `build - success API 33`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            assertThat(result.type).isInstanceOf(WorkManagerWorkerType.Unique::class.java)
            result.request.run {
                assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
                assertThat(workSpec.input.hasKeyWithValueOfType<String>(SyncPendingNotificationsRequestBuilder.SESSION_ID)).isTrue()
                assertThat(workSpec.hasConstraints()).isTrue()
                // True in API 33+
                assertThat(workSpec.expedited).isTrue()
                assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
            }
        }
    }

    @Test
    fun `build - success API 32 and lower`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 32,
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()

        results.getOrNull()!!.first().let { result ->
            assertThat(result.type).isInstanceOf(WorkManagerWorkerType.Unique::class.java)
            result.request.run {
                assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
                assertThat(workSpec.input.hasKeyWithValueOfType<String>(SyncPendingNotificationsRequestBuilder.SESSION_ID)).isTrue()
                assertThat(workSpec.hasConstraints()).isTrue()
                // False before API 33
                assertThat(workSpec.expedited).isFalse()
                assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
            }
        }
    }

    @Test
    fun `build - has NET_CAPABILITY_VALIDATED constraint if not in air-gapped env`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = false,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to true
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isTrue()
            }
        }
    }

    @Test
    fun `build - does not have NET_CAPABILITY_VALIDATED constraint if in air-gapped env`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = true,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to true
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isFalse()
            }
        }
    }

    @Test
    fun `build - does not have NET_CAPABILITY_VALIDATED constraint if feature flag is disabled`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = false,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to false
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isFalse()
            }
        }
    }
}

private fun createSyncPendingNotificationsRequestBuilder(
    sessionId: SessionId,
    sdkVersion: Int = 33,
    isInAirGapEnvironment: Boolean = false,
    featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
) = DefaultSyncPendingNotificationsRequestBuilder(
    sessionId = sessionId,
    buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkVersion),
    networkMonitor = FakeNetworkMonitor().apply { givenIsInAirGappedEnvironment(isInAirGapEnvironment) },
    featureFlagService = featureFlagService,
)
