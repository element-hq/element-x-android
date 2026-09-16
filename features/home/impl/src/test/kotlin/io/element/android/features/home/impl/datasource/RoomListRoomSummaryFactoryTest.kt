/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomSummary
import org.junit.Test

class RoomListRoomSummaryFactoryTest {
    @Test
    fun `a room with two members does not prefix its preview with the sender`() {
        val formatter = FakeRoomLatestEventFormatter()
        val factory = aRoomListRoomSummaryFactory(roomLatestEventFormatter = formatter)

        factory.create(aRoomSummary(info = aRoomInfo(isDm = false, activeMembersCount = 2)))

        assertThat(formatter.lastIsDmRoom).isTrue()
    }

    @Test
    fun `a room with more than two members prefixes its preview with the sender`() {
        val formatter = FakeRoomLatestEventFormatter()
        val factory = aRoomListRoomSummaryFactory(roomLatestEventFormatter = formatter)

        factory.create(aRoomSummary(info = aRoomInfo(isDm = false, activeMembersCount = 3)))

        assertThat(formatter.lastIsDmRoom).isFalse()
    }
}

fun aRoomListRoomSummaryFactory(
    dateFormatter: DateFormatter = FakeDateFormatter { _, _, _ -> "Today" },
    roomLatestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
) = RoomListRoomSummaryFactory(
    dateFormatter = dateFormatter,
    roomLatestEventFormatter = roomLatestEventFormatter,
)
