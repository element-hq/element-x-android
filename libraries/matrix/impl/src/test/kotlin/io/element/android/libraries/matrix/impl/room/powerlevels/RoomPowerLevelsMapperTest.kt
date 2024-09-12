/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomPowerLevels as RustRoomPowerLevels

class RoomPowerLevelsMapperTest {
    @Test
    fun `test that mapping of RoomPowerLevels is correct`() {
        assertThat(
            RoomPowerLevelsMapper.map(
                RustRoomPowerLevels(
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
            MatrixRoomPowerLevels(
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
