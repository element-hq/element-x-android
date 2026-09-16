/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fixtures.aFallbackNotifiableEvent
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import org.junit.Test

class FallbackNotificationFactoryTest {
    @Test
    fun `create silent event`() {
        val sut = FallbackNotificationFactory(FakeSystemClock())
        val result = sut.create(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
            cause = "A cause",
            noisy = false,
        )
        assertThat(result).isEqualTo(
            aFallbackNotifiableEvent(
                description = "",
                canBeReplaced = true,
                noisy = false,
                cause = "A cause",
            )
        )
    }

    @Test
    fun `create noisy event`() {
        val sut = FallbackNotificationFactory(FakeSystemClock())
        val result = sut.create(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
            cause = "A cause",
            noisy = true,
        )
        assertThat(result).isEqualTo(
            aFallbackNotifiableEvent(
                description = "",
                canBeReplaced = true,
                noisy = true,
                cause = "A cause",
            )
        )
    }
}
