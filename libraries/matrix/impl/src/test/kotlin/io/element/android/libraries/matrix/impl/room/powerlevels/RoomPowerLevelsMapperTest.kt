/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomPowerLevels
import org.junit.Test

class RoomPowerLevelsMapperTest {
    @Test
    fun `test that mapping of RoomPowerLevels is correct`() {
        assertThat(
            RoomPowerLevelsMapper.map(
                aRustRoomPowerLevels(
                    ban = 1,
                    invite = 2,
                    kick = 3,
                    redact = 4,
                    eventsDefault = 5,
                    stateDefault = 6,
                    usersDefault = 7,
                    roomName = 8,
                    roomAvatar = 9,
                    roomTopic = 10,
                )
            )
        ).isEqualTo(
            RoomPowerLevels(
                ban = 1,
                invite = 2,
                kick = 3,
                sendEvents = 5,
                redactEvents = 4,
                roomName = 8,
                roomAvatar = 9,
                roomTopic = 10,
            )
        )
    }
}
