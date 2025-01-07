/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.intent

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.deeplink.DeepLinkCreator
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.x.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultIntentProviderTest {
    @Test
    fun `test getViewRoomIntent with Session`() {
        val sut = createDefaultIntentProvider()
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = null,
            threadId = null,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("elementx://open/@alice:server.org")
    }

    @Test
    fun `test getViewRoomIntent with Session and Room`() {
        val sut = createDefaultIntentProvider()
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            threadId = null,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain")
    }

    @Test
    fun `test getViewRoomIntent with Session, Room and Thread`() {
        val sut = createDefaultIntentProvider()
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            threadId = A_THREAD_ID,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId")
    }

    private fun createDefaultIntentProvider(): DefaultIntentProvider {
        return DefaultIntentProvider(
            context = RuntimeEnvironment.getApplication() as Context,
            deepLinkCreator = DeepLinkCreator(),
        )
    }

    private fun Intent.commonAssertions() {
        assertThat(action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(component?.className).isEqualTo(MainActivity::class.java.name)
    }
}
