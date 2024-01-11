/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
class IntentProviderImplTest {
    @Test
    fun `test getViewRoomIntent with Session`() {
        val sut = createIntentProviderImpl()
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
        val sut = createIntentProviderImpl()
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
        val sut = createIntentProviderImpl()
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            threadId = A_THREAD_ID,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId")
    }

    @Test
    fun `test getInviteListIntent`() {
        val sut = createIntentProviderImpl()
        val result = sut.getInviteListIntent(
            sessionId = A_SESSION_ID,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("elementx://open/@alice:server.org/invites")
    }

    private fun createIntentProviderImpl(): IntentProviderImpl {
        return IntentProviderImpl(
            context = RuntimeEnvironment.getApplication() as Context,
            deepLinkCreator = DeepLinkCreator(),
        )
    }

    private fun Intent.commonAssertions() {
        assertThat(action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(component?.className).isEqualTo(MainActivity::class.java.name)
    }
}
