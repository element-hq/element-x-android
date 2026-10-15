/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.ptt.test.FakePttFeatureAvailability
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PttFeatureGateTest {
    @Test
    fun `default implementation never offers PTT`() = runTest {
        assertThat(NoopPttFeatureAvailability().isAvailable.first()).isFalse()
    }

    @Test
    fun `feature gate masks persisted rooms and rejects enabling while off`() = runTest {
        val availability = FakePttFeatureAvailability(false)
        val store = mockk<PttEnabledStore>(relaxed = true) {
            every { availableRoomIds() } returns flowOf(setOf(A_ROOM_ID))
            every { enabledRoomIds() } returns flowOf(setOf(A_ROOM_ID))
        }
        val service = DefaultPttRoomService(mockk<JoinedRoom> { every { roomId } returns A_ROOM_ID }, store, availability)
        assertThat(service.isPttAvailableFlow().first()).isFalse()
        assertThat(service.isPttEnabledFlow().first()).isFalse()
        service.setPttEnabled(true)
        service.setPttAvailable(true)
        coVerify(exactly = 0) { store.setEnabled(A_ROOM_ID, true) }
        coVerify(exactly = 0) { store.setAvailable(A_ROOM_ID, true) }
        availability.isAvailable.value = true
        assertThat(service.isPttAvailableFlow().first()).isTrue()
        assertThat(service.isPttEnabledFlow().first()).isTrue()
    }
}
