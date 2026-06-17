/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkAllRoomsAsReadTest {
    @Test
    fun `invoke - delegates to client and clears all notifications`() = runTest {
        val markAllRoomsAsReadLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = markAllRoomsAsReadLambda,
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isSuccess).isTrue()
        markAllRoomsAsReadLambda.assertions().isCalledOnce()
        clearAllMessagesEventsLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `invoke - does not clear notifications when client fails`() = runTest {
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = { Result.failure(IllegalStateException("Failed")) },
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isFailure).isTrue()
        clearAllMessagesEventsLambda.assertions().isNeverCalled()
    }
}
