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

package io.element.android.libraries.matrix.api.notification

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.notification.aNotificationData
import org.junit.Test

class NotificationDataTest {
    @Test
    fun `getSenderName should return user id if there is no sender name`() {
        val sut = aNotificationData(
            senderDisplayName = null,
            senderIsNameAmbiguous = false,
        )
        assertThat(sut.getDisambiguatedDisplayName(A_USER_ID)).isEqualTo("@alice:server.org")
    }

    @Test
    fun `getSenderName should return sender name if defined`() {
        val sut = aNotificationData(
            senderDisplayName = "Alice",
            senderIsNameAmbiguous = false,
        )
        assertThat(sut.getDisambiguatedDisplayName(A_USER_ID)).isEqualTo("Alice")
    }

    @Test
    fun `getSenderName should return sender name and user id in case of ambiguous display name`() {
        val sut = aNotificationData(
            senderDisplayName = "Alice",
            senderIsNameAmbiguous = true,
        )
        assertThat(sut.getDisambiguatedDisplayName(A_USER_ID)).isEqualTo("Alice (@alice:server.org)")
    }
}
