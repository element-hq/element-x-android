/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("SameParameterValue")

package io.element.android.x.intent

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.deeplink.api.DeepLinkCreator
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.x.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultIntentProviderTest {
    @Test
    fun `test getViewRoomIntent with data`() {
        val deepLinkCreator = lambdaRecorder<SessionId, RoomId?, ThreadId?, EventId?, String> { _, _, _, _ -> "deepLinkCreatorResult" }
        val sut = createDefaultIntentProvider(
            deepLinkCreator = { sessionId, roomId, threadId, eventId -> deepLinkCreator.invoke(sessionId, roomId, threadId, eventId) },
        )
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            threadId = A_THREAD_ID,
            eventId = AN_EVENT_ID,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("deepLinkCreatorResult")
        deepLinkCreator.assertions().isCalledOnce().with(
            value(A_SESSION_ID),
            value(A_ROOM_ID),
            value(A_THREAD_ID),
            value(AN_EVENT_ID),
        )
    }

    private fun createDefaultIntentProvider(
        deepLinkCreator: DeepLinkCreator = DeepLinkCreator { _, _, _, _ -> "" },
    ): DefaultIntentProvider {
        return DefaultIntentProvider(
            context = RuntimeEnvironment.getApplication() as Context,
            deepLinkCreator = deepLinkCreator,
        )
    }

    private fun Intent.commonAssertions() {
        assertThat(action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(component?.className).isEqualTo(MainActivity::class.java.name)
    }
}
