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

package io.element.android.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import org.junit.Test

class NotificationIdProviderTest {
    @Test
    fun `test notification id provider`() {
        val sut = NotificationIdProvider
        val offsetForASessionId = 305_410
        assertThat(sut.getSummaryNotificationId(A_SESSION_ID)).isEqualTo(offsetForASessionId + 0)
        assertThat(sut.getRoomMessagesNotificationId(A_SESSION_ID)).isEqualTo(offsetForASessionId + 1)
        assertThat(sut.getRoomEventNotificationId(A_SESSION_ID)).isEqualTo(offsetForASessionId + 2)
        assertThat(sut.getRoomInvitationNotificationId(A_SESSION_ID)).isEqualTo(offsetForASessionId + 3)
        // Check that value will be different for another sessionId
        assertThat(sut.getSummaryNotificationId(A_SESSION_ID)).isNotEqualTo(sut.getSummaryNotificationId(A_SESSION_ID_2))
    }
}
