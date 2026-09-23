/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

class DefaultPttEnabledSourceTest : RobolectricTest() {
    @Test
    fun `availability and audio mode are independent and disabling availability clears PTT`() = runTest {
        val source = DefaultPttEnabledSource(
            context = ApplicationProvider.getApplicationContext(),
            appCoroutineScope = backgroundScope,
            sessionObserver = mockk<SessionObserver>(relaxed = true),
        )
        val session = SessionId("@${UUID.randomUUID()}:example.org")
        val room = RoomId("!room:example.org")
        assertThat(source.availableRoomIdsFlow(session).first()).isEmpty()
        source.setEnabled(session, room, true)
        assertThat(source.isPttEnabled(session, room)).isFalse()

        source.setAvailable(session, room, true)
        assertThat(source.availableRoomIdsFlow(session).first()).containsExactly(room)
        assertThat(source.isPttEnabled(session, room)).isFalse()
        source.setEnabled(session, room, true)
        assertThat(source.isPttEnabled(session, room)).isTrue()

        source.setEnabled(session, room, false)
        assertThat(source.availableRoomIdsFlow(session).first()).containsExactly(room)
        assertThat(source.isPttEnabled(session, room)).isFalse()

        source.setEnabled(session, room, true)
        source.setAvailable(session, room, false)
        assertThat(source.availableRoomIdsFlow(session).first()).isEmpty()
        assertThat(source.isPttEnabled(session, room)).isFalse()
    }

    @Test
    fun `availability and selection do not leak across rooms or sessions`() = runTest {
        val source = DefaultPttEnabledSource(
            context = ApplicationProvider.getApplicationContext(),
            appCoroutineScope = backgroundScope,
            sessionObserver = mockk<SessionObserver>(relaxed = true),
        )
        val session = SessionId("@${UUID.randomUUID()}:example.org")
        val otherSession = SessionId("@${UUID.randomUUID()}:example.org")
        val room = RoomId("!room:example.org")
        val otherRoom = RoomId("!other:example.org")
        source.setAvailable(session, room, true)
        source.setEnabled(session, room, true)
        assertThat(source.availableRoomIdsFlow(otherSession).first()).isEmpty()
        assertThat(source.isPttEnabled(otherSession, room)).isFalse()
        assertThat(source.isPttEnabled(session, otherRoom)).isFalse()
    }
}
