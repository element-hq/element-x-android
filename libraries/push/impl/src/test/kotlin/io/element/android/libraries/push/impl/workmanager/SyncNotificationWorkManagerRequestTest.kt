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
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.fixtures.aNotificationEventRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.collections.first

class SyncNotificationWorkManagerRequestTest {
    @Test
    fun `build - success API 33`() = runTest {
        val request = createSyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = listOf(aNotificationEventRequest()),
            sdkVersion = 33,
        )

        val result = request.build()
        assertThat(result.isSuccess).isTrue()
        result.getOrNull()!!.first().run {
            assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
            assertThat(workSpec.input.hasKeyWithValueOfType<String>("requests")).isTrue()
            // True in API 33+
            assertThat(workSpec.expedited).isTrue()
            assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
        }
    }

    @Test
    fun `build - success API 32 and lower`() = runTest {
        val request = createSyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = listOf(aNotificationEventRequest()),
            sdkVersion = 32,
        )

        val result = request.build()
        assertThat(result.isSuccess).isTrue()
        result.getOrNull()!!.first().run {
            assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
            assertThat(workSpec.input.hasKeyWithValueOfType<String>("requests")).isTrue()
            // False before API 33
            assertThat(workSpec.expedited).isFalse()
            assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
        }
    }

    @Test
    fun `build - empty list of requests fails`() = runTest {
        val request = createSyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = emptyList()
        )

        val result = request.build()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `build - invalid serialization`() = runTest {
        val request = createSyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = listOf(aNotificationEventRequest()),
            workerDataConverter = WorkerDataConverter({ error("error during serialization") })
        )
        val result = request.build()
        assertThat(result.isFailure).isTrue()
    }
}

private fun createSyncNotificationWorkManagerRequest(
    sessionId: SessionId,
    notificationEventRequests: List<NotificationEventRequest>,
    workerDataConverter: WorkerDataConverter = WorkerDataConverter(DefaultJsonProvider()),
    sdkVersion: Int = 33,
) = SyncNotificationWorkManagerRequest(
    sessionId = sessionId,
    notificationEventRequests = notificationEventRequests,
    workerDataConverter = workerDataConverter,
    buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkVersion),
)
