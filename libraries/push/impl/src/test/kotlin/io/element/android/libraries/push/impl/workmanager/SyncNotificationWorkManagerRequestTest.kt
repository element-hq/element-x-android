/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.OneTimeWorkRequest
import androidx.work.hasKeyWithValueOfType
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fixtures.aNotificationEventRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncNotificationWorkManagerRequestTest {
    @Test
    fun `build - success`() = runTest {
        val request = SyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = listOf(aNotificationEventRequest())
        )

        val result = request.build()
        assertThat(result.isSuccess).isTrue()
        result.getOrNull()!!.run {
            assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
            assertThat(workSpec.input.hasKeyWithValueOfType<String>("requests")).isTrue()
            assertThat(workSpec.expedited).isTrue()
            assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
        }
    }

    @Test
    fun `build - empty list of requests fails`() = runTest {
        val request = SyncNotificationWorkManagerRequest(
            sessionId = A_SESSION_ID,
            notificationEventRequests = emptyList()
        )

        val result = request.build()
        assertThat(result.isFailure).isTrue()
    }

    // TODO add test for invalid serialization (how?)
}
