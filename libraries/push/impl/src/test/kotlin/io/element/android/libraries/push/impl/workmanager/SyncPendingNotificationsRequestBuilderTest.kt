/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.OneTimeWorkRequest
import androidx.work.hasKeyWithValueOfType
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncPendingNotificationsRequestBuilderTest {
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
                // False before API 33
                assertThat(workSpec.expedited).isFalse()
                assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
            }
        }
    }
}

private fun createSyncPendingNotificationsRequestBuilder(
    sessionId: SessionId,
    sdkVersion: Int = 33,
) = SyncPendingNotificationsRequestBuilder(
    sessionId = sessionId,
    buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkVersion),
)
