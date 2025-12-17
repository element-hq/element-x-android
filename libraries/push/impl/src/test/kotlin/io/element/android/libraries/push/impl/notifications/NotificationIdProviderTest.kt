/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
