/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
